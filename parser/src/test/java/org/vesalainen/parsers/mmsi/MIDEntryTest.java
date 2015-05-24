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
public class MIDEntryTest
{
    
    public MIDEntryTest()
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
     * Test of getMid method, of class MIDEntry.
     */
    @Test
    public void testGetMid()
    {
        System.out.println("getMid");
        MIDEntry instance = MID.get(230);
        int expResult = 230;
        int result = instance.getMid();
        assertEquals(expResult, result);
    }

    /**
     * Test of getCountry method, of class MIDEntry.
     */
    @Test
    public void testGetCountry()
    {
        System.out.println("getCountry");
        MIDEntry instance = MID.get(230);
        String expResult = "Finland";
        String result = instance.getCountry();
        assertEquals(expResult, result);
    }

    /**
     * Test of getIso3166Entry method, of class MIDEntry.
     */
    @Test
    public void testGetIso3166Entry()
    {
        System.out.println("getIso3166Entry");
        MIDEntry instance = MID.get(230);
        String expResult = "Finland";
        ISO3166Entry result = instance.getIso3166Entry();
        assertEquals(expResult, result.getEnglishShortName());
    }

    /**
     * Test of getContinent method, of class MIDEntry.
     */
    @Test
    public void testGetContinent()
    {
        System.out.println("getContinent");
        MIDEntry instance = MID.get(230);
        Continent expResult = Continent.Europe;
        Continent result = instance.getContinent();
        assertEquals(expResult, result);
    }
    
}
