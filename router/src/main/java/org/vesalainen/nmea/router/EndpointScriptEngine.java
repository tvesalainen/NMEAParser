/*
 * Copyright (C) 2015 tkv
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.vesalainen.nmea.router;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vesalainen.nmea.router.Router.Endpoint;
import org.vesalainen.nmea.script.ScriptEngine;

/**
 *
 * @author tkv
 */
public class EndpointScriptEngine implements Runnable
{
    private final Router router;
    private final Endpoint endpoint;
    private final String script;
    private final EndpointScriptObjectFactory factory;

    public EndpointScriptEngine(Router router, Endpoint endpoint, String script)
    {
        this.router = router;
        this.endpoint = endpoint;
        this.script = script;
        this.factory = new EndpointScriptObjectFactory(router, endpoint);
    }

    @Override
    public void run()
    {
        try
        {
            ScriptEngine<Boolean> engine = ScriptEngine.newInstance();
            engine.exec(script, factory);
        }
        catch (IOException ex)
        {
            Logger.getLogger(EndpointScriptEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
