/*
 * Copyright (C) 2016 tkv
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
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.EnumMap;
import org.vesalainen.parsers.nmea.NMEAClock;
import org.vesalainen.util.IntMap;
import org.vesalainen.util.IntReference;

/**
 *
 * @author tkv
 */
public final class GPSClock extends Clock implements NMEAClock, TemporalAccessor
{
    private static final int OffsetCommitCount = 1000;    // number commits between offset calculation.
    IntMap<ChronoField> uncommitted = new IntMap<>(new EnumMap<ChronoField,IntReference>(ChronoField.class));
    IntMap<ChronoField> committed = new IntMap<>(new EnumMap<ChronoField,IntReference>(ChronoField.class));
    private int commitCount;
    private Clock baseClock;
    private Clock clock;
    private TemporalAccessorImpl accessor = new TemporalAccessorImpl();
    private boolean changed;

    public GPSClock()
    {
        this(Clock.systemUTC());
    }

    public GPSClock(Clock clock)
    {
        this.baseClock = clock;
        this.clock = clock;
    }
    
    @Override
    public ZoneId getZone()
    {
        return clock.getZone();
    }

    @Override
    public Clock withZone(ZoneId zone)
    {
        return clock.withZone(zone);
    }

    @Override
    public Instant instant()
    {
        return clock.instant();
    }

    @Override
    public long millis()
    {
        return clock.millis();
    }

    @Override
    public void commit()
    {
        if (
                changed(ChronoField.DAY_OF_MONTH) ||
                changed(ChronoField.MONTH_OF_YEAR) ||
                changed(ChronoField.YEAR)
                )
        {
            if (
                    uncommitted.containsKey(ChronoField.YEAR) &&
                    uncommitted.containsKey(ChronoField.MONTH_OF_YEAR) &&
                    uncommitted.containsKey(ChronoField.DAY_OF_MONTH) &&
                    uncommitted.containsKey(ChronoField.HOUR_OF_DAY) &&
                    uncommitted.containsKey(ChronoField.MINUTE_OF_HOUR) && 
                    uncommitted.containsKey(ChronoField.SECOND_OF_MINUTE)
                    )
            {
                ZonedDateTime zonedDateTime = ZonedDateTime.of(
                        uncommitted.getInt(ChronoField.YEAR),
                        uncommitted.getInt(ChronoField.MONTH_OF_YEAR), 
                        uncommitted.getInt(ChronoField.DAY_OF_MONTH), 
                        uncommitted.getInt(ChronoField.HOUR_OF_DAY), 
                        uncommitted.getInt(ChronoField.MINUTE_OF_HOUR), 
                        uncommitted.getInt(ChronoField.SECOND_OF_MINUTE),
                        0,
                        ZoneOffset.UTC);
                Duration duration = Duration.between(baseClock.instant(), zonedDateTime);
                clock = Clock.offset(baseClock, duration);
            }
        }
        committed.putAll(uncommitted);
    }

    private boolean changed(ChronoField chronoField)
    {
        if (committed.containsKey(chronoField))
        {
            int u = uncommitted.getInt(chronoField);
            int c = committed.getInt(chronoField);
            return u == c;
        }
        else
        {
            return true;
        }
    }
    @Override
    public void rollback()
    {
    }

    @Override
    public boolean isSupported(TemporalField field)
    {
        if (field instanceof ChronoField)
        {
            ChronoField chronoField = (ChronoField) field;
            switch (chronoField)
            {
                case YEAR:
                case MONTH_OF_YEAR:
                case DAY_OF_MONTH:
                case HOUR_OF_DAY:
                case MINUTE_OF_HOUR:
                case SECOND_OF_MINUTE:
                case MICRO_OF_SECOND:
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

    @Override
    public long getLong(TemporalField field)
    {
        if (field instanceof ChronoField)
        {
            ChronoField chronoField = (ChronoField) field;
            if (committed.containsKey(chronoField))
            {
                return committed.getInt(chronoField);
            }
            else
            {
                throw new DateTimeException("no value for "+field);
            }
        }
        throw new UnsupportedTemporalTypeException(field.toString());
    }

    @Override
    public void setTime(int hour, int minute, int second, int microSecond)
    {
        setHour(hour);
        setMinute(minute);
        setSecond(second);
        setMicroSecond(microSecond);
    }

    @Override
    public void setDate(int year, int month, int day)
    {
        setYear(year);
        setMonth(month);
        setDay(day);
    }

    @Override
    public void setHour(int hour)
    {
        uncommitted.put(ChronoField.HOUR_OF_DAY, hour);
    }

    @Override
    public void setMinute(int minute)
    {
        uncommitted.put(ChronoField.MINUTE_OF_HOUR, minute);
    }

    @Override
    public void setSecond(int second)
    {
        int s = (int) second;
        uncommitted.put(ChronoField.SECOND_OF_MINUTE, s);
    }

    @Override
    public void setMicroSecond(int microSecond)
    {
        uncommitted.put(ChronoField.MICRO_OF_SECOND, microSecond);
    }

    @Override
    public void setDay(int day)
    {
        uncommitted.put(ChronoField.DAY_OF_MONTH, day);
    }

    @Override
    public void setMonth(int month)
    {
        uncommitted.put(ChronoField.MONTH_OF_YEAR, month);
    }

    @Override
    public void setYear(int year)
    {
        uncommitted.put(ChronoField.YEAR, getYear(year));
    }

    @Override
    public void setZoneHours(int localZoneHours)
    {
        System.err.println("WARNING: ZDA sentence setZoneHours("+localZoneHours+") not implemented");
    }

    @Override
    public void setZoneMinutes(int localZoneMinutes)
    {
        System.err.println("WARNING: ZDA sentence setZoneMinutes("+localZoneMinutes+") not implemented");
    }

    @Override
    public boolean isCommitted()
    {
        return !committed.isEmpty();
    }
    
    private static int getYear(int year)
    {
        if (year < 70)
        {
            return 2000 + year;
        }
        else
        {
            if (year < 100)
            {
                return 1900 + year;
            }
            else
            {
                return year;
            }
        }
    }

}
