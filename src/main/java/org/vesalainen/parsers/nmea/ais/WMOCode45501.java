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
public enum WMOCode45501
{
    /**
     * Clear (no clouds at any level)
     */
    Clear,
    Cloudy,
    Rain,
    Fog,
    Snow,
    TyphoonHurricane,
    Monsoon,
    Thunderstorm,
    NA,
    ReservedForFutureUse9,
    ReservedForFutureUse10,
    ReservedForFutureUse11,
    ReservedForFutureUse12,
    ReservedForFutureUse13,
    ReservedForFutureUse14,
    ReservedForFutureUse15;
}
