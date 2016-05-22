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

import java.time.DateTimeException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.EnumMap;
import org.vesalainen.parsers.nmea.NMEAClock;
import org.vesalainen.util.LongMap;
import org.vesalainen.util.LongReference;

/**
 *
 * @author tkv
 */
public class TemporalAccessorImpl implements TemporalAccessor, NMEAClock, CharSequence
{
    LongMap<ChronoField> uncommitted = new LongMap<>(new EnumMap<ChronoField,LongReference>(ChronoField.class));
    LongMap<ChronoField> committed = new LongMap<>(new EnumMap<ChronoField,LongReference>(ChronoField.class));

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
                return committed.getLong(chronoField);
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
    public void rollback()
    {
    }

    @Override
    public void commit()
    {
        committed.putAll(uncommitted);
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

    @Override
    public int length()
    {
        return 20;
    }

    @Override
    public char charAt(int index)
    {
        switch (index)
        {
            case 0:
            case 1:
            case 2:
            case 3:
                return chr(ChronoField.YEAR, 3-index);
            case 4:
                return '-';
            case 5:
            case 6:
                return chr(ChronoField.MONTH_OF_YEAR, 1-index+5);
            case 7:
                return '-';
            case 8:
            case 9:
                return chr(ChronoField.DAY_OF_MONTH, 1-index+8);
            case 10:
                return 'T';
            case 11:
            case 12:
                return chr(ChronoField.HOUR_OF_DAY, 1-index+11);
            case 13:
                return ':';
            case 14:
            case 15:
                return chr(ChronoField.MINUTE_OF_HOUR, 1-index+14);
            case 16:
                return ':';
            case 17:
            case 18:
                return chr(ChronoField.SECOND_OF_MINUTE, 1-index+17);
            case 19:
                return 'Z';
            default:
                throw new IllegalArgumentException(index+" ");
        }
    }

    private char chr(ChronoField field, int idx)
    {
        if (committed.containsKey(field))
        {
            long l = committed.getLong(field);
            int m = (int) (l/Math.pow(10, idx)%10);
            return Character.forDigit(m, 10);
        }
        else
        {
            return '0';
        }
    }
    @Override
    public CharSequence subSequence(int start, int end)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
