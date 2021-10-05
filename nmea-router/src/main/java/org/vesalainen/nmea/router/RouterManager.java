/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.lang.management.ManagementFactory;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import static java.util.logging.Level.SEVERE;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.xml.bind.JAXBException;
import org.vesalainen.nmea.router.endpoint.Endpoint;
import org.vesalainen.parsers.nmea.NMEASentence;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RouterManager extends JavaLogging implements RouterManagerMXBean, Runnable
{
    public static CachedScheduledThreadPool POOL = new CachedScheduledThreadPool();
    private final ObjectName objectName;
    private CommandLine cmdArgs;
    private RouterConfig config;

    public RouterManager()
    {
        super(RouterManager.class);
        try
        {
            this.objectName = new ObjectName("org.vesalainen.nmea.router:type=manager");
            ManagementFactory.getPlatformMBeanServer().registerMBean(this, objectName);
        }
        catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    void start(CommandLine cmdArgs, RouterConfig config)
    {
        this.cmdArgs = cmdArgs;
        this.config = config;
        cmdArgs.attachInstant(this);
        config("initial start");
        POOL.schedule(()->start(), 0, TimeUnit.SECONDS);
        try
        {
            while (POOL != null)
            {
                POOL.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            }
        }
        catch (InterruptedException ex)
        {
            log(SEVERE, ex, "main interrupted %s", ex.getMessage());
        }
    }
    private void start()
    {
        try
        {
            Router router = new Router(config);
            cmdArgs.attachInstant(router);
            if (router.start())
            {
                restart("port config changed");
            }
        }
        catch (Throwable ex)
        {
            log(Level.SEVERE, ex, "stopped...");
        }
    }
    
    @Override
    public void restart(String reason) throws IOException
    {
        severe("restarting because %s", reason);
        ScheduledExecutorService oldPool = POOL;
        POOL = new CachedScheduledThreadPool();
        config("created new thread pool");
        POOL.schedule(()->start(), 10, TimeUnit.SECONDS);
        config("scheduled router start");
        try
        {
            config.load();
        }
        catch (JAXBException ex)
        {
            throw new IOException(ex);
        }
        cmdArgs.attachInstant(this);
        oldPool.shutdownNow();
    }

    @Override
    public void send(String msg, String to)
    {
        Endpoint endpoint = Endpoint.get(to);
        if (endpoint != null)
        {
            NMEASentence nmea = NMEASentence.builder(msg).build();
            try
            {
                endpoint.write(nmea.getByteBuffer());
            }
            catch (IOException ex)
            {
                log(DEBUG, ex, "send(%s, %s) %s", msg, to, ex.getMessage());
            }
        }
        else
        {
            warning("send(%s, %s) target not found", msg, to);
        }
    }
    
    @Override
    public void run()
    {
        try
        {
            config("started shutdown-hook");
            ScheduledExecutorService oldPool = POOL;
            POOL = null;
            oldPool.shutdownNow();
            config.store();
            config("stored configuration");
        }
        catch (IOException ex)
        {
            log(SEVERE, ex, "shutting down");
        }
    }

}
