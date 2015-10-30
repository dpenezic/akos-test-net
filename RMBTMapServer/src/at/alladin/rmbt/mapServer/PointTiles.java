/*******************************************************************************
 * Copyright 2013-2015 alladin-IT GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package at.alladin.rmbt.mapServer;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.restlet.data.Form;

import at.alladin.rmbt.mapServer.MapServerOptions.MapOption;
import at.alladin.rmbt.mapServer.MapServerOptions.SQLFilter;
import at.alladin.rmbt.mapServer.parameters.PointTileParameters;
import at.alladin.rmbt.mapServer.parameters.TileParameters.Path;

public class PointTiles extends TileRestlet<PointTileParameters>
{
    static class Dot
    {
        public Dot(final double x, final double y, final Color color, final boolean highlight)
        {
            this.x = x;
            this.y = y;
            this.color = color;
            this.highlight = highlight;
        }
        
        final double x;
        final double y;
        final Color color;
        final boolean highlight;
    }
    
    @Override
    protected PointTileParameters getTileParameters(Path path, Form params)
    {
        return new PointTileParameters(path, params);
    }

    @Override
    protected byte[] generateTile(PointTileParameters params, final int tileSizeIdx,
            final int zoom, final DBox box,
            final MapOption mo, final List<SQLFilter> filters, final float quantile)
    {
        filters.add(mso.getAccuracyMapFilter());
        
        final String hightlightUUIDString = params.getHighlight();
        UUID hightlightUUID = null;
        if (hightlightUUIDString != null)
            try
            {
                hightlightUUID = UUID.fromString(hightlightUUIDString);
            }
            catch (final Exception e)
            {
            }
        
        final double diameter = params.getPointDiameter();
        final double radius = diameter / 2d;
        final double triangleSide = diameter * 1.5;
        final double triangleHeight = Math.sqrt(3) / 2d * triangleSide;
        final int transparency = (int) Math.round(params.getTransparency() * 255);
        final boolean noFill = params.isNoFill();
        final boolean noColor = params.isNoColor();
        
        final Color borderColor = new Color(0, 0, 0, transparency);
        final Color highlightBorderColor = new Color(0, 0, 0, transparency);
        final Color colorGreen = new Color(0, 255, 0, transparency);
        final Color colorYellow = new Color(255, 255, 0, transparency);
        final Color colorRed = new Color(255, 0, 0, transparency);
        final Color colorGray = new Color(128, 128, 128, transparency);
        
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            final List<Dot> dots = new ArrayList<>();
            
            final StringBuilder whereSQL = new StringBuilder(mo.sqlFilter);
            for (final SQLFilter sf : filters)
                whereSQL.append(" AND ").append(sf.where);
            
            con = DbConnection.getConnection();
            final String sql = String.format("SELECT ST_X(t.location) x, ST_Y(t.location) y, \"%s\" val"
                    + (hightlightUUID == null ? "" : " , c.uid") + " FROM v_test t"
                    + (hightlightUUID == null ? "" : " LEFT JOIN client c ON (t.client_id=c.uid AND c.uuid=?)")
                    + " WHERE "
                    + " %s"
                    + " AND location && ST_SetSRID(ST_MakeBox2D(ST_Point(?,?), ST_Point(?,?)), 900913)"
                    + " ORDER BY"
                    + (hightlightUUID == null ? "" : " c.uid DESC, ") + " t.uid", mo.valueColumn, whereSQL);
            
            //System.out.println(sql);
            
            ps = con.prepareStatement(sql);
            
            int i = 1;
            
            if (hightlightUUID != null)
                ps.setObject(i++, hightlightUUID);
            
            for (final SQLFilter sf : filters)
                i = sf.fillParams(i, ps);
            
            final double margin = box.res * triangleSide;
            ps.setDouble(i++, box.x1 - margin);
            ps.setDouble(i++, box.y1 - margin);
            ps.setDouble(i++, box.x2 + margin);
            ps.setDouble(i++, box.y2 + margin);
            
//            System.out.println(ps);
            
            if (!ps.execute())
                throw new IllegalArgumentException(ps.getWarnings());
            
            rs = ps.getResultSet();
            boolean _emptyTile = true;
            while (rs.next())
            {
                _emptyTile = false;
                
                final double cx = rs.getDouble(1);
                final double cy = rs.getDouble(2);
                final long value = rs.getLong(3);
                
                final boolean highlight;
                if (hightlightUUID != null)
                {
                    final Object clientUUID = rs.getObject(4);
                    highlight = clientUUID != null;
                }
                else
                    highlight = false;
                
                final int classification = noColor || noFill ? 0 : mo.getClassification(value);
                
                final Color color;
                switch (classification)
                {
                case 3:
                    color = colorGreen;
                    break;
                case 2:
                    color = colorYellow;
                    break;
                case 1:
                    color = colorRed;
                    break;
                default:
                    color = colorGray;
                    break;
                }
                
                dots.add(new Dot(cx, cy, color, highlight));
            }
            
            if (_emptyTile)
                return null;
                    
            final Image img = images[tileSizeIdx].get();
            final Graphics2D g = img.g;
            
            g.setBackground(new Color(0, 0, 0, 0));
            g.clearRect(0, 0, img.width, img.height);
            g.setComposite(AlphaComposite.Src);
            g.setStroke((new BasicStroke(((float) diameter / 8f))));
            
            final Path2D.Double triangle = new Path2D.Double();
            final Ellipse2D.Double shape = new Ellipse2D.Double(0, 0, diameter, diameter);
            
            for (final Dot dot : dots)
            {
                final double relX = (dot.x - box.x1) / box.res;
                final double relY = TILE_SIZES[tileSizeIdx] - (dot.y - box.y1) / box.res;
                
                if (dot.highlight)
                {
                    triangle.reset();
                    triangle.moveTo(relX, relY - triangleHeight / 3 * 2);
                    triangle.lineTo(relX - triangleSide / 2, relY + triangleHeight / 3);
                    triangle.lineTo(relX + triangleSide / 2, relY + triangleHeight / 3);
                    triangle.closePath();
                    if (!noFill)
                    {
                        g.setPaint(dot.color);
                        g.fill(triangle);
                    }
                    g.setPaint(highlightBorderColor);
                    g.draw(triangle);
                }
                else
                {
                    shape.x = relX - radius;
                    shape.y = relY - radius;
                    if (!noFill)
                    {
                        g.setPaint(dot.color);
                        g.fill(shape);
                    }
                    g.setPaint(borderColor);
                    g.draw(shape);
                }
            }
            
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img.bi, "png", baos);
            final byte[] data = baos.toByteArray();
            return data;
        }
        catch (final Exception e)
        {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
        finally
        {
            try
            {
                if (rs != null)
                    rs.close();
                if (ps != null)
                    ps.close();
                if (con != null)
                    con.close();
            }
            catch (final SQLException e)
            {
                e.printStackTrace();
                throw new IllegalStateException(e);
            }
        }
    }
}
