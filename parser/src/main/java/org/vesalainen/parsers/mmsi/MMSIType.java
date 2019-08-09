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
    NavigationalAid,
    /**
     * Unknown type
     */
    Unknown;
    /**
     * Returns MMSI type. This is fast method not fully parsing MMSI.
     * @param mmsi
     * @return 
     */
    public static MMSIType getType(int mmsi)
    {
        switch (mmsi / 100000000)
        {
            case 0:
                if (mmsi / 10000000 == 0)
                {
                    return CoastStation;
                }
                else
                {
                    return GroupShipStation;
                }
            case 1:
                return SarAircraft;
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                return ShipStation;
            case 8:
                return HandheldVHF;
            case 9:
                switch (mmsi / 10000000)
                {
                    case 99:
                        return NavigationalAid;
                    case 98:
                        return CraftAssociatedWithParentShip;
                    case 97:
                        switch (mmsi / 1000000)
                        {
                            case 970:
                                return SearchAndRescueTransponder;
                            case 972:
                                return MobDevice;
                            case 974:
                                return EPIRB;
                            default:
                                return Unknown;
                                
                        }
                    default:
                        return Unknown;
                }
        }
        return Unknown;
    }
}
