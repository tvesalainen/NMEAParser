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

/**
 *
 * @author Timo Vesalainen
 */
public interface Clock
{
    /**
     * Returns this Clocks Time
     * @return 
     */
    long getTime();

    /**
     * Update UTC Date
     * @param time hhmmss.ss
     */
    public void setTime(float utc);

    /**
     * Update UTC Date
     * @param date ddmmyy
     */
    public void setDate(int date);
    /**
     * Read NMEA sentence was broken.
     */
    public void rollback();
    /**
     * Read NMEA sentence was correct.
     */
    public void commit();
    /**
     * Day, 01 to 31
     * @param day 
     */
    public void setDay(int day);
    /**
     * Month, 01 to 12
     * @param month 
     */
    public void setMonth(int month);
    /**
     * Year (4 digits)
     * @param year 
     */
    public void setYear(int year);
    /**
     * Local zone description, 00 to +- 13 hours
     * @param localZoneHours 
     */
    public void setZoneHours(int localZoneHours);
    /**
     * Local zone minutes description, apply same sign as local hours
     * @param localZoneMinutes 
     */
    public void setZoneMinutes(int localZoneMinutes);
    /**
     * returns true if committed ever
     * @return 
     */
    public boolean isCommitted();
}
