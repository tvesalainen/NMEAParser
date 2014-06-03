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

package org.vesalainen.parsers.mmsi;

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
public class MMSIParserTest
{
    
    public MMSIParserTest()
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

    @Test
    public void testParseSarAircraft()
    {
        System.out.println("parse");
        int mmsi = 111232506;
        MMSIParser instance = MMSIParser.getInstance();
        MMSIType expResult = MMSIType.SarAircraft;
        MMSIEntry result = instance.parse(mmsi);
        assertEquals(expResult, result.getType());
        MIDEntry mid = result.getMid();
        assertEquals("United Kingdom", mid.getCountry());
        assertEquals("111232506", result.getString());
    }

    /**
     * Test of parse method, of class MMSIParser.
     */
    @Test
    public void testParseShip()
    {
        System.out.println("parse");
        int mmsi = 230123250;
        MMSIParser instance = MMSIParser.getInstance();
        MMSIType expResult = MMSIType.ShipStation;
        MMSIEntry result = instance.parse(mmsi);
        assertEquals(expResult, result.getType());
        MIDEntry mid = result.getMid();
        assertEquals("Finland", mid.getCountry());
        assertEquals("230123250", result.getString());
    }

    @Test
    public void testParseGroup()
    {
        System.out.println("parse");
        int mmsi = 23012325;
        MMSIParser instance = MMSIParser.getInstance();
        MMSIType expResult = MMSIType.GroupShipStation;
        MMSIEntry result = instance.parse(mmsi);
        assertEquals(expResult, result.getType());
        MIDEntry mid = result.getMid();
        assertEquals("Finland", mid.getCountry());
        assertEquals("023012325", result.getString());
    }

    @Test
    public void testParseCoast()
    {
        System.out.println("parse");
        int mmsi = 2301232;
        MMSIParser instance = MMSIParser.getInstance();
        MMSIType expResult = MMSIType.CoastStation;
        MMSIEntry result = instance.parse(mmsi);
        assertEquals(expResult, result.getType());
        MIDEntry mid = result.getMid();
        assertEquals("Finland", mid.getCountry());
        assertEquals("002301232", result.getString());
    }

    @Test
    public void testParseVHF()
    {
        System.out.println("parse");
        int mmsi = 842517724;
        MMSIParser instance = MMSIParser.getInstance();
        MMSIType expResult = MMSIType.HandheldVHF;
        MMSIEntry result = instance.parse(mmsi);
        assertEquals(expResult, result.getType());
        MIDEntry mid = result.getMid();
        assertNull(mid);
        assertEquals("842517724", result.getString());
    }

    @Test
    public void testParseNavAid()
    {
        System.out.println("parse");
        int mmsi = 994136301;
        MMSIParser instance = MMSIParser.getInstance();
        MMSIType expResult = MMSIType.NavigationalAid;
        MMSIEntry result = instance.parse(mmsi);
        assertEquals(expResult, result.getType());
        MIDEntry mid = result.getMid();
        assertEquals("China", mid.getCountry());
        assertEquals("994136301", result.getString());
    }

    @Test
    public void testParseSAR()
    {
        System.out.println("parse");
        int mmsi = 970123456;
        MMSIParser instance = MMSIParser.getInstance();
        MMSIType expResult = MMSIType.SearchAndRescueTransponder;
        MMSIEntry result = instance.parse(mmsi);
        assertEquals(expResult, result.getType());
        MIDEntry mid = result.getMid();
        assertNull(mid);
        assertEquals("970123456", result.getString());
    }

    @Test
    public void testParseMOB()
    {
        System.out.println("parse");
        int mmsi = 972123456;
        MMSIParser instance = MMSIParser.getInstance();
        MMSIType expResult = MMSIType.MobDevice;
        MMSIEntry result = instance.parse(mmsi);
        assertEquals(expResult, result.getType());
        MIDEntry mid = result.getMid();
        assertNull(mid);
        assertEquals("972123456", result.getString());
    }

    @Test
    public void testParseEPIRB()
    {
        System.out.println("parse");
        int mmsi = 974123456;
        MMSIParser instance = MMSIParser.getInstance();
        MMSIType expResult = MMSIType.EPIRB;
        MMSIEntry result = instance.parse(mmsi);
        assertEquals(expResult, result.getType());
        MIDEntry mid = result.getMid();
        assertNull(mid);
        assertEquals("974123456", result.getString());
    }

    @Test
    public void testParseLifeRaft()
    {
        System.out.println("parse");
        int mmsi = 982306301;
        MMSIParser instance = MMSIParser.getInstance();
        MMSIType expResult = MMSIType.CraftAssociatedWithParentShip;
        MMSIEntry result = instance.parse(mmsi);
        assertEquals(expResult, result.getType());
        MIDEntry mid = result.getMid();
        assertEquals("Finland", mid.getCountry());
        assertEquals("982306301", result.getString());
    }

}
