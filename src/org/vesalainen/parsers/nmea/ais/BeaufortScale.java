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
public enum BeaufortScale
{

    /**
     * Calm
     */
    Calm("Calm"),
    /**
     * Light air
     */
    LightAir("Light air"),
    /**
     * Light breeze
     */
    LightBreeze("Light breeze"),
    /**
     * Gentle breeze
     */
    GentleBreeze("Gentle breeze"),
    /**
     * Moderate breeze
     */
    ModerateBreeze("Moderate breeze"),
    /**
     * Fresh breeze
     */
    FreshBreeze("Fresh breeze"),
    /**
     * Strong breeze
     */
    StrongBreeze("Strong breeze"),
    /**
     * High wind
     */
    HighWind("High wind"),
    /**
     * Gale
     */
    Gale("Gale"),
    /**
     * Strong gale
     */
    StrongGale("Strong gale"),
    /**
     * Storm
     */
    Storm("Storm"),
    /**
     * Violent storm
     */
    ViolentStorm("Violent storm"),
    /**
     * Hurricane force
     */
    HurricaneForce("Hurricane force");
    private String description;

    BeaufortScale(String description)
    {
        this.description = description;
    }

    public String toString()
    {
        return description;
    }
}
