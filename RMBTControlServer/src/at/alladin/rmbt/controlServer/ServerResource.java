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
package at.alladin.rmbt.controlServer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.naming.NamingException;

import org.restlet.data.Reference;
import org.restlet.engine.header.Header;
import org.restlet.representation.Representation;
import org.restlet.resource.Options;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

import at.alladin.rmbt.db.DbConnection;
import at.alladin.rmbt.shared.Classification;
import at.alladin.rmbt.shared.ResourceManager;
import at.alladin.rmbt.shared.SettingsHelper;
import at.alladin.rmbt.shared.Settings;

public class ServerResource extends org.restlet.resource.ServerResource implements Settings
{
    protected Connection conn;
    protected ResourceBundle labels;
    protected ResourceBundle settings;
    protected Classification classification;
    
    @Override
    public void doInit() throws ResourceException
    {
        super.doInit();
        
        classification = Classification.getInstance();
        
        settings = ResourceManager.getCfgBundle();
        // Set default Language for System
        Locale.setDefault(new Locale(settings.getString("RMBT_DEFAULT_LANGUAGE")));
        labels = ResourceManager.getSysMsgBundle();
        
        // Get DB-Connection
        try
        {
            conn = DbConnection.getConnection();
        }
        catch (final NamingException e)
        {
            e.printStackTrace();
        }
        catch (final SQLException e)
        {
            System.out.println(labels.getString("ERROR_DB_CONNECTION_FAILED"));
            e.printStackTrace();
        }
    }
    
    @Override
    protected void doRelease() throws ResourceException
    {
        try
        {
            if (conn != null)
                conn.close();
        }
        catch (final SQLException e)
        {
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("unchecked")
    protected void addAllowOrigin()
    {
        Series<Header> responseHeaders = (Series<Header>) getResponse().getAttributes().get("org.restlet.http.headers");
        if (responseHeaders == null)
        {
            responseHeaders = new Series<>(Header.class);
            getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
        }
        responseHeaders.add("Access-Control-Allow-Origin", "*");
        responseHeaders.add("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
        responseHeaders.add("Access-Control-Allow-Headers", "Content-Type");
        responseHeaders.add("Access-Control-Allow-Credentials", "false");
        responseHeaders.add("Access-Control-Max-Age", "60");
    }

    @Options
    public void doOptions(final Representation entity)
    {
        addAllowOrigin();
    }
    
    @SuppressWarnings("unchecked")
    public String getIP()
    {
        final Series<Header> headers = (Series<Header>) getRequest().getAttributes().get("org.restlet.http.headers");
        final String realIp = headers.getFirstValue("X-Real-IP", true);
        if (realIp != null)
            return realIp;
        else
            return getRequest().getClientInfo().getAddress();
    }
    
    @SuppressWarnings("unchecked")
    public Reference getURL()
    {
        final Series<Header> headers = (Series<Header>) getRequest().getAttributes().get("org.restlet.http.headers");
        final String realURL = headers.getFirstValue("X-Real-URL", true);
        if (realURL != null)
            return new Reference(realURL);
        else
            return getRequest().getOriginalRef();
    }
    
    @Override
    public String getSetting(String key)
    {
        return getSetting(key, null);
    }
    
    // TODO: add caching!
    @Override
    public String getSetting(String key, String lang)
    {
        return SettingsHelper.getSetting(conn, key, lang);
    }
}
