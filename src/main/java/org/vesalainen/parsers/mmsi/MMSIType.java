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
 *
 * @author Timo Vesalainen
 */
public enum MMSIType
{
    /**
     * The 9-digit code constituting a ship station identity
     */
    ShipStation,
    /**
     * Group ship station call identities for calling simultaneously more than one ship
     */
    GroupShipStation,
    /**
     * Coast station identity
     */
    CoastStation,
    /**
     * SAR aircraft
     */
    SarAircraft,
    /**
     * Handheld VHF transceiver with DSC and GNSS
     */
    HandheldVHF,
    /**
     * Search and Rescue Transponder
     */
    SearchAndRescueTransponder, // S.A.R.T, ...
    /**
     * Man overboard DSC and/or AIS device
     */
    MobDevice,
    /**
     * 406 MHz EPIRBs fitted with an AIS transmitter
     */
    EPIRB,
    /**
     * Craft associated with a parent ship
     */
    CraftAssociatedWithParentShip,  // life-rafts,...
    /**
     * Navigational aid
     */
    NavigationalAid
    
}
