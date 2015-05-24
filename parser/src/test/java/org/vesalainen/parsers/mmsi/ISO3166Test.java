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
public class ISO3166Test
{
    
    public ISO3166Test()
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
     * Test of getForAlpha2 method, of class ISO3166.
     */
    @Test
    public void testGetForAlpha2()
    {
        System.out.println("getForAlpha2");
        String alpha2 = "AX";
        String expResult = "Åland Islands";
        ISO3166Entry result = ISO3166.getForAlpha2(alpha2);
        assertEquals(expResult, result.getEnglishShortName());
    }

    /**
     * Test of getForAlpha3 method, of class ISO3166.
     */
    @Test
    public void testGetForAlpha3()
    {
        System.out.println("getForAlpha3");
        String alpha3 = "VUT";
        String expResult = "Vanuatu";
        ISO3166Entry result = ISO3166.getForAlpha3(alpha3);
        assertEquals(expResult, result.getEnglishShortName());
    }

    /**
     * Test of getForNumeric method, of class ISO3166.
     */
    @Test
    public void testGetForNumeric()
    {
        System.out.println("getForNumeric");
        int numeric = 716;
        String expResult = "Zimbabwe (le)";
        ISO3166Entry result = ISO3166.getForNumeric(numeric);
        assertEquals(expResult, result.getFrenchShortName());
    }
    
}
