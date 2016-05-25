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
import org.vesalainen.time.MutableTime;
import org.vesalainen.util.Transactional;

/**
 *
 * @author Timo Vesalainen
 */
public interface NMEAClock extends MutableTime, Transactional
{
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
     * returns true if committed ever
     * @return 
     */
    boolean isCommitted();
    /**
     * Returns milliseconds from epoch.
     * @return 
     */
    long millis();
    /**
     * Set time in milliseconds from epoch.
     * @param millis 
     */
    void setMillis(long millis);
    /**
     * Returns ZonedDateTime created from latest fix from GPS.
     * @return 
     */
    ZonedDateTime getZonedDateTime();
    /**
     * Returns offset from system clock.
     * @return 
     */
    long offset();
}
