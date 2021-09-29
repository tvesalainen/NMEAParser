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
package org.vesalainen.nmea.processor;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class N2KGatewayT
{
    
    public N2KGatewayT()
    {
        JavaLogging.setConsoleHandler("org.vesalainen", Level.INFO);
    }

    @Test
    public void test() throws IOException, InterruptedException, ExecutionException
    {
        Path in = Paths.get("C:\\Users\\tkv\\share", "candump.txt");
        Path out = Paths.get("C:\\Temp", "can2nmea.txt");
        N2KGateway gw = N2KGateway.getInstance("can1", in, out, Executors.newCachedThreadPool(), "RMC", "DBT", "HDT", "MTW", "MWV", "VHW");
        gw.startAndWait();
    }
    
}
