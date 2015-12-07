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

import java.io.File;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;
import java.util.logging.MemoryHandler;
import java.util.logging.SocketHandler;
import javax.xml.bind.JAXBException;
import org.vesalainen.util.CmdArgs;
import org.vesalainen.util.CmdArgsException;
import org.vesalainen.util.logging.MinimalFormatter;

/**
 *
 * @author tkv
 */
public class CommandLine extends CmdArgs<Router>
{

    public CommandLine()
    {
        addArgument(File.class, "configuration file");
        addOption("-lp", "log pattern", "filelog", "%t/router%g.log");
        addOption("-l", "log limit", "filelog", 409600);
        addOption("-c", "log count", "filelog", 16);
        addOption("-h", "host", "netlog", "localhost");
        addOption("-p", "port", "netlog", 0);
        addOption("-ll", "log level", null, INFO);
        addOption("-pl", "push level", null, SEVERE);
        addOption("-f", "force port resolv", null, Boolean.FALSE);
        addOption("-rt", "resolv timeout", null, 2000L);
    }
    
    public static void main(String... args)
    {
        CommandLine cmdArgs = new CommandLine();
        try
        {
            cmdArgs.setArgs(args);
        }
        catch (CmdArgsException ex)
        {
            Logger logger = Logger.getLogger(Router.class.getName());
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            logger.log(Level.SEVERE, ex.usage());
            System.exit(-1);
        }
        Logger log = Logger.getLogger("org.vesalainen");
        log.setUseParentHandlers(false);
        log.setLevel((Level) cmdArgs.getOption("-ll"));
        Handler handler = null;
        RouterConfig config = null;
        try
        {
            String effectiveGroup = cmdArgs.getEffectiveGroup();
            if (effectiveGroup == null)
            {
                handler = new ConsoleHandler();
            }
            else
            {
                switch (effectiveGroup)
                {
                case "filelog":
                    
                    handler = new FileHandler((String) cmdArgs.getOption("-lp"), (int) cmdArgs.getOption("-l"), (int) cmdArgs.getOption("-c"), true);
                    break;
                case "netlog":
                    handler = new SocketHandler((String) cmdArgs.getOption("-h"), (int) cmdArgs.getOption("p"));
                    break;
                }
            }
            handler.setLevel((Level) cmdArgs.getOption("-ll"));
            File configfile = (File) cmdArgs.getArgument("configuration file");
            config = new RouterConfig(configfile);
        }
        catch (IOException | SecurityException | JAXBException ex)
        {
            ex.printStackTrace();
            return;
        }
        MinimalFormatter minimalFormatter = new MinimalFormatter();
        handler.setFormatter(minimalFormatter);
        MemoryHandler memoryHandler = new MemoryHandler(handler, 256, (Level) cmdArgs.getOption("-pl"));
        memoryHandler.setFormatter(minimalFormatter);
        log.addHandler(memoryHandler);
        try
        {
            Router router = new Router(config, log, cmdArgs);
            cmdArgs.attach(router);
            router.loop();
        }
        catch (RestartException ex)
        {
            log.info("restarted by "+ex.getMessage());
        }
        catch (ShutdownException ex)
        {
            log.info("shutdown by "+ex.getMessage());
        }
        catch (Throwable ex)
        {
            log.log(Level.SEVERE, "recovering...", ex);
        }
    }
}
