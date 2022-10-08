/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nmea.server;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.time.Clock;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.servlet.ServletContext;
import javax.xml.bind.JAXBException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.session.DefaultSessionIdManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.log.JavaUtilLog;
import org.eclipse.jetty.util.log.Log;
import org.vesalainen.nmea.server.anchor.AnchorManager;
import org.vesalainen.parsers.nmea.NMEAService;
import org.vesalainen.parsers.nmea.ais.AISService;
import org.vesalainen.util.LoggingCommandLine;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CommandLine extends LoggingCommandLine
{

    static
    {
        JavaUtilLog log = new JavaUtilLog();
        Log.setLog(log);    // make jetty use java.util.logger
    }
    
    public CommandLine()
    {
        addArgument(Path.class, "configuration file");
    }
    
    private void start() throws IOException, InterruptedException, JAXBException, Exception
    {
        config("starting NMEA Server");
        Path configfile = getArgument("configuration file");
        Config config = new Config(configfile);
        String address = config.getNmeaMulticastAddress();
        int nmeaPort = config.getNmeaMulticastPort();
        int httpPort = config.getHttpPort();
        URI aisDirectory = config.getAisDirectory();
        long aisMaxLogSize = config.getAisMaxLogSize();
        long aisTtl = config.getAisTtl();
        config("NMEA Multicast Address=%s", address);
        config("NMEA Multicast Port=%s", nmeaPort);
        config("HTTP Port=%s", httpPort);
        config("AIS Directory =%s", aisDirectory);
        config("AIS Max Log Size =%d", aisMaxLogSize);
        config("AIS TTL =%d ms", aisTtl);
        CachedScheduledThreadPool executor = new CachedScheduledThreadPool(64);
        config("ThreadPool started %s", executor);
        NMEAService nmeaService = new NMEAService(address, nmeaPort, executor);
        AISService aisService = AISService.getInstance(nmeaService, aisDirectory, aisTtl, aisMaxLogSize, executor);
        PropertyServer propertyServer = new PropertyServer(Clock.systemDefaultZone(), config, executor);
        aisService.addObserver(propertyServer);
        nmeaService.addNMEAObserver(propertyServer);
        AnchorManager anchorManager = AnchorManager.getInstance(propertyServer, config.getBoat(), executor);
        nmeaService.addNMEAObserver(anchorManager);
        nmeaService.start();
        config("NMEA Service started");
        
        Server server = new Server(httpPort);
        HandlerList handlers = new HandlerList();
        ServletContextHandler context = new ServletContextHandler();

        ServletHolder defautlHolder = new ServletHolder(DefaultServlet.class);
        defautlHolder.setInitParameter("resourceBase", aisDirectory.toString());
        context.addServlet(defautlHolder, "*.dat");

        ServletHolder sseHolder = new ServletHolder(SseServlet.class);
        sseHolder.setAsyncSupported(true);
        
        context.addServlet(sseHolder, "/sse");
        context.addServlet(ResourceServlet.class, "/");
        context.addServlet(ResourceServlet.class, "*.html");
        context.addServlet(ResourceServlet.class, "*.js");
        context.addServlet(ResourceServlet.class, "*.css");
        context.addServlet(ResourceServlet.class, "*.gif");
        context.addServlet(ResourceServlet.class, "*.png");
        context.addServlet(ResourceServlet.class, "*.ico");
        context.addServlet(PrefsServlet.class, "/prefs");
        context.addServlet(I18nServlet.class, "/i18n");
        
        SessionHandler sessionHandler = new SessionHandler();
        context.setSessionHandler(sessionHandler);
        handlers.addHandler(context);
        server.setHandler(handlers);
        server.setSessionIdManager(new DefaultSessionIdManager(server));
        ServletContext servletContext = context.getServletContext().getContext("/sse");
        servletContext.setAttribute(PropertyServer.class.getName(), propertyServer);
        server.start();
        
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }
    public static void main(String... args)
    {
        CommandLine cmdArgs = new CommandLine();
        cmdArgs.command(args);
        JavaLogging log = JavaLogging.getLogger(CommandLine.class);
        try
        {
            cmdArgs.start();
        }
        catch (Throwable ex)
        {
            ex.printStackTrace();
            log.log(Level.SEVERE, ex, "command-line %s", ex.getMessage());
            System.exit(2);
        }
    }
}
