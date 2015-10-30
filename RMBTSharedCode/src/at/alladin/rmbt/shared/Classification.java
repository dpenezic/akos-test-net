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
package at.alladin.rmbt.shared;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

public final class Classification
{
    public static Classification getInstance()
    {
        return instance;
    }
    
    private static Classification instance;
    
    public static void initInstance(Connection conn)
    {
        instance = new Classification(conn);
    }
    
    private Classification(Connection conn)
    {
        int[] uploadValues = null;
        int[] downloadValues = null;
        try (PreparedStatement ps = conn.prepareStatement("SELECT value FROM settings WHERE key = ?"))
        {
            try
            {
                uploadValues = getIntValues(ps, "threshold_upload", 2);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            
            try
            {
                downloadValues = getIntValues(ps, "threshold_download", 2);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        if (uploadValues == null)
            uploadValues = new int[] { 1000, 500 }; // default
        THRESHOLD_UPLOAD = uploadValues;
        THRESHOLD_UPLOAD_CAPTIONS = getCaptions(uploadValues);
        
        if (downloadValues == null)
            downloadValues = new int[] { 2000, 1000 }; // default
        THRESHOLD_DOWNLOAD = downloadValues;
        THRESHOLD_DOWNLOAD_CAPTIONS = getCaptions(downloadValues);
    }

    private static String[] getCaptions(int[] values)
    {
        final String[] result = new String[values.length];
        for (int i = 0; i < values.length; i++)
            result[i] = String.format(Locale.US, "%.1f", ((double)values[i]) / 1000);
        return result;
    }

    private static int[] getIntValues(PreparedStatement ps, String key, int expectCount) throws SQLException, NumberFormatException, IllegalArgumentException
    {
        ps.setString(1, key);
        try (ResultSet rs = ps.executeQuery())
        {
            if (! rs.next())
                return null;
            final String value = rs.getString("value");
            if (value == null)
                return null;
            final String[] parts = value.split(";");
            if (parts.length != expectCount)
                throw new IllegalArgumentException(String.format(Locale.US, "unexpected number of parameters (expected %d): \"%s\"", expectCount, value));
            final int[] result = new int[parts.length];
            for (int i = 0; i < parts.length; i++)
                result[i] = Integer.parseInt(parts[i]);
            return result;
        }
    }
    
    public final int[] THRESHOLD_UPLOAD;
    public final String[] THRESHOLD_UPLOAD_CAPTIONS;
    
    public final int[] THRESHOLD_DOWNLOAD;
    public final String[] THRESHOLD_DOWNLOAD_CAPTIONS;
    
    public final int[] THRESHOLD_PING = { 25000000, 75000000 };
    public final String[] THRESHOLD_PING_CAPTIONS = { "25", "75" };
    
    // RSSI limits used for 2G,3G (and 4G when RSSI is used)
    // only odd values are reported by 2G/3G 
    public final int[] THRESHOLD_SIGNAL_MOBILE = { -85, -101 }; // -85 is still green, -101 is still yellow
    public final String[] THRESHOLD_SIGNAL_MOBILE_CAPTIONS = { "-85", "-101" };
    
    // RSRP limit used for 4G
    public final int[] THRESHOLD_SIGNAL_RSRP = { -95, -111 };
    public final String[] THRESHOLD_SIGNAL_RSRP_CAPTIONS = { "-95", "-111" };

    // RSSI limits used for Wifi
    public final int[] THRESHOLD_SIGNAL_WIFI = { -61, -76 };
    public final String[] THRESHOLD_SIGNAL_WIFI_CAPTIONS = { "-61", "-76" };
    
    public static int classify(final int[] threshold, final long value)
    {
        final boolean inverse = threshold[0] < threshold[1];
        
        if (!inverse)
        {
            if (value >= threshold[0])
                return 3; // GREEN
            else if (value >= threshold[1])
                return 2; // YELLOW
            else
                return 1; // RED
        }
        else if (value <= threshold[0])
            return 3;
        else if (value <= threshold[1])
            return 2;
        else
            return 1;
    }

    
}
