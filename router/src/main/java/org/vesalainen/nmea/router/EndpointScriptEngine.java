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

import java.util.List;
import java.util.logging.Level;
import org.vesalainen.nio.RingByteBuffer;
import org.vesalainen.nmea.router.Router.Endpoint;
import org.vesalainen.nmea.script.ScriptParser;
import org.vesalainen.nmea.script.ScriptStatement;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author tkv
 */
public class EndpointScriptEngine extends JavaLogging implements Runnable
{
    private final Router router;
    private final Endpoint endpoint;
    private Thread thread;
    private final String scriptName;
    private final List<ScriptStatement<Boolean>> script;

    public EndpointScriptEngine(Router router, Endpoint endpoint, String script)
    {
        setLogger(this.getClass(), endpoint.name);
        this.router = router;
        this.endpoint = endpoint;
        EndpointScriptObjectFactory factory = new EndpointScriptObjectFactory(router, endpoint);
        ScriptParser<Boolean> engine = ScriptParser.newInstance();
        this.script = engine.exec(script, factory);
        scriptName = endpoint.name+".script";
    }

    public void start()
    {
        if (thread != null)
        {
            throw new IllegalStateException("already started");
        }
        thread = new Thread(this, scriptName);
        thread.start();
        info("%s started", scriptName);
    }
    public void stop()
    {
        if (thread == null)
        {
            throw new IllegalStateException(scriptName+" not started");
        }
        thread.interrupt();
    }
    public void read(RingByteBuffer ring)
    {
        
    }
    @Override
    public void run()
    {
        try
        {
            info("script %s started", scriptName);
            for (ScriptStatement<Boolean> statement : script)
            {
                fine("exec: %s", statement);
                statement.exec();
            }
            info("script %s ended", scriptName);
        }
        catch (InterruptedException ex)
        {
            info("script %s interrupted", scriptName);
        }
        catch (Exception ex)
        {
            log(Level.SEVERE, null, ex);
        }
    }
    
}
