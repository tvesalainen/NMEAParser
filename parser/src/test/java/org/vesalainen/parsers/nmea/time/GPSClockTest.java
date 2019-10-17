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
import java.util.Random;
import java.util.function.LongSupplier;
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
        NanoTime time = new NanoTime();
        GPSClock clock = GPSClock.getInstance(time, true);
        clock.start(null);
        clock.setYear(1970);
        clock.setMonth(1);
        clock.setDay(1);
        clock.setTime(0, 0, 0, 100);
        clock.commit(null);
        clock.setTime(0, 0, 0, 500);
        clock.commit(null);
        assertEquals(500, clock.millis());
        assertEquals("1970-01-01T00:00:00.500Z", clock.instant().toString());
        
        time.time = 123L;
        assertEquals("1970-01-01T00:00:00.500000123Z", clock.instant().toString());
        
        
        time.time = 1000000123L;
        assertEquals("1970-01-01T00:00:01.500000123Z", clock.instant().toString());
        
    }
    @Test
    public void testLive2()
    {
        Random random = new Random(123456789L);
        NanoTime time = new NanoTime();
        GPSClock clock = GPSClock.getInstance(time, true);
        for (int s=0;s<3;s++)
        {
            time.time = 1000000000L*s;
            clock.start(null);
            clock.setYear(1970);
            clock.setMonth(1);
            clock.setDay(1);
            clock.setTime(0, 0, s, 0);
            clock.commit(null);
        }
        for (int s=3;s<60;s++)
        {
            time.time = 1000000000L*s + random.nextInt(100000000);
            clock.start(null);
            clock.setYear(1970);
            clock.setMonth(1);
            clock.setDay(1);
            clock.setTime(0, 0, s, 0);
            clock.commit(null);
            time.time = 1000000000L*s + 500000000;
            assertEquals(s*1000+500, clock.millis());
        }
    }
    @Test
    public void testMonotonicy()
    {
        NanoTime time = new NanoTime();
        GPSClock clock = GPSClock.getInstance(time, true);
        for (int s=0;s<3;s++)
        {
            time.time = 1000000000L*s;
            clock.start(null);
            clock.setYear(1970);
            clock.setMonth(1);
            clock.setDay(1);
            clock.setTime(0, 0, s, 0);
            clock.commit(null);
        }
        clock.start(null);
        clock.setYear(1970);
        clock.setMonth(1);
        clock.setDay(1);
        clock.setTime(0, 0, 3, 0);
        time.time = 1000000000L*3 + 500000000;
        clock.commit(null);
        assertEquals(3500, clock.millis());
        time.time = 1000000000L*3 + 400000000;
        assertEquals(3500, clock.millis());
    }
    private class NanoTime implements LongSupplier
    {
        private long time;
        
        @Override
        public long getAsLong()
        {
            return time;
        }
        
    }
}
