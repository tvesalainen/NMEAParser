/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nmea.router;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class YXXDRFilterTest
{
    
    public YXXDRFilterTest()
    {
    }

    @Test
    public void test1()
    {
        YXXDRFilter f = new YXXDRFilter();
        assertTrue(f.accept("$HCHDG,64.3,0.0,E,,*18"));
        assertTrue(f.accept("$YXXDR,A,-0.080,G,XACC,A,0.000,G,YACC,A,1.010,G,ZACC*58"));
        assertFalse(f.accept("$YXXDR,A,-1.221,D,RRAT,A,-0.838,D,PRAT,A,1.414,D,YRAT*7B"));
        assertFalse(f.accept("$YXXDR,A,-1.219,D,RRTR,A,-0.914,D,PRTR,A,1.367,D,YRTR*6F"));
    }
    
}
