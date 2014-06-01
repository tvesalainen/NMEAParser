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
        assertEquals("230123250", result.toString());
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
        assertEquals("023012325", result.toString());
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
        assertEquals("002301232", result.toString());
    }

}
