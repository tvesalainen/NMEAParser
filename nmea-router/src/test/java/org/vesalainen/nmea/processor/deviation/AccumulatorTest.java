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
package org.vesalainen.nmea.processor.deviation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.lang.Primitives;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AccumulatorTest
{
    
    public AccumulatorTest()
    {
    }

    @Test
    public void test1()
    {
        Accumulator a = new Accumulator();
        a.add(1, 2);
        a.add(2, 4);
        a.setAnnotation("foo bar");
        assertEquals(1.5, a.getX(), 1e-10);
        assertEquals(3, a.getY(), 1e-10);
        String str = a.toString();
        Accumulator b = new Accumulator(str);
        assertEquals(a, b);
    }
    @Test
    public void test2()
    {
        Accumulator a = new Accumulator(1, 2, 1, 0, null);
        Accumulator b = new Accumulator("1 2 1");
        assertEquals(a, b);
    }    
    @Test
    public void test3()
    {
        Accumulator a = new Accumulator(1, 2, 1, 1, null);
        Accumulator b = new Accumulator("1 2 1 1");
        assertEquals(a, b);
    }    
    @Test
    public void test4()
    {
        Accumulator a = new Accumulator(1, 2, 1, 1, "foo bar");
        Accumulator b = new Accumulator("1 2 1 1 foo bar");
        assertEquals(a, b);
    }    
    @Test
    public void test5()
    {
        Accumulator a = new Accumulator();
        a.add(0, 2);
        a.add(10, 2, 3);
        assertEquals(7.5, a.getX(), 1e-10);
        assertEquals(2, a.getY(), 1e-10);
    }    
    @Test
    public void test6()
    {
        Accumulator a = new Accumulator();
        int index = 1;
        int from = index*10;
        int to = from+10;
        a.setAnnotation("# ["+from+" - "+to+")");
        assertEquals("0.0 0.0 0.0 0 # [10 - 20)", a.toString());
    }
}
