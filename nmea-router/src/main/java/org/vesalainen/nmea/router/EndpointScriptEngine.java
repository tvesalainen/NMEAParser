/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import org.vesalainen.nio.RingByteBuffer;
import org.vesalainen.nmea.router.OldRouter.Endpoint;
import org.vesalainen.nmea.script.ScriptParser;
import org.vesalainen.nmea.script.ScriptStatement;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class EndpointScriptEngine extends JavaLogging implements Runnable
{
    private final OldRouter router;
    private final ThreadGroup threadGroup;
    private final Endpoint endpoint;
    private Thread thread;
    private final String scriptName;
    private final List<ScriptStatement> script;
    private ReentrantLock lock = new ReentrantLock();
    private Semaphore semaphore;
    private String waitMsg;

    public EndpointScriptEngine(OldRouter router, ThreadGroup threadGroup, Endpoint endpoint, String script)
    {
        setLogger(this.getClass(), endpoint.name);
        this.router = router;
        this.threadGroup = threadGroup;
        this.endpoint = endpoint;
        EndpointScriptObjectFactory factory = new EndpointScriptObjectFactory(router, endpoint);
        ScriptParser<EndpointScriptEngine> engine = ScriptParser.newInstance();
        this.script = engine.exec(script, factory);
        scriptName = endpoint.name+".script";
    }

    public void start()
    {
        if (thread != null)
        {
            throw new IllegalStateException("already started");
        }
        thread = new Thread(threadGroup, this, scriptName);
        thread.start();
        info("%s started", scriptName);
    }
    public void stop()
    {
        if (thread != null)
        {
            thread.interrupt();
        }
    }
    public boolean startWait(long millis, String msg) throws InterruptedException
    {
        waitMsg = msg;
        semaphore = new Semaphore(0);
        boolean result = semaphore.tryAcquire(millis, TimeUnit.MILLISECONDS);
        lock.lock();
        try
        {
            waitMsg = null;
            semaphore = null;
            return result;
        }
        finally
        {
            lock.unlock();
        }
    }
    public void write(RingByteBuffer ring)
    {
        lock.lock();
        try
        {
            if (semaphore != null)
            {
                int length = waitMsg.length();
                if (ring.length() >= length)
                {
                    for (int ii=0;ii<length;ii++)
                    {
                        if (ring.charAt(ii) != waitMsg.charAt(ii))
                        {
                            return;
                        }
                    }
                    semaphore.release();
                }
            }
        }
        finally
        {
            lock.unlock();
        }
    }
    
    @Override
    public void run()
    {
        try
        {
            info("script %s started", scriptName);
            for (ScriptStatement statement : script)
            {
                config("exec: %s", statement);
                statement.exec(this);
            }
            config("script %s ended", scriptName);
        }
        catch (InterruptedException ex)
        {
            config("script %s interrupted", scriptName);
        }
        catch (IOException ex)
        {
            log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

}
