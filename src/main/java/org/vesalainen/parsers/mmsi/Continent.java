/*
 * Copyright (C) 2014 Timo Vesalainen
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

package org.vesalainen.parsers.mmsi;

/**
 * Continent enumerates continates as in MMSI MID
 * @author Timo Vesalainen
 */
public enum Continent
{
    NotUsed0("Not used 0"),
    NotUsed1("Not used 1"),
    Europe("Europe"),
    NorthAndCentralAmericaAndCaribbean("North and Central America and Caribbean"),
    Asia("Asia"),
    Oceana("Oceana"),
    Africa("Africa"),
    SouthAmerica("South America");
    
    private final String description;

    private Continent(String description)
    {
        this.description = description;
    }

    @Override
    public String toString()
    {
        return description;
    }
}
