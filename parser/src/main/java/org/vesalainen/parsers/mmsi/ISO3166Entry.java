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

import java.util.Objects;

/**
 *
 * @author Timo Vesalainen
 */
public final class ISO3166Entry
{
    private final String englishShortName;
    private final String frenchShortName;
    private final String alpha2Code;
    private final String alpha3Code;
    private final int numeric;

    ISO3166Entry(String englishShortName, String frenchShortName, String alpha2Code, String alpha3Code, int numeric)
    {
        this.englishShortName = englishShortName;
        this.frenchShortName = frenchShortName;
        this.alpha2Code = alpha2Code;
        this.alpha3Code = alpha3Code;
        this.numeric = numeric;
    }
    /**
     * Return country name in English
     * @return 
     */
    public String getEnglishShortName()
    {
        return englishShortName;
    }
    /**
     * Return country name in French
     * @return 
     */
    public String getFrenchShortName()
    {
        return frenchShortName;
    }
    /**
     * Returns a two-letter code that represents a country name, recommended as 
     * the general purpose code
     * @return 
     */
    public String getAlpha2Code()
    {
        return alpha2Code;
    }
    /**
     * Returns a three-letter code that represents a country name, which is 
     * usually more closely related to the country name
     * @return 
     */
    public String getAlpha3Code()
    {
        return alpha3Code;
    }
    /**
     * Returns a numeric code
     * @return 
     */
    public int getNumeric()
    {
        return numeric;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode(this.alpha2Code);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final ISO3166Entry other = (ISO3166Entry) obj;
        if (!Objects.equals(this.alpha2Code, other.alpha2Code))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "ISO3166Entry{" + englishShortName + '}';
    }
    
}
