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
import java.nio.file.Path;
import java.time.Clock;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.xml.bind.JAXBException;
import org.vesalainen.parsers.nmea.NMEAService;
import org.vesalainen.util.LoggingCommandLine;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CommandLine extends LoggingCommandLine
{

    public CommandLine()
    {
        addArgument(Path.class, "configuration file");
    }
    
    private void start() throws IOException, InterruptedException, JAXBException
    {
        config("starting NMEA Server");
        Path configfile = getArgument("configuration file");
        Config config = new Config(configfile);
        String address = config.getNmeaMulticastAddress();
        int nmeaPort = config.getNmeaMulticastPort();
        int httpPort = config.getHttpPort();
        config("NMEA Multicast Address=%s", address);
        config("NMEA Multicast Port=%s", nmeaPort);
        config("HTTP Port=%s", httpPort);
        CachedScheduledThreadPool executor = new CachedScheduledThreadPool(64);
        config("ThreadPool started %s", executor);
        NMEAService nmeaService = new NMEAService(address, nmeaPort, executor);
        PropertyServer propertyServer = new PropertyServer(Clock.systemDefaultZone(), config);
        nmeaService.addNMEAObserver(propertyServer);
        nmeaService.start();
        config("NMEA Service started");
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
