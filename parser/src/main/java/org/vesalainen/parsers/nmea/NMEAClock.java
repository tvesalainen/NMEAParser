/*
 * Copyright (C) 2013 Timo Vesalainen
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
package org.vesalainen.parsers.nmea;

import java.time.ZonedDateTime;
import org.vesalainen.time.MutableDateTime;
import org.vesalainen.util.Transactional;

/**
 *
 * @author Timo Vesalainen
 */
public interface NMEAClock extends Transactional
{
    void setDay(int day);
    void setMonth(int month);
    void setYear(int year);
    void setDate(int year, int month, int day);
    void setTime(int hour, int minute, int second, int milliSecond);
    public int getHour();

    public int getMinute();
    public int getSecond();
    public int getMilliSecond();
    public int getDay();
    public int getMonth();
    public int getYear();
    /**
     * returns true if committed ever
     * @return 
     */
    boolean isCommitted();

}
