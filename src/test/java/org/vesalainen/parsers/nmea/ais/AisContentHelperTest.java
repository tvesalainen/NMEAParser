/*
 * Copyright (C) 2014 Timo Vesalainen
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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen
 */
public class AisContentHelperTest
{
    
    public AisContentHelperTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
    }
    
    @AfterClass
    public static void tearDownClass()
    {
    }
    
    @Before
    public void setUp()
    {
    }
    
    @After
    public void tearDown()
    {
    }

    /**
     * Test of getAisData method, of class AisContentHelper.
     */
    @Test
    public void testGetAisData()
    {
        System.out.println("getAisData");
        String nmea = 
                "!AIVDM,4,1,7,A,16KDQR0000b0AtpDFwm6LTkF0L5PK01ue317mkPji7B46P0PMN1@i`0211H@:uCF,0*4C\r\n"+
                "!AIVDM,4,2,7,A,j4a3RS`dfS0nPFWE0enMwFGDdUBk05JAoH2Gc32G@RD<mK96`haN8B9i4Ca6HdId,0*3B\r\n"+
                "!AIVDM,4,3,7,A,E2;N1PG<JdhhRf>oNPhANBfRASdCjfI`3D3=UG2J`RQ9i1H<PS9bm<CKbREJVTPS,0*00\r\n"+
                "!AIVDM,4,4,7,A,rQA1@<aHJVkWD4ot2@,0*2E\r\n"
                ;
        String expResult = 
                "16KDQR0000b0AtpDFwm6LTkF0L5PK01ue317mkPji7B46P0PMN1@i`0211H@:uCF"+
                "j4a3RS`dfS0nPFWE0enMwFGDdUBk05JAoH2Gc32G@RD<mK96`haN8B9i4Ca6HdId"+
                "E2;N1PG<JdhhRf>oNPhANBfRASdCjfI`3D3=UG2J`RQ9i1H<PS9bm<CKbREJVTPS"+
                "rQA1@<aHJVkWD4ot2@\n"
                ;
        String result = AisContentHelper.getAisData(nmea);
        assertEquals(expResult, result);
    }

    /**
     * Test of dump method, of class AisContentHelper.
     */
    @Test
    public void testDump() throws Exception
    {
        System.out.println("dump");
        String nmea = "!AIVDM,1,1,,A,13HOI:0P0000VOHLCnHQKwvL05Ip,0*23";
        AisContentHelper.dump(nmea);
    }

    /**
     * Test of getUInt method, of class AisContentHelper.
     */
    @Test
    public void testGetInt()
    {
        System.out.println("getInt");
        int begin = 0;
        int end = 6;
        AisContentHelper instance = new AisContentHelper("!AIVDM,1,1,,A,13HOI:0P0000VOHLCnHQKwvL05Ip,0*23");
        int expResult = 1;
        int result = instance.getUInt(begin, end);
        assertEquals(expResult, result);
    }

}
