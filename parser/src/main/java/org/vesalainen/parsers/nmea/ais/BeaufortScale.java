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
    Calm("Calm", "Flat."),
    /**
     * Light air
     */
    LightAir("Light air", "Ripples without crests."),
    /**
     * Light breeze
     */
    LightBreeze("Light breeze", "Small wavelets. Crests of glassy appearance, not breaking."),
    /**
     * Gentle breeze
     */
    GentleBreeze("Gentle breeze", "Large wavelets. Crests begin to break; scattered whitecaps."),
    /**
     * Moderate breeze
     */
    ModerateBreeze("Moderate breeze", "Small waves."),
    /**
     * Fresh breeze
     */
    FreshBreeze("Fresh breeze", "Moderate (1.2 m) longer waves. Some foam and spray."),
    /**
     * Strong breeze
     */
    StrongBreeze("Strong breeze", "Large waves with foam crests and some spray."),
    /**
     * High wind
     */
    HighWind("High wind", "Sea heaps up and foam begins to streak."),
    /**
     * Gale
     */
    Gale("Gale", "Moderately high waves with breaking crests forming spindrift. Streaks of foam."),
    /**
     * Strong gale
     */
    StrongGale("Strong gale", "High waves (6-7 m) with dense foam. Wave crests start to roll over. Considerable spray."),
    /**
     * Storm
     */
    Storm("Storm", "Very high waves. The sea surface is white and there is considerable tumbling. Visibility is reduced."),
    /**
     * Violent storm
     */
    ViolentStorm("Violent storm", "Exceptionally high waves."),
    /**
     * Hurricane force
     */
    HurricaneForce("Hurricane force", "Huge waves. Air filled with foam and spray. Sea completely white with driving spray. Visibility greatly reduced."),
    NADefault("N/A Default", ""),
    Reserved14("Reserved14", ""),
    Reserved15("Reserved15", "");
    
    private final String description;
    private final String seaConditions;

    private BeaufortScale(String description, String seaConditions)
    {
        this.description = description;
        this.seaConditions = seaConditions;
    }

    public String toString()
    {
        return description;
    }

    public String getSeaConditions()
    {
        return seaConditions;
    }
    
}
