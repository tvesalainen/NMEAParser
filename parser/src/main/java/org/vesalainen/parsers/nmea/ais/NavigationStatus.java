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
public enum NavigationStatus
{

    /**
     * Under way using engine
     */
    UnderWayUsingEngine("Under way using engine"),
    /**
     * At anchor
     */
    AtAnchor("At anchor"),
    /**
     * Not under command
     */
    NotUnderCommand("Not under command"),
    /**
     * Restricted manoeuverability
     */
    RestrictedManoeuverability("Restricted manoeuverability"),
    /**
     * Constrained by her draught
     */
    ConstrainedByHerDraught("Constrained by her draught"),
    /**
     * Moored
     */
    Moored("Moored"),
    /**
     * Aground
     */
    Aground("Aground"),
    /**
     * Engaged in Fishing
     */
    EngagedInFishing("Engaged in Fishing"),
    /**
     * Under way sailing
     */
    UnderWaySailing("Under way sailing"),
    /**
     * Reserved for future amendment of Navigational Status for HSC
     */
    ReservedForFutureAmendmentOfNavigationalStatusForHSC("Reserved for future amendment of Navigational Status for HSC"),
    /**
     * Reserved for future amendment of Navigational Status for WIG
     */
    ReservedForFutureAmendmentOfNavigationalStatusForWIG("Reserved for future amendment of Navigational Status for WIG"),
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
     * Not defined (default)
     */
    NotDefinedDefault("Not defined");
    private String description;

    NavigationStatus(String description)
    {
        this.description = description;
    }

    public String getDescription()
    {
        return description;
    }
}
