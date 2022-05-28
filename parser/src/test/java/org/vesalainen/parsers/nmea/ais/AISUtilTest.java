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

import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.util.HexUtil;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AISUtilTest
{
    
    public AISUtilTest()
    {
    }

    @Test
    public void test0()
    {
        int rot = AISUtil.rot(-0.04464029F);
        float rot1 = AISUtil.rot(rot);
    }
    @Test
    public void testRot()
    {
        for (int r=-128;r<128;r++)
        {
            float rot = AISUtil.rot(r);
            int rot1 = AISUtil.rot(rot);
            assertEquals(r, rot1);
        }
    }
    @Test
    public void test6Bit()
    {
        byte[] arr = HexUtil.fromString("44482452");
        byte[] arr6 = AISUtil.makeArray(arr);
        System.err.println(HexUtil.toString(arr6));
        String makeString = AISUtil.makeString("001111001110010111000001001111000110000110000000000000");
    }    
}
