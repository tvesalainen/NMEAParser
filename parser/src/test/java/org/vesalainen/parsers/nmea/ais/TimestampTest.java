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
package org.vesalainen.parsers.nmea.ais;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TimestampTest
{
    
    public TimestampTest()
    {
    }

    @Test
    public void testAdjustIntoSecond()
    {
        ZonedDateTime zdt0 = ZonedDateTime.of(2018, 10, 23, 10, 11, 12, 0, ZoneId.of("Z"));
        ZonedDateTime zdt1 = ZonedDateTime.of(2018, 10, 23, 10, 11, 9, 0, ZoneId.of("Z"));
        ZonedDateTime zdt2 = ZonedDateTime.of(2018, 10, 23, 10, 10, 14, 0, ZoneId.of("Z"));
        assertEquals(zdt0, TimestampSupport.adjustIntoSecond(zdt0, 12));
        assertEquals(zdt1, TimestampSupport.adjustIntoSecond(zdt0, 9));
        assertEquals(zdt2, TimestampSupport.adjustIntoSecond(zdt0, 14));
    }
    
}
