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
public enum SensorOwnerCodes
{

    /**
     * Unknown (default)
     */
    UnknownDefault("Unknown (default)"),
    /**
     * Hydrographic office
     */
    HydrographicOffice("Hydrographic office"),
    /**
     * Inland waterway authority
     */
    InlandWaterwayAuthority("Inland waterway authority"),
    /**
     * Coastal directorate
     */
    CoastalDirectorate("Coastal directorate"),
    /**
     * Meteorological service
     */
    MeteorologicalService("Meteorological service"),
    /**
     * Port Authority
     */
    PortAuthority("Port Authority"),
    /**
     * Coast guard
     */
    CoastGuard("Coast guard"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse("(reserved for future use)"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse8("(reserved for future use)"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse9("(reserved for future use)"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse10("(reserved for future use)"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse11("(reserved for future use)"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse12("(reserved for future use)"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse13("(reserved for future use)"),
    /**
     * (reserved for regional use)
     */
    ReservedForRegionalUse("(reserved for regional use)");
    private String description;

    SensorOwnerCodes(String description)
    {
        this.description = description;
    }

    @Override
    public String toString()
    {
        return description;
    }
}
