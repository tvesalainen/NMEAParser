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
package org.vesalainen.parsers.nmea.time;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoZonedDateTime;
import java.time.chrono.IsoChronology;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class GPSClockTest
{
    
    public GPSClockTest()
    {
    }

    @Test
    public void testFixed()
    {
        GPSClock clock = GPSClock.getInstance(false);
        clock.start(null);
        clock.setYear(1970);
        clock.setMonth(1);
        clock.setDay(1);
        clock.setTime(0, 0, 0, 500);
        clock.commit(null);
        assertEquals(500, clock.millis());
    }
    @Test
    public void testLive()
    {
        GPSClock clock = GPSClock.getInstance(true);
        clock.setCurrentTimeMillis(()->0L);
        clock.start(null);
        clock.setYear(1970);
        clock.setMonth(1);
        clock.setDay(1);
        clock.setTime(0, 0, 0, 0);
        clock.commit(null);
        assertEquals(0, clock.millis());

        clock.start(null);
        clock.setYear(1970);
        clock.setMonth(1);
        clock.setDay(1);
        clock.setTime(0, 0, 1, 0);
        clock.setCurrentTimeMillis(()->1002L);
        clock.commit(null);
        assertEquals(1001, clock.millis());

        clock.start(null);
        clock.setYear(1970);
        clock.setMonth(1);
        clock.setDay(1);
        clock.setTime(0, 0, 2, 0);
        clock.setCurrentTimeMillis(()->2002L);
        clock.commit(null);
        assertEquals(2001, clock.millis());

        clock.start(null);
        clock.setYear(1970);
        clock.setMonth(1);
        clock.setDay(1);
        clock.setTime(0, 0, 3, 0);
        clock.setCurrentTimeMillis(()->3001L);
        clock.commit(null);
        assertEquals(3000, clock.millis());
    }
    
}
