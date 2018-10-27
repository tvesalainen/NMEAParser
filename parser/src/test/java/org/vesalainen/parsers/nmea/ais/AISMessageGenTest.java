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
import static org.vesalainen.parsers.nmea.ais.EPFDFixTypes.GPS;
import static org.vesalainen.parsers.nmea.ais.ManeuverIndicator.NoSpecialManeuver;
import static org.vesalainen.parsers.nmea.ais.NavigationStatus.UnderWaySailing;

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
    public void testMsg1() throws IOException
    {
        NMEASentence[] msg1 = AISMessageGen.msg1(cache.getEntry(230123250));
        NMEAParser parser = NMEAParser.newInstance();
        TC tc = new TC();
        parser.parse(msg1[0].toString(), null, tc);
        assertEquals(230123250, tc.mmsi);
        assertEquals(UnderWaySailing, tc.navigationStatus);
        assertEquals(-2, tc.rateOfTurn, 1);
        assertEquals(4.6, tc.speed, 1e-3);
        assertEquals(true, tc.accuracy);
        assertEquals(-124.707214, tc.longitude, 1e-5);
        assertEquals(-6.16368, tc.latitude, 1e-5);
        assertEquals(252.1, tc.course, 1e-2);
        assertEquals(250, tc.heading);
        assertEquals(23, tc.second);
        assertEquals(NoSpecialManeuver, tc.maneuver);
        assertEquals(true, tc.raim);
        assertEquals(393222, tc.radio);
    }
    @Test
    public void testMsg5() throws IOException
    {
        NMEASentence[] msg5 = AISMessageGen.msg5(cache.getEntry(230123250));
        NMEAParser parser = NMEAParser.newInstance();
        TC tc = new TC();
        parser.parse(msg5[0].toString()+msg5[1].toString(), null, tc);
        assertEquals(230123250, tc.mmsi);
        assertEquals(0, tc.aisVersion);
        assertEquals(123456789, tc.imoNumber);
        assertEquals("OJ3231", tc.callSign);
        assertEquals("IIRIS", tc.shipname);
        assertEquals(Sailing, tc.shipType);
        assertEquals(12, tc.dimensionToBow);
        assertEquals(0, tc.dimensionToStern);
        assertEquals(4, tc.dimensionToPort);
        assertEquals(0, tc.dimensionToStarboard);
        assertEquals(GPS, tc.epfd);
        assertEquals(11, tc.eta_month);
        assertEquals(4, tc.eta_day);
        assertEquals(11, tc.eta_hour);
        assertEquals(12, tc.eta_minute);
        assertEquals(1.7, tc.draught, 1e-3);
        assertEquals("NUKU HIVA", tc.destination);
        assertEquals(false, tc.dte);
    }
    @Test
    public void testMsg18() throws IOException
    {
        NMEASentence[] msg18 = AISMessageGen.msg18(cache.getEntry(230123250));
        NMEAParser parser = NMEAParser.newInstance();
        TC tc = new TC();
        parser.parse(msg18[0].toString(), null, tc);
        assertEquals(230123250, tc.mmsi);
        assertEquals(4.6, tc.speed, 1e-3);
        assertEquals(true, tc.accuracy);
        assertEquals(-124.707214, tc.longitude, 1e-5);
        assertEquals(-6.16368, tc.latitude, 1e-5);
        assertEquals(252.1, tc.course, 1e-2);
        assertEquals(250, tc.heading);
        assertEquals(23, tc.second);
        assertEquals(true, tc.cs);
        assertEquals(false, tc.display);
        assertEquals(true, tc.dsc);
        assertEquals(true, tc.band);
        assertEquals(true, tc.msg22);
        assertEquals(false, tc.assigned);
        assertEquals(true, tc.raim);
        assertEquals(393222, tc.radio);
    }
    @Test
    public void testMsg24A() throws IOException
    {
        NMEASentence[] msg24A = AISMessageGen.msg24A(cache.getEntry(230123250));
        NMEAParser parser = NMEAParser.newInstance();
        TC tc = new TC();
        parser.parse(msg24A[0].toString(), null, tc);
        assertEquals(230123250, tc.mmsi);
        assertEquals("IIRIS", tc.shipname);
        
    }
    @Test
    public void testMsg24B() throws IOException
    {
        NMEASentence[] msg24B = AISMessageGen.msg24B(cache.getEntry(230123250));
        NMEAParser parser = NMEAParser.newInstance();
        TC tc = new TC();
        parser.parse(msg24B[0].toString(), null, tc);
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
