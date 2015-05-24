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

import java.util.Collection;
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
public class MIDTest
{
    
    public MIDTest()
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
     * Test of getAllEntries method, of class MID.
     */
    @Test
    public void testGetAllEntries()
    {
        System.out.println("getAllEntries");
        Collection<MIDEntry> result = MID.getAllEntries();
        assertTrue(!result.isEmpty());
    }

    /**
     * Test of get method, of class MID.
     */
    @Test
    public void testGet()
    {
        System.out.println("get");
        int mid = 230;
        String expResult = "Finland";
        MIDEntry result = MID.get(mid);
        assertEquals(expResult, result.getCountry());
        assertEquals(230, result.getMid());
        ISO3166Entry iso3166Entry = result.getIso3166Entry();
        assertEquals("FI", iso3166Entry.getAlpha2Code());
    }
    
}
