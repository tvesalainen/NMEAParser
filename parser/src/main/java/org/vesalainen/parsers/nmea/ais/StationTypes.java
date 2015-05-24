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
public enum StationTypes
{

    /**
     * All types of mobiles (default)
     */
    AllTypesOfMobilesDefault("All types of mobiles (default)"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse("Reserved for future use"),
    /**
     * All types of Class B mobile stations
     */
    AllTypesOfClassBMobileStations("All types of Class B mobile stations"),
    /**
     * SAR airborne mobile station
     */
    SARAirborneMobileStation("SAR airborne mobile station"),
    /**
     * Aid to Navigation station
     */
    AidToNavigationStation("Aid to Navigation station"),
    /**
     * Class B shipborne mobile station (IEC62287 only)
     */
    ClassBShipborneMobileStationIEC62287Only("Class B shipborne mobile station (IEC62287 only)"),
    /**
     * Regional use and inland waterways
     */
    RegionalUseAndInlandWaterways("Regional use and inland waterways"),
    /**
     * Regional use and inland waterways
     */
    RegionalUseAndInlandWaterways7("Regional use and inland waterways"),
    /**
     * Regional use and inland waterways
     */
    RegionalUseAndInlandWaterways8("Regional use and inland waterways"),
    /**
     * Regional use and inland waterways
     */
    RegionalUseAndInlandWaterways9("Regional use and inland waterways"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse10("Reserved for future use"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse11("Reserved for future use"),
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

    StationTypes(String description)
    {
        this.description = description;
    }

    @Override
    public String toString()
    {
        return description;
    }
}
