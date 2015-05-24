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
public enum CodesForShipType
{

    /**
     * Not available (default)
     */
    NotAvailableDefault("Not available (default)"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse("Reserved for future use"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse2("Reserved for future use"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse3("Reserved for future use"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse4("Reserved for future use"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse5("Reserved for future use"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse6("Reserved for future use"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse7("Reserved for future use"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse8("Reserved for future use"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse9("Reserved for future use"),
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
    ReservedForFutureUse15("Reserved for future use"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse16("Reserved for future use"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse17("Reserved for future use"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse18("Reserved for future use"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse19("Reserved for future use"),
    /**
     * Wing in ground (WIG), all ships of this type
     */
    WingInGroundWIGAllShipsOfThisType("Wing in ground (WIG), all ships of this type"),
    /**
     * Wing in ground (WIG), Hazardous category A
     */
    WingInGroundWIGHazardousCategoryA("Wing in ground (WIG), Hazardous category A"),
    /**
     * Wing in ground (WIG), Hazardous category B
     */
    WingInGroundWIGHazardousCategoryB("Wing in ground (WIG), Hazardous category B"),
    /**
     * Wing in ground (WIG), Hazardous category C
     */
    WingInGroundWIGHazardousCategoryC("Wing in ground (WIG), Hazardous category C"),
    /**
     * Wing in ground (WIG), Hazardous category D
     */
    WingInGroundWIGHazardousCategoryD("Wing in ground (WIG), Hazardous category D"),
    /**
     * Wing in ground (WIG), Reserved for future use
     */
    WingInGroundWIGReservedForFutureUse("Wing in ground (WIG), Reserved for future use"),
    /**
     * Wing in ground (WIG), Reserved for future use
     */
    WingInGroundWIGReservedForFutureUse26("Wing in ground (WIG), Reserved for future use"),
    /**
     * Wing in ground (WIG), Reserved for future use
     */
    WingInGroundWIGReservedForFutureUse27("Wing in ground (WIG), Reserved for future use"),
    /**
     * Wing in ground (WIG), Reserved for future use
     */
    WingInGroundWIGReservedForFutureUse28("Wing in ground (WIG), Reserved for future use"),
    /**
     * Wing in ground (WIG), Reserved for future use
     */
    WingInGroundWIGReservedForFutureUse29("Wing in ground (WIG), Reserved for future use"),
    /**
     * Fishing
     */
    Fishing("Fishing"),
    /**
     * Towing
     */
    Towing("Towing"),
    /**
     * Towing: length exceeds 200m or breadth exceeds 25m
     */
    TowingLengthExceeds200mOrBreadthExceeds25m("Towing: length exceeds 200m or breadth exceeds 25m"),
    /**
     * Dredging or underwater ops
     */
    DredgingOrUnderwaterOps("Dredging or underwater ops"),
    /**
     * Diving ops
     */
    DivingOps("Diving ops"),
    /**
     * Military ops
     */
    MilitaryOps("Military ops"),
    /**
     * Sailing
     */
    Sailing("Sailing"),
    /**
     * Pleasure Craft
     */
    PleasureCraft("Pleasure Craft"),
    /**
     * Reserved
     */
    Reserved("Reserved"),
    /**
     * Reserved
     */
    Reserved39("Reserved"),
    /**
     * High speed craft (HSC), all ships of this type
     */
    HighSpeedCraftHSCAllShipsOfThisType("High speed craft (HSC), all ships of this type"),
    /**
     * High speed craft (HSC), Hazardous category A
     */
    HighSpeedCraftHSCHazardousCategoryA("High speed craft (HSC), Hazardous category A"),
    /**
     * High speed craft (HSC), Hazardous category B
     */
    HighSpeedCraftHSCHazardousCategoryB("High speed craft (HSC), Hazardous category B"),
    /**
     * High speed craft (HSC), Hazardous category C
     */
    HighSpeedCraftHSCHazardousCategoryC("High speed craft (HSC), Hazardous category C"),
    /**
     * High speed craft (HSC), Hazardous category D
     */
    HighSpeedCraftHSCHazardousCategoryD("High speed craft (HSC), Hazardous category D"),
    /**
     * High speed craft (HSC), Reserved for future use
     */
    HighSpeedCraftHSCReservedForFutureUse("High speed craft (HSC), Reserved for future use"),
    /**
     * High speed craft (HSC), Reserved for future use
     */
    HighSpeedCraftHSCReservedForFutureUse46("High speed craft (HSC), Reserved for future use"),
    /**
     * High speed craft (HSC), Reserved for future use
     */
    HighSpeedCraftHSCReservedForFutureUse47("High speed craft (HSC), Reserved for future use"),
    /**
     * High speed craft (HSC), Reserved for future use
     */
    HighSpeedCraftHSCReservedForFutureUse48("High speed craft (HSC), Reserved for future use"),
    /**
     * High speed craft (HSC), No additional information
     */
    HighSpeedCraftHSCNoAdditionalInformation("High speed craft (HSC), No additional information"),
    /**
     * Pilot Vessel
     */
    PilotVessel("Pilot Vessel"),
    /**
     * Search and Rescue vessel
     */
    SearchAndRescueVessel("Search and Rescue vessel"),
    /**
     * Tug
     */
    Tug("Tug"),
    /**
     * Port Tender
     */
    PortTender("Port Tender"),
    /**
     * Anti-pollution equipment
     */
    AntiPollutionEquipment("Anti-pollution equipment"),
    /**
     * Law Enforcement
     */
    LawEnforcement("Law Enforcement"),
    /**
     * Spare - Local Vessel
     */
    SpareLocalVessel("Spare - Local Vessel"),
    /**
     * Spare - Local Vessel
     */
    SpareLocalVessel57("Spare - Local Vessel"),
    /**
     * Medical Transport
     */
    MedicalTransport("Medical Transport"),
    /**
     * Noncombatant ship according to RR Resolution No. 18
     */
    NoncombatantShipAccordingToRRResolutionNo18("Noncombatant ship according to RR Resolution No. 18"),
    /**
     * Passenger, all ships of this type
     */
    PassengerAllShipsOfThisType("Passenger, all ships of this type"),
    /**
     * Passenger, Hazardous category A
     */
    PassengerHazardousCategoryA("Passenger, Hazardous category A"),
    /**
     * Passenger, Hazardous category B
     */
    PassengerHazardousCategoryB("Passenger, Hazardous category B"),
    /**
     * Passenger, Hazardous category C
     */
    PassengerHazardousCategoryC("Passenger, Hazardous category C"),
    /**
     * Passenger, Hazardous category D
     */
    PassengerHazardousCategoryD("Passenger, Hazardous category D"),
    /**
     * Passenger, Reserved for future use
     */
    PassengerReservedForFutureUse("Passenger, Reserved for future use"),
    /**
     * Passenger, Reserved for future use
     */
    PassengerReservedForFutureUse66("Passenger, Reserved for future use"),
    /**
     * Passenger, Reserved for future use
     */
    PassengerReservedForFutureUse67("Passenger, Reserved for future use"),
    /**
     * Passenger, Reserved for future use
     */
    PassengerReservedForFutureUse68("Passenger, Reserved for future use"),
    /**
     * Passenger, No additional information
     */
    PassengerNoAdditionalInformation("Passenger, No additional information"),
    /**
     * Cargo, all ships of this type
     */
    CargoAllShipsOfThisType("Cargo, all ships of this type"),
    /**
     * Cargo, Hazardous category A
     */
    CargoHazardousCategoryA("Cargo, Hazardous category A"),
    /**
     * Cargo, Hazardous category B
     */
    CargoHazardousCategoryB("Cargo, Hazardous category B"),
    /**
     * Cargo, Hazardous category C
     */
    CargoHazardousCategoryC("Cargo, Hazardous category C"),
    /**
     * Cargo, Hazardous category D
     */
    CargoHazardousCategoryD("Cargo, Hazardous category D"),
    /**
     * Cargo, Reserved for future use
     */
    CargoReservedForFutureUse("Cargo, Reserved for future use"),
    /**
     * Cargo, Reserved for future use
     */
    CargoReservedForFutureUse76("Cargo, Reserved for future use"),
    /**
     * Cargo, Reserved for future use
     */
    CargoReservedForFutureUse77("Cargo, Reserved for future use"),
    /**
     * Cargo, Reserved for future use
     */
    CargoReservedForFutureUse78("Cargo, Reserved for future use"),
    /**
     * Cargo, No additional information
     */
    CargoNoAdditionalInformation("Cargo, No additional information"),
    /**
     * Tanker, all ships of this type
     */
    TankerAllShipsOfThisType("Tanker, all ships of this type"),
    /**
     * Tanker, Hazardous category A
     */
    TankerHazardousCategoryA("Tanker, Hazardous category A"),
    /**
     * Tanker, Hazardous category B
     */
    TankerHazardousCategoryB("Tanker, Hazardous category B"),
    /**
     * Tanker, Hazardous category C
     */
    TankerHazardousCategoryC("Tanker, Hazardous category C"),
    /**
     * Tanker, Hazardous category D
     */
    TankerHazardousCategoryD("Tanker, Hazardous category D"),
    /**
     * Tanker, Reserved for future use
     */
    TankerReservedForFutureUse("Tanker, Reserved for future use"),
    /**
     * Tanker, Reserved for future use
     */
    TankerReservedForFutureUse86("Tanker, Reserved for future use"),
    /**
     * Tanker, Reserved for future use
     */
    TankerReservedForFutureUse87("Tanker, Reserved for future use"),
    /**
     * Tanker, Reserved for future use
     */
    TankerReservedForFutureUse88("Tanker, Reserved for future use"),
    /**
     * Tanker, No additional information
     */
    TankerNoAdditionalInformation("Tanker, No additional information"),
    /**
     * Other Type, all ships of this type
     */
    OtherTypeAllShipsOfThisType("Other Type, all ships of this type"),
    /**
     * Other Type, Hazardous category A
     */
    OtherTypeHazardousCategoryA("Other Type, Hazardous category A"),
    /**
     * Other Type, Hazardous category B
     */
    OtherTypeHazardousCategoryB("Other Type, Hazardous category B"),
    /**
     * Other Type, Hazardous category C
     */
    OtherTypeHazardousCategoryC("Other Type, Hazardous category C"),
    /**
     * Other Type, Hazardous category D
     */
    OtherTypeHazardousCategoryD("Other Type, Hazardous category D"),
    /**
     * Other Type, Reserved for future use
     */
    OtherTypeReservedForFutureUse("Other Type, Reserved for future use"),
    /**
     * Other Type, Reserved for future use
     */
    OtherTypeReservedForFutureUse96("Other Type, Reserved for future use"),
    /**
     * Other Type, Reserved for future use
     */
    OtherTypeReservedForFutureUse97("Other Type, Reserved for future use"),
    /**
     * Other Type, Reserved for future use
     */
    OtherTypeReservedForFutureUse98("Other Type, Reserved for future use"),
    /**
     * Other Type, no additional information
     */
    OtherTypeNoAdditionalInformation("Other Type, no additional information");
    private String description;

    CodesForShipType(String description)
    {
        this.description = description;
    }

    public String toString()
    {
        return description;
    }
}
