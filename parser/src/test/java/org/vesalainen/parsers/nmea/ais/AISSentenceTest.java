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
import java.time.LocalTime;
import static java.time.ZoneOffset.UTC;
import java.time.ZonedDateTime;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.vesalainen.parsers.mmsi.MMSIParser;
import org.vesalainen.parsers.nmea.NMEAParser;
import org.vesalainen.parsers.nmea.NMEASentence;
import org.vesalainen.parsers.nmea.ais.areanotice.CircleArea;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AISSentenceTest
{

    private NMEAParser parser;
    
    public AISSentenceTest()
    {
    }

    @Before
    public void setUp()
    {
        parser = NMEAParser.newInstance();
    }

    @Test
    public void testAreaNotice_Circle() throws IOException
    {
        testAreaNotice("!AIVDM,1,1,0,A,85M:Ih1KUQU6jAs85`0MK4lh<7=B42l0000,2*7F\r\n");
        testAreaNotice("!AIVDM,1,1,0,A,85M:Ih1KUQU6jAs85`0MKFaH;k4>42l0000,2*0E\r\n");
    }
    @Test
    public void testAreaNotice_Rectangle() throws IOException
    {
        testAreaNotice("!AIVDM,1,1,0,A,85M:Ih1KUQVhjAs80e1MJCPP;uR91@:2`00,2*73\r\n");
        testAreaNotice("!AIVDM,1,1,0,A,85M:Ih1KUQVhjAs80e1MKJCh;iDq1@:2`00,2*12\r\n");
    }
    @Test
    public void testAreaNotice_Sector() throws IOException
    {
        testAreaNotice("!AIVDM,1,1,0,A,85M:Ih1KUQW5BAs80e2eKiP8;hoV06BgL80,2*5E\r\n");
    }
    @Test
    public void testAreaNotice_PolylinePlusText() throws IOException
    {
        testAreaNotice("!AIVDM,2,1,0,A,85M:Ih1KUQ`tBAs85`0=KshH;iLe4000031JvP=uo0`GVBo8C0OA<000000000,0*0F\r\n!AIVDM,2,2,0,A,05D5CDP<9>5Pi0000,2*00\r\n");
    }
    @Test
    public void testAreaNotice_Polygon() throws IOException
    {
        testAreaNotice("!AIVDM,1,1,0,A,85M:Ih1KUQa8jAs85`0=Ki@P;k:54000040tUPUTd000000000,4*2F\r\n");
    }
    //@Test // this is from opencpn and broken????
    public void testCapeCod() throws IOException
    {
        testAreaNotice("!AIVDM,2,1,1,A,81mg=5@0EP:0FT;H02PN04ha=3Ul>N000`IrAhaQT2B,0*11\r\n!AIVDM,2,2,1,A,qQl40,3*5C\r\n");
        testAreaNotice("!AIVDM,2,1,2,A,81mg=5@0EP90FT;H02PMwvii=<=t>N000`IrAhaQT2B,0*61\r\n!AIVDM,2,2,2,A,qQl40,3*5F\r\n");
        testAreaNotice("!AIVDM,2,1,3,A,81mg=5@0EP80FT;H02PMwpkq=DsD>N000`IrAhaQT2B,0*7B\r\n!AIVDM,2,2,3,A,qQl40,3*5E\r\n");
        testAreaNotice("!AIVDM,2,1,4,A,81mg=5@0EP70FT;H02PMwiEQ=MI4>N000`IrAhaQT2B,0*27\r\n!AIVDM,2,2,4,A,qQl40,3*59\r\n");
        testAreaNotice("!AIVDM,2,1,5,A,81mg=5@0EP60FT;H02PMwW1Q=NGd>N000`IrAhaQT2B,0*30\r\n!AIVDM,2,2,5,A,qQl40,3*58\r\n");
        testAreaNotice("!AIVDM,2,1,6,A,81mg=5@0EP50FT;H02PMwIoi=OAD>N000`IrAhaQT2B,0*6F\r\n!AIVDM,2,2,6,A,qQl40,3*5B\r\n");
        testAreaNotice("!AIVDM,2,1,7,A,81mg=5@0EP40FT;H02PMwN000`IrAhaQT2B,0*5E\r\n!AIVDM,2,2,7,A,qQl40,3*5A\r\n");
        testAreaNotice("!AIVDM,2,1,8,A,81mg=5@0EP30FT;H02PMvwd9=Prl>N000`IrAhaQT2B,0*07\r\n!AIVDM,2,2,8,A,qQl40,3*55\r\n");
        testAreaNotice("!AIVDM,2,1,9,A,81mg=5@0EP20FT;H02PMvjRq=Qg<>N000`IrAhaQT2B,0*20\r\n!AIVDM,2,2,9,A,qQl40,3*54\r\n");
        testAreaNotice("!AIVDM,2,1,1,A,81mg=5@0EP10FT;H02PMvUO1=RTd>N000`IrAhaQT2B,0*21\r\n!AIVDM,2,2,1,A,qQl40,3*5C\r\n");
        }
    public void testAreaNotice(String msg) throws IOException
    {
        AISContentHelper ach = new AISContentHelper(msg);
        int contentLength = ach.getContentLength();
        String dump = ach.dumpLen(6, 2, 30, 2, 10, 6, 10, 7, 4, 5, 5, 6, 18, 3, 2, 28, 27, 3, 12, 15, 7);
        System.err.println(dump);
        TC tc = new TC();
        parser.parse(msg, null, tc);
        assertNull(tc.rollbackReason);
        ZonedDateTime date = ZonedDateTime.of(0, tc.month, tc.day, tc.hour, tc.minute, 0, 0, UTC);
        NMEASentence[] msgs = AISSentence.getMsg8(tc.mmsi, tc.dac, tc.linkage, tc.notice, date, tc.duration, tc.area);
        StringBuilder sb = new StringBuilder();
        for (NMEASentence m : msgs)
        {
            sb.append(m);
        }
        AISContentHelper ach2 = new AISContentHelper(sb.toString());
        assertEquals(ach, ach2);
    }
    
}
