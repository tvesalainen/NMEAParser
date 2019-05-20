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
import java.time.Instant;
import java.time.ZoneId;
import static java.time.ZoneOffset.UTC;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.function.LongSupplier;
import org.vesalainen.math.sliding.SlidingAverage;
import org.vesalainen.math.sliding.SlidingMin;
import org.vesalainen.parsers.nmea.NMEAClock;
import static org.vesalainen.time.MutableDateTime.HOUR_IN_MILLIS;
import static org.vesalainen.time.MutableDateTime.MINUTE_IN_MILLIS;
import static org.vesalainen.time.MutableDateTime.SECOND_IN_MILLIS;
import org.vesalainen.time.SimpleMutableDateTime;
import org.vesalainen.util.logging.JavaLogging;

/**
 * Transactional MutableClock.
 * <p>
 * There are two modes depending on base clock.
 * <p>Live (default). Data coming from active GPS. Time is updated between 
 * time-setting NMEA sentences to give accurate timing for other sentences as well.
 * <p>Fixed (use fixed base clock). Data coming from recorded NMEA sentences
 * like track file. Time is not updated between time-setting NMEA sentences.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class GPSClock extends Clock implements NMEAClock
{
    private static final long DRIFT_LIMIT = 500;
    private static final long MAX_DRIFT = 1;
    private static final int YEA = 1;
    private static final int MON = 2;
    private static final int DAY = 4;
    private static final int HOU = 8;
    private static final int MIN = 16;
    private static final int SEC = 32;
    private static final int MIL = 64;
    private static final int TIM = HOU|MIN|SEC|MIL;
    private static final int ALL = 127;
    private SimpleMutableDateTime uncommitted = new SimpleMutableDateTime();
    protected boolean committed;
    private boolean needCalc = true;
    private long dateMillis;
    protected int upd;
    private long millis;
    protected LongSupplier currentTimeMillis = System::currentTimeMillis;
    protected boolean partialUpdate;    // update with only time GGA, GLL,...
    

    @Override
    public void commit(String reason)
    {
        if (upd == ALL || (partialUpdate && uncommitted.get(ChronoField.YEAR) > 0))
        {
            if (needCalc)
            {
                ZonedDateTime zonedDate = ZonedDateTime.of(
                        uncommitted.get(ChronoField.YEAR),
                        uncommitted.get(ChronoField.MONTH_OF_YEAR),
                        uncommitted.get(ChronoField.DAY_OF_MONTH),
                        0,
                        0,
                        0,
                        0,
                        UTC);
                dateMillis = zonedDate.toInstant().toEpochMilli();
                needCalc = false;
            }
            long mls = dateMillis;
            mls += uncommitted.get(ChronoField.HOUR_OF_DAY)*HOUR_IN_MILLIS;
            mls += uncommitted.get(ChronoField.MINUTE_OF_HOUR)*MINUTE_IN_MILLIS;
            mls += uncommitted.get(ChronoField.SECOND_OF_MINUTE)*SECOND_IN_MILLIS;
            mls += uncommitted.get(ChronoField.NANO_OF_SECOND)/1000000;
            millis = mls;
            committed = true;
        }
    }

    @Override
    public long millis()
    {
        return millis;
    }
    
    @Override
    public ZoneId getZone()
    {
        return UTC;
    }

    @Override
    public Clock withZone(ZoneId zone)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Instant instant()
    {
        return Instant.ofEpochMilli(millis());
    }

    void setCurrentTimeMillis(LongSupplier currentTimeMillis)
    {
        this.currentTimeMillis = currentTimeMillis;
    }

    @Override
    public void setDay(int day)
    {
        upd |= DAY;
        if (uncommitted.getDay() != day)
        {
            uncommitted.setDay(day);
            needCalc = true;
        }
    }

    @Override
    public void setMonth(int month)
    {
        upd |= MON;
        if (uncommitted.getMonth()!= month)
        {
            uncommitted.setMonth(month);
            needCalc = true;
        }
    }

    @Override
    public void setYear(int year)
    {
        upd |= YEA;
        if (uncommitted.getYear()!= year)
        {
            uncommitted.setYear(year);
            needCalc = true;
        }
    }

    @Override
    public void setDate(int year, int month, int day)
    {
        setDay(day);
        setMonth(month);
        setYear(year);
    }

    @Override
    public void setTime(int hour, int minute, int second, int milliSecond)
    {
        upd |= TIM;
        uncommitted.setHour(hour);
        uncommitted.setMinute(minute);
        uncommitted.setSecond(second);
        uncommitted.setMilliSecond(milliSecond);
    }

    @Override
    public int getHour()
    {
        return uncommitted.getHour();
    }

    @Override
    public int getMinute()
    {
        return uncommitted.getMinute();
    }

    @Override
    public int getSecond()
    {
        return uncommitted.getSecond();
    }

    @Override
    public int getMilliSecond()
    {
        return uncommitted.getMilliSecond();
    }

    @Override
    public int getDay()
    {
        return uncommitted.getDay();
    }

    @Override
    public int getMonth()
    {
        return uncommitted.getMonth();
    }

    @Override
    public int getYear()
    {
        return uncommitted.getYear();
    }

    @Override
    public boolean isCommitted()
    {
        return committed;
    }

    @Override
    public void start(String reason)
    {
        upd = 0;
    }

    @Override
    public void rollback(String reason)
    {
    }

    @Override
    public long offset()
    {
        return 0L;
    }

    public void setPartialUpdate(boolean partialUpdate)
    {
        this.partialUpdate = partialUpdate;
    }

    @Override
    public String toString()
    {
        return "GPSClock{" + instant().toString() + '}';
    }

    public static final GPSClock getInstance(boolean live)
    {
        return live ? new LiveGPSClock() : new FixedGPSClock();
    }
    public static final GPSClock getSyncInstance()
    {
        return new SyncLiveGPSClock();
    }
    public static final class LiveGPSClock extends GPSClock
    {
        private long offset;
        
        @Override
        public void commit(String reason)
        {
            if (upd == ALL || partialUpdate)
            {
                super.commit(reason);
                offset = super.millis() - currentTimeMillis.getAsLong();
            }
        }

        @Override
        public long millis()
        {
            return currentTimeMillis.getAsLong() + offset;
        }

        @Override
        public long offset()
        {
            return offset;
        }

    }
    public static final class SyncLiveGPSClock extends GPSClock
    {
        private JavaLogging logger = JavaLogging.getLogger(LiveGPSClock.class);
        private long offset;
        private SlidingMin min = new SlidingMin(4);
        private SlidingAverage average = new SlidingAverage(256);
        
        @Override
        public void commit(String reason)
        {
            if (upd == ALL || partialUpdate)
            {
                super.commit(reason);
                long off = super.millis() - currentTimeMillis.getAsLong();
                min.accept(off);
                average.accept(min.getMin());
                logger.fine("off = %d %s", off, average);
                long delta = (long) (average.fast()-offset);
                long sig = delta >=0 ? 1 : -1;
                long adelta = Math.abs(delta);
                if (adelta > DRIFT_LIMIT)
                {
                    logger.fine("setting time %d = %d", off, offset);
                    offset = off;
                }
                else
                {
                    long adj = sig*Math.min(adelta, MAX_DRIFT);
                    if (adj != 0)
                    {
                        offset += adj;
                        logger.fine("adjusting time %d = %d", adj, offset);
                    }
                }
                committed = true;
            }
        }

        @Override
        public long millis()
        {
            return currentTimeMillis.getAsLong() + offset;
        }

        @Override
        public long offset()
        {
            return offset;
        }

    }
    public static final class FixedGPSClock extends GPSClock
    {
        
    }
    
}
