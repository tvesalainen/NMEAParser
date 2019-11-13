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
package org.vesalainen.parsers.nmea;

import java.io.IOException;
import java.time.LocalTime;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.math.UnitType;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class NMEASentenceTest
{
    NMEAParser parser = NMEAParser.newInstance();
    public NMEASentenceTest()
    {
    }

    @Test
    public void testRMC() throws IOException
    {
        SimpleStorage ss = new SimpleStorage();
        NMEAObserver tc = ss.getStorage(NMEAObserver.class);
        NMEASentence rmc = NMEASentence.rmc(12.3);
        parser.parse(rmc.toString(), tc, null);
        assertEquals(12.3, ss.getFloat("magneticVariation"), 1e-5);
    }
    @Test
    public void testDBT() throws IOException
    {
        SimpleStorage ss = new SimpleStorage();
        NMEAObserver tc = ss.getStorage(NMEAObserver.class);
        NMEASentence dbt = NMEASentence.dbt(15, UnitType.Meter);
        parser.parse(dbt.toString(), tc, null);
        assertEquals(15, ss.getFloat("depthBelowTransducer"), 1e-2);
    }    
    @Test
    public void testVHW() throws IOException
    {
        SimpleStorage ss = new SimpleStorage();
        NMEAObserver tc = ss.getStorage(NMEAObserver.class);
        NMEASentence vhw = NMEASentence.vhw(5.1, UnitType.Knot);
        parser.parse(vhw.toString(), tc, null);
        assertEquals(5.1, ss.getFloat("waterSpeed"), 1e-1);
    }
    @Test
    public void testMWV() throws IOException
    {
        SimpleStorage ss = new SimpleStorage();
        NMEAObserver tc = ss.getStorage(NMEAObserver.class);
        NMEASentence mwv = NMEASentence.mwv(15, 15, UnitType.MS, true);
        parser.parse(mwv.toString(), tc, null);
        assertEquals(15, ss.getFloat("windSpeed"), 1e-1);
        assertEquals(15, ss.getFloat("trueWindAngle"), 1e-1);
    }
    @Test
    public void testMTW() throws IOException
    {
        SimpleStorage ss = new SimpleStorage();
        NMEAObserver tc = ss.getStorage(NMEAObserver.class);
        NMEASentence mtw = NMEASentence.mtw(30, UnitType.Celsius);
        parser.parse(mtw.toString(), tc, null);
        assertEquals(30, ss.getFloat("waterTemperature"), 1e-1);
    }
    @Test
    public void testTLL() throws IOException
    {
        SimpleStorage ss = new SimpleStorage();
        NMEAObserver tc = ss.getStorage(NMEAObserver.class);
        LocalTime now = LocalTime.now();
        NMEASentence tll = NMEASentence.tll(66, 60.12, 25.45, "test", now, 'T', "ref");
        parser.parse(tll.toString(), tc, null);
        //targetNumber c destinationWaypointLocation c targetName c targetTime c targetStatus c referenceTarget"
        assertEquals(66, ss.getInt("targetNumber"));
        assertEquals(60.12, ss.getDouble("destinationWaypointLatitude"), 1e-10);
        assertEquals(25.45, ss.getDouble("destinationWaypointLongitude"), 1e-10);
        assertEquals("test", ss.getProperty("targetName"));
        assertEquals(now.getHour(), ss.getProperty("targetHour"));
        assertEquals(now.getMinute(), ss.getProperty("targetMinute"));
        assertEquals(now.getSecond(), ss.getFloat("targetSecond"), 1e-10);
        assertEquals('T', ss.getProperty("targetStatus"));
        assertEquals("ref", ss.getProperty("referenceTarget"));
    }
    @Test
    public void testTXT() throws IOException
    {
        SimpleStorage ss = new SimpleStorage();
        NMEAObserver tc = ss.getStorage(NMEAObserver.class);
        NMEASentence txt = NMEASentence.txt("ällöttää");
        parser.parse(txt.toString(), tc, null);
        assertEquals("ällöttää", ss.getProperty("message"));
    }
    @Test
    public void testPrefix1() throws IOException
    {
        String exp = "$PICOA,08,90,TXF,*1F\r\n";
        NMEASentence picoa = NMEASentence.builder("$PICOA", "08", "90", "TXF", "").build();
        assertEquals(exp, picoa.toString());
    }
    @Test
    public void testPrefix2() throws IOException
    {
        String exp = "$PICOA,08,90,TXF,*1F\r\n";
        NMEASentence picoa = NMEASentence.builder("$PICOA,08,90,TXF,").build();
        assertEquals(exp, picoa.toString());
    }
    @Test
    public void testConstructor() throws IOException
    {
        String exp = "$PICOA,08,90,TXF,*1F\r\n";
        NMEASentence picoa = new NMEASentence(exp);
        assertEquals(exp, picoa.toString());
    }
}
