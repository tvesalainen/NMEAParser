/*
 * Copyright (C) 2012 Timo Vesalainen
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

import java.io.ByteArrayInputStream;
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
public class AISInputStreamTest
{
    
    public AISInputStreamTest()
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
     * Test of read method, of class AISInputStream.
     */
    @Test
    public void testRead() throws Exception
    {
        System.out.println("read");
        ByteArrayInputStream in = new ByteArrayInputStream("14eG;o@034o8sd<L9i:a;WF>062D".getBytes());
        AISInputStream ain = new AISInputStream(in);
        StringBuilder result = new StringBuilder();
        int cc = ain.read();
        while (cc != -1)
        {
            result.append((char)cc);
            cc = ain.read();
        }
        String expResult = "000001000100101101010111001011110111010000000000000011000100110111001000111011101100001100011100001001110001001010101001001011100111010110001110000000000110000010010100";
        assertEquals(expResult, result.toString());
    }

}
