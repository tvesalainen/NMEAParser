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
 * MMSI MID entry
 * @author Timo Vesalainen
 */
public final class MIDEntry
{
    private final int mid;
    private final String country;
    private final ISO3166Entry iso3166Entry;

    MIDEntry(int mid, String country, ISO3166Entry iso3166Entry)
    {
        this.mid = mid;
        this.country = country;
        this.iso3166Entry = iso3166Entry;
    }
    /**
     * Returns Continent
     * @return 
     */
    public Continent getContinent()
    {
        return Continent.values()[mid/100];
    }
    /**
     * Returns MID
     * @return 
     */
    public int getMid()
    {
        return mid;
    }
    /**
     * Returns country name
     * @return 
     */
    public String getCountry()
    {
        return country;
    }
    /**
     * Returns ISO3166Entry or null if no ISO3166Entry is associated
     * @return 
     */
    public ISO3166Entry getIso3166Entry()
    {
        return iso3166Entry;
    }
    
}
