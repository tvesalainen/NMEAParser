/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nmea.processor;

import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.nmea.processor.CompassCorrector.Angle;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CompassCorrectorTest
{
    
    public CompassCorrectorTest()
    {
    }

    @Test
    public void test1()
    {
        Angle a = new Angle(1);
        a.add(60, 25, 60, 26);
        Angle b = new Angle(a.toString());
        assertEquals(a, b);
        assertEquals(90 , a.angle(), 1e-6);
        a.add(60, 25, 61, 25);
        assertEquals(45 , a.angle(), 1e-6);
    }
}
