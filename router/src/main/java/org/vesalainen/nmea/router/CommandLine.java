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
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import org.vesalainen.util.CmdArgs;

/**
 *
 * @author tkv
 */
public class CommandLine extends CmdArgs
{

    public CommandLine()
    {
        addArgument(File.class, "configuration file");
        addOption("-lp", "log pattern", "filelog", "%t/router%g.log");
        addOption("-h", "host", "netlog", "localhost");
        addOption("-p", "port", "netlog", 0);
        addOption("-ll", "log level", null, INFO);
        addOption("-pl", "push level", null, SEVERE);
        addOption("-f", "force port resolv", null, Boolean.FALSE);
    }
    
}
