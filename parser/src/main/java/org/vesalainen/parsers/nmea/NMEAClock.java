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

import java.util.concurrent.TimeUnit;
import org.vesalainen.util.Transactional;

/**
 *
 * @author Timo Vesalainen
 */
public interface NMEAClock extends Transactional
{
    /**
     * Set day of month. (1 - 31)
     * @param day 
     */
    void setDay(int day);
    /**
     * Set month (1 - 12)
     * @param month 
     */
    void setMonth(int month);
    /**
     * Set year. If year &lt; 70 add 2000. If 
     * year &lt; 100 add 1900. 
     * @param year 
     */
    void setYear(int year);
    /**
     * Set year, month and day
     * @param year
     * @param month
     * @param day 
     * @see org.vesalainen.parsers.nmea.NMEAClock#setYear(int) 
     * @see org.vesalainen.parsers.nmea.NMEAClock#setMonth(int)
     * @see org.vesalainen.parsers.nmea.NMEAClock#setDay(int) 
     */
    void setDate(int year, int month, int day);
    /**
     * Set hour (0-23), minute (0-59), second (0-59) and milli second (0-999).
     * @param hour
     * @param minute
     * @param second
     * @param milliSecond 
     */
    void setTime(int hour, int minute, int second, int milliSecond);
    /**
     * Returns hour of day (0-23)
     * @return 
     */
    int getHour();
    /**
     * Returns minute of hour (0-59)
     * @return 
     */
    int getMinute();
    /**
     * Returns second of minute (0-59)
     * @return 
     */
    int getSecond();
    /**
     * Returnr millisecond of second (0-999)
     * @return 
     */
    int getMilliSecond();
    /**
     * Returns day of month (1-31)
     * @return 
     */
    int getDay();
    /**
     * Returns month of year (1-12)
     * @return 
     */
    int getMonth();
    /**
     * Returns 4-digit year
     * @return 
     */
    int getYear();
    /**
     * returns true if time is synchronized
     * @return 
     */
    boolean isReady();
    /**
     * Wait until synchronized
     * @param timeout
     * @param unit
     * @return True if ready false if timeout
     * @throws java.lang.InterruptedException
     */
    boolean waitUntilReady(long timeout, TimeUnit unit) throws InterruptedException;

}
