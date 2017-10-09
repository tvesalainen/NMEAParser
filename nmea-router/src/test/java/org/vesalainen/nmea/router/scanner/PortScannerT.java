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
package org.vesalainen.nmea.router.scanner;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.comm.channel.SerialChannel;
import org.vesalainen.nmea.jaxb.router.SerialType;
import static org.vesalainen.nmea.router.PortType.*;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PortScannerT
{
    
    public PortScannerT()
    {
        JavaLogging.setConsoleHandler("org.vesalainen", Level.FINE);
    }

    @Test
    public void testScan() throws IOException
    {
        PortScanner portScanner = new PortScanner()
                .setChannelSuppliers(EnumSet.of(NMEA, NMEA_HS, SEA_TALK))
                .setPorts(SerialChannel.getFreePorts());
        Map<String,SerialType> uniqueMap = new HashMap<>();
        uniqueMap.put("$GPRMC", null);
        portScanner.scan((s)->System.err.println(s), uniqueMap);
        portScanner.waitScanner();
    }
    
}
