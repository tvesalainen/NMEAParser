/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.parsers.nmea.ais;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.Level;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.parsers.nmea.NMEAService;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AISServiceT
{
    
    public AISServiceT()
    {
        JavaLogging.setConsoleHandler("org.vesalainen", Level.CONFIG);
    }

    @Test
    public void test1() throws IOException, InterruptedException
    {
        NMEAService nmeaService = new NMEAService("224.0.0.3", 10110);
        AISService aisService = new AISService(Paths.get("c:\\temp"), 10);
        aisService.attach(nmeaService);
        nmeaService.start();
        Thread.sleep(100000000);
    }
    
}
