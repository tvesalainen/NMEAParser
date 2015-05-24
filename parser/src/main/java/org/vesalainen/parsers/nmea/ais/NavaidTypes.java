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
public enum NavaidTypes
{

    /**
     * Default, Type of Aid to Navigation not specified
     */
    DefaultTypeOfAidToNavigationNotSpecified("Default, Type of Aid to Navigation not specified"),
    /**
     * Reference point
     */
    ReferencePoint("Reference point"),
    /**
     * RACON (radar transponder marking a navigation hazard)
     */
    RACONRadarTransponderMarkingANavigationHazard("RACON (radar transponder marking a navigation hazard)"),
    /**
     * Fixed structure off shore, such as oil platforms, wind farms, rigs.
     * (Note: This code should identify an obstruction that is fitted with an
     * Aid-to-Navigation AIS station.)
     */
    FixedStructureOffShoreSuchAsOilPlatformsWindFarmsRigsNoteThisCodeShouldIdentifyAnObstructionThatIsFittedWithAnAidToNavigationAISStation("Fixed structure off shore, such as oil platforms, wind farms, rigs. (Note: This code should identify an obstruction that is fitted with an Aid-to-Navigation AIS station.)"),
    /**
     * Spare, Reserved for future use.
     */
    SpareReservedForFutureUse("Spare, Reserved for future use."),
    /**
     * Light, without sectors
     */
    LightWithoutSectors("Light, without sectors"),
    /**
     * Light, with sectors
     */
    LightWithSectors("Light, with sectors"),
    /**
     * Leading Light Front
     */
    LeadingLightFront("Leading Light Front"),
    /**
     * Leading Light Rear
     */
    LeadingLightRear("Leading Light Rear"),
    /**
     * Beacon, Cardinal N
     */
    BeaconCardinalN("Beacon, Cardinal N"),
    /**
     * Beacon, Cardinal E
     */
    BeaconCardinalE("Beacon, Cardinal E"),
    /**
     * Beacon, Cardinal S
     */
    BeaconCardinalS("Beacon, Cardinal S"),
    /**
     * Beacon, Cardinal W
     */
    BeaconCardinalW("Beacon, Cardinal W"),
    /**
     * Beacon, Port hand
     */
    BeaconPortHand("Beacon, Port hand"),
    /**
     * Beacon, Starboard hand
     */
    BeaconStarboardHand("Beacon, Starboard hand"),
    /**
     * Beacon, Preferred Channel port hand
     */
    BeaconPreferredChannelPortHand("Beacon, Preferred Channel port hand"),
    /**
     * Beacon, Preferred Channel starboard hand
     */
    BeaconPreferredChannelStarboardHand("Beacon, Preferred Channel starboard hand"),
    /**
     * Beacon, Isolated danger
     */
    BeaconIsolatedDanger("Beacon, Isolated danger"),
    /**
     * Beacon, Safe water
     */
    BeaconSafeWater("Beacon, Safe water"),
    /**
     * Beacon, Special mark
     */
    BeaconSpecialMark("Beacon, Special mark"),
    /**
     * Cardinal Mark N
     */
    CardinalMarkN("Cardinal Mark N"),
    /**
     * Cardinal Mark E
     */
    CardinalMarkE("Cardinal Mark E"),
    /**
     * Cardinal Mark S
     */
    CardinalMarkS("Cardinal Mark S"),
    /**
     * Cardinal Mark W
     */
    CardinalMarkW("Cardinal Mark W"),
    /**
     * Port hand Mark
     */
    PortHandMark("Port hand Mark"),
    /**
     * Starboard hand Mark
     */
    StarboardHandMark("Starboard hand Mark"),
    /**
     * Preferred Channel Port hand
     */
    PreferredChannelPortHand("Preferred Channel Port hand"),
    /**
     * Preferred Channel Starboard hand
     */
    PreferredChannelStarboardHand("Preferred Channel Starboard hand"),
    /**
     * Isolated danger
     */
    IsolatedDanger("Isolated danger"),
    /**
     * Safe Water
     */
    SafeWater("Safe Water"),
    /**
     * Special Mark
     */
    SpecialMark("Special Mark"),
    /**
     * Light Vessel / LANBY / Rigs
     */
    LightVesselLANBYRigs("Light Vessel / LANBY / Rigs");
    private String description;

    NavaidTypes(String description)
    {
        this.description = description;
    }

    @Override
    public String toString()
    {
        return description;
    }
}
