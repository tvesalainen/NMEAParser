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
package org.vesalainen.parsers.nmea.ais;

/**
 *
 * @author Timo Vesalainen
 */
public enum DataTimeoutCodes
{

    /**
     * No time period (default)
     */
    NoTimePeriodDefault("No time period (default)"),
    /**
     * 10 minutes
     */
    T10Minutes(
    "10 minutes"),
    /**
     * 1 hour
     */
    T1Hour("1 hour"),
    /**
     * 6 hours
     */
    T6Hours("6 hours"),
    /**
     * 12 hours
     */
    T12Hours("12 hours"),
    /**
     * 24 hours
     */
    T24Hours("24 hours"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse("(reserved for future use)"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse7("(reserved for future use)");
    private String description;

    DataTimeoutCodes(String description)
    {
        this.description = description;
    }

    @Override
    public String toString()
    {
        return description;
    }
}
