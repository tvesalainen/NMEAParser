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

import org.vesalainen.util.Transactional;

/**
 *
 * @author Timo Vesalainen
 */
public interface NMEAClock extends Transactional
{
    /**
     * Set utc time
     * @param hour 0 - 23
     * @param minute 0 - 59
     * @param second 0 - 59
     * @param microSecond 0-999
     */
    void setTime(int hour, int minute, int second, int microSecond);
    /**
     * Set utc date
     * @param year yy
     * @param month mm 1 - 12
     * @param day dd 1 - 31
     */
    void setDate(int year, int month, int day);
    /**
     * Update UTC Hour
     * @param hour
     */
    void setHour(int hour);
    /**
     * Update utc minute
     * @param minute 
     */
    void setMinute(int minute);
    /**
     * Update utc second
     * @param second 
     */
    void setSecond(int second);
    /**
     * Update UTC microsecond
     * @param milliSecond 
     */
    void setMilliSecond(int milliSecond);
    /**
     * Day of Month, 01 to 31
     * @param day 
     */
    void setDay(int day);
    /**
     * Month of Year, 01 to 12
     * @param month 
     */
    void setMonth(int month);
    /**
     * Year (4 digits)
     * @param year 
     */
    void setYear(int year);
    /**
     * Local zone description, 00 to +- 13 hours
     * @param localZoneHours 
     */
    void setZoneHours(int localZoneHours);
    /**
     * Local zone minutes description, apply same sign as local hours
     * @param localZoneMinutes 
     */
    void setZoneMinutes(int localZoneMinutes);
    /**
     * Return Year (4 digits)
     * @return 
     */
    int getYear();
    /**
     * Returns Month of Year (1-12)
     * @return 
     */
    int getMonth();
    /**
     * Return Day of Month (1-31)
     * @return 
     */
    int getDay();
    /**
     * Return Hour of Day (0-23)
     * @return 
     */
    int getHour();
    /**
     * Return Minute of Hour (0-59)
     * @return 
     */
    int getMinute();
    /**
     * Return Second of Minute (0-59)
     * @return 
     */
    int getSecond();
    /**
     * Return Milli Second of Second (0-999)
     * @return 
     */
    int getMilliSecond();
    /**
     * returns true if committed ever
     * @return 
     */
    boolean isCommitted();
}
