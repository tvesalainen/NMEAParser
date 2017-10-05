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

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import javax.xml.bind.JAXBException;
import org.vesalainen.util.LoggingCommandLine;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CommandLine extends LoggingCommandLine
{

    public CommandLine()
    {
        addArgument(File.class, "configuration file");
        addOption("-f", "force port resolv", null, Boolean.FALSE);
        addOption("-rt", "resolv timeout", null, 2000L);
    }
    
    public static void main(String... args)
    {
        CommandLine cmdArgs = new CommandLine();
        cmdArgs.command(args);
        JavaLogging log = JavaLogging.getLogger(CommandLine.class);
        RouterConfig config = null;
        try
        {
            File configfile = (File) cmdArgs.getArgument("configuration file");
            if (!configfile.exists())
            {
                ConfigCreator configCreator
            }
            config = new RouterConfig(configfile);
        }
        catch (IOException | SecurityException | JAXBException ex)
        {
            ex.printStackTrace();
            return;
        }
        try
        {
            OldRouter router = new OldRouter(config);
            cmdArgs.attachInstant(router);
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
            log.log(Level.SEVERE, ex, "recovering...");
        }
    }
}
