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
import static java.time.temporal.ChronoField.INSTANT_SECONDS;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import org.vesalainen.math.sliding.LongTimeoutSlidingMin;
import org.vesalainen.parsers.nmea.NMEAClock;
import org.vesalainen.time.AdjustableClock;
import org.vesalainen.time.MutableInstant;
import org.vesalainen.time.SimpleMutableDateTime;
import org.vesalainen.util.logging.AttachedLogger;
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
    private static final long MINUTE_IN_SECONDS = 60;
    private static final long HOUR_IN_SECONDS = 60*MINUTE_IN_SECONDS;
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
    private boolean needCalc = true;
    protected MutableInstant time = new MutableInstant();
    protected MutableInstant lastTime = new MutableInstant();
    private boolean lastTimeSet;
    private long dateSecond;
    private CountDownLatch ready = new CountDownLatch(1);
    protected int upd;
    protected final LongSupplier nanoTime;
    protected boolean partialUpdate;    // update with only time GGA, GLL,...

    public GPSClock()
    {
        this(System::nanoTime);
    }

    public GPSClock(LongSupplier nanoTime)
    {
        this.nanoTime = nanoTime;
    }
    
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
                dateSecond = zonedDate.getLong(INSTANT_SECONDS);
                needCalc = false;
            }
            long sec = dateSecond;
            sec += uncommitted.get(ChronoField.HOUR_OF_DAY)*HOUR_IN_SECONDS;
            sec += uncommitted.get(ChronoField.MINUTE_OF_HOUR)*MINUTE_IN_SECONDS;
            sec += uncommitted.get(ChronoField.SECOND_OF_MINUTE);
            time.set(sec, uncommitted.get(ChronoField.NANO_OF_SECOND));
            pulse0();
        }
    }

    private void pulse0()
    {
        if (time.compareTo(lastTime) > 0)
        {
            if (lastTimeSet)
            {
                pulse();
                if (ready != null)
                {
                    ready.countDown();
                    ready = null;
                    started();
                    JavaLogging.getLogger(GPSClock.class).config("GPSClock is ready");
                }
            }
            else
            {
                lastTimeSet = true;
            }
            lastTime.set(time);
        }
    }
    protected void started()
    {
    }
    protected void pulse()
    {
    }
    protected void adjust()
    {
    }
    public Instant reference()
    {
        return time.instant();
    }
    @Override
    public long millis()
    {
        return time.millis();
    }
    
    @Override
    public Instant instant()
    {
        return time.instant();
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
    public boolean isReady()
    {
        return ready == null;
    }

    @Override
    public boolean waitUntilReady(long timeout, TimeUnit unit) throws InterruptedException
    {
        if (ready != null)
        {
            return ready.await(timeout, unit);
        }
        return true;
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
        return getInstance(System::nanoTime, live);
    }
    public static final GPSClock getInstance(LongSupplier clock, boolean live)
    {
        return live ? new LiveGPSClock(clock) : new FixedGPSClock(clock);
    }
    public static final class LiveGPSClock extends GPSClock implements AdjustableClock, AttachedLogger
    {
        private static final int WINDOW_IN_MINUTES = 10;
        private static final long WINDOW = TimeUnit.MINUTES.toNanos(WINDOW_IN_MINUTES);
        private MutableInstant pulse = new MutableInstant();
        private MutableInstant now = new MutableInstant();
        private MutableInstant corrected = new MutableInstant();
        private MutableInstant last = new MutableInstant();
        private LongTimeoutSlidingMin offset = new LongTimeoutSlidingMin(nanoTime, WINDOW_IN_MINUTES, WINDOW);
        private long adjustment;
        private LongSupplier millis = System::currentTimeMillis;
        private Supplier<Instant> instant = Clock.systemUTC()::instant;
        private boolean useSystem = true;
        private ReentrantLock lock = new ReentrantLock();

        public LiveGPSClock(LongSupplier nanoTime)
        {
            super(nanoTime);
        }

        @Override
        protected void pulse()
        {
            lock.lock();
            try
            {
                long t = nanoTime.getAsLong();
                pulse.set(t);
                long behind = time.until(pulse);
                offset.accept(behind, t);
                long dif = behind-offset.getMin();
                pulse.plus(-dif);
                if (doMillis() == System.currentTimeMillis())
                {
                    if (!useSystem)
                    {
                        useSystem = true;
                        millis = System::currentTimeMillis;
                        fine("using system clock");
                    }
                }
                else
                {
                    if (useSystem)
                    {
                        useSystem = false;
                        millis = this::doMillis;
                        fine("using GPS clock");
                    }
                }
            }
            finally
            {
                lock.unlock();
            }
        }

        @Override
        protected void adjust()
        {
            super.adjust();
            lock.lock();
            try
            {
                now.set(nanoTime.getAsLong());
                long d = pulse.until(now);
                corrected.set(time);
                corrected.plus(d + adjustment);
                // monotonic enforced
                if (corrected.compareTo(last) < 0)
                {
                    corrected.set(last);
                }
                else
                {
                    last.set(corrected);
                }
            }
            finally
            {
                lock.unlock();
            }
        }

        @Override
        protected void started()
        {
            instant = this::doInstant;
        }

        private long doMillis()
        {
            adjust();
            return corrected.millis();
        }
        @Override
        public long millis()
        {
            return millis.getAsLong();
        }

        private Instant doInstant()
        {
            adjust();
            return corrected.instant();
        }
        @Override
        public Instant instant()
        {
            return instant.get();
        }

        @Override
        public void adjust(long nanos)
        {
            adjustment += nanos;
        }

        @Override
        public long offset()
        {
            return adjustment;
        }

    }
    public static final class FixedGPSClock extends GPSClock
    {

        public FixedGPSClock(LongSupplier nanoTime)
        {
            super(nanoTime);
        }

    }
    
}
