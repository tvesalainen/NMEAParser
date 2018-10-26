/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.io.InputStream;
import java.time.Clock;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.parsers.nmea.NMEAParser;
import org.vesalainen.parsers.nmea.NMEASentence;
import static org.vesalainen.parsers.nmea.ais.CodesForShipType.Sailing;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AISMessageGenTest
{
    Properties properties = new Properties();
    AISCache cache = new AISCache(Clock.systemUTC(), 100, TimeUnit.DAYS, (m)->new Properties());

    public AISMessageGenTest() throws IOException
    {
        InputStream is = AISMessageGenTest.class.getResourceAsStream("/230123250.dat");
        properties.load(is);
        cache.update(properties);
    }

    @Test
    public void testMsg24A() throws IOException
    {
        NMEASentence msg24A = AISMessageGen.msg24A(cache.getEntry(230123250));
        NMEAParser parser = NMEAParser.newInstance();
        TC tc = new TC();
        parser.parse(msg24A.toString(), null, tc);
        assertEquals(230123250, tc.mmsi);
        assertEquals("IIRIS", tc.shipname);
        
    }
    @Test
    public void testMsg24B() throws IOException
    {
        NMEASentence msg24B = AISMessageGen.msg24B(cache.getEntry(230123250));
        NMEAParser parser = NMEAParser.newInstance();
        TC tc = new TC();
        parser.parse(msg24B.toString(), null, tc);
        assertEquals(230123250, tc.mmsi);
        assertEquals(Sailing, tc.shipType);
        assertEquals("NVC", tc.vendorid);
        assertEquals(1, tc.model);
        assertEquals(35090, tc.serial);
        assertEquals("OJ3231", tc.callSign);
        assertEquals(12, tc.dimensionToBow);
        assertEquals(0, tc.dimensionToStern);
        assertEquals(4, tc.dimensionToPort);
        assertEquals(0, tc.dimensionToStarboard);
    }
    
}
