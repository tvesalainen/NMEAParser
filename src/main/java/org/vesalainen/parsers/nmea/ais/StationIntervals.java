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
public enum StationIntervals
{

    /**
     * As given by the autonomous mode
     */
    AsGivenByTheAutonomousMode("As given by the autonomous mode"),
    /**
     * 10 Minutes
     */
    T10Minutes("10 Minutes"),
    /**
     * 6 Minutes
     */
    T6Minutes("6 Minutes"),
    /**
     * 3 Minutes
     */
    T3Minutes("3 Minutes"),
    /**
     * 1 Minute
     */
    T1Minute("1 Minute"),
    /**
     * 30 Seconds
     */
    T30Seconds("30 Seconds"),
    /**
     * 15 Seconds
     */
    T15Seconds("15 Seconds"),
    /**
     * 10 Seconds
     */
    T10Seconds("10 Seconds"),
    /**
     * 5 Seconds
     */
    T5Seconds("5 Seconds"),
    /**
     * Next Shorter Reporting Interval
     */
    NextShorterReportingInterval("Next Shorter Reporting Interval"),
    /**
     * Next Longer Reporting Interval
     */
    NextLongerReportingInterval("Next Longer Reporting Interval"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse("Reserved for future use"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse12("Reserved for future use"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse13("Reserved for future use"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse14("Reserved for future use"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse15("Reserved for future use");
    private String description;

    StationIntervals(String description)
    {
        this.description = description;
    }

    @Override
    public String toString()
    {
        return description;
    }
}
