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
public enum PrecipitationTypes
{

    /**
     * Reserved
     */
    Reserved("Reserved"),
    /**
     * Rain
     */
    Rain("Rain"),
    /**
     * Thunderstorm
     */
    Thunderstorm("Thunderstorm"),
    /**
     * Freezing rain
     */
    FreezingRain("Freezing rain"),
    /**
     * Mixed/ice
     */
    MixedIce("Mixed/ice"),
    /**
     * Snow
     */
    Snow("Snow"),
    /**
     * Reserved
     */
    Reserved6("Reserved"),
    /**
     * N/A (default)
     */
    NADefault("N/A (default)");
    private String description;

    PrecipitationTypes(String description)
    {
        this.description = description;
    }

    public String toString()
    {
        return description;
    }
}
