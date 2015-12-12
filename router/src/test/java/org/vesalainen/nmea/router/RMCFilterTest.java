/*
 * Copyright (C) 2015 tkv
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

import java.util.concurrent.TimeUnit;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class RMCFilterTest
{
    
    public RMCFilterTest()
    {
        System.err.println(TimeUnit.DAYS.convert(Integer.MAX_VALUE, TimeUnit.SECONDS));
    }

    @Test
    public void test1()
    {
        RMCFilter filter = new RMCFilter();
        assertFalse(filter.accept("$GPRMC,062458,A,6009.2053,N,02453.6493,E,000.0,001.3,171009,,,A*72\r\n"));
        assertFalse(filter.accept("$GPRMC,081836,A,3751.65,S,14507.36,E,000.0,360.0,130998,011.3,E*62\r\n"));
        assertFalse(filter.accept("$GPRMC,062457,A,6009.2053,N,02453.6493,E,000.0,001.3,171009,,,A*7D\r\n"));
        assertTrue(filter.accept("$GPAAM,A,A,0.10,N,WPTNME*32\r\n"));
        assertTrue(filter.accept("$GPRMC,062458,A,6009.2053,N,02453.6493,E,000.0,001.3,171009,,,A*72\r\n"));
        assertFalse(filter.accept("$GPRMC,081836,A,3751.65,S,14507.36,E,000.0,360.0,130998,011.3,E*62\r\n"));
        assertTrue(filter.accept("$GPRMC,062458,A,6009.2053,N,02453.6493,E,000.0,001.3,171009,,,A*72\r\n"));
    }
    
}
