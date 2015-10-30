/*******************************************************************************
 * Copyright 2013-2014 alladin-IT GmbH
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
package at.alladin.rmbt.statisticServer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import at.alladin.rmbt.db.DbConnection;
import at.alladin.rmbt.shared.Classification;

public class ContextListener implements ServletContextListener
{
    
    @Override
    public void contextInitialized(ServletContextEvent sce)
    {
        try
        {
            Classification.initInstance(DbConnection.getConnection());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent arg0)
    {
    }
}
