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
public enum AreaNoticeDescription
{

    /**
     * Caution Area: Marine mammals habitat
     */
    CautionAreaMarineMammalsHabitat("Caution Area: Marine mammals habitat"),
    /**
     * Caution Area: Marine mammals in area - reduce speed
     */
    CautionAreaMarineMammalsInAreaReduceSpeed("Caution Area: Marine mammals in area - reduce speed"),
    /**
     * Caution Area: Marine mammals in area - stay clear
     */
    CautionAreaMarineMammalsInAreaStayClear("Caution Area: Marine mammals in area - stay clear"),
    /**
     * Caution Area: Marine mammals in area - report sightings
     */
    CautionAreaMarineMammalsInAreaReportSightings("Caution Area: Marine mammals in area - report sightings"),
    /**
     * Caution Area: Protected habitat - reduce speed
     */
    CautionAreaProtectedHabitatReduceSpeed("Caution Area: Protected habitat - reduce speed"),
    /**
     * Caution Area: Protected habitat - stay clear
     */
    CautionAreaProtectedHabitatStayClear("Caution Area: Protected habitat - stay clear"),
    /**
     * Caution Area: Protected habitat - no fisging or anchoring
     */
    CautionAreaProtectedHabitatNoFisgingOrAnchoring("Caution Area: Protected habitat - no fisging or anchoring"),
    /**
     * Caution Area: Derelicts (drifting objects)
     */
    CautionAreaDerelictsDriftingObjects("Caution Area: Derelicts (drifting objects)"),
    /**
     * Caution Area: Traffic congestion
     */
    CautionAreaTrafficCongestion("Caution Area: Traffic congestion"),
    /**
     * Caution Area: Marine event
     */
    CautionAreaMarineEvent("Caution Area: Marine event"),
    /**
     * Caution Area: Divers down
     */
    CautionAreaDiversDown("Caution Area: Divers down"),
    /**
     * Caution Area: Swim area
     */
    CautionAreaSwimArea("Caution Area: Swim area"),
    /**
     * Caution Area: Dredge operations
     */
    CautionAreaDredgeOperations("Caution Area: Dredge operations"),
    /**
     * Caution Area: Survey operations
     */
    CautionAreaSurveyOperations("Caution Area: Survey operations"),
    /**
     * Caution Area: Underwater operation
     */
    CautionAreaUnderwaterOperation("Caution Area: Underwater operation"),
    /**
     * Caution Area: Seaplane operations
     */
    CautionAreaSeaplaneOperations("Caution Area: Seaplane operations"),
    /**
     * Caution Area: Fishery ἓ nets in water
     */
    CautionAreaFisheryἛNetsInWater("Caution Area: Fishery ἓ nets in water"),
    /**
     * Caution Area: Cluster of fishing vessels
     */
    CautionAreaClusterOfFishingVessels("Caution Area: Cluster of fishing vessels"),
    /**
     * Caution Area: Fairway closed
     */
    CautionAreaFairwayClosed("Caution Area: Fairway closed"),
    /**
     * Caution Area: Harbour closed
     */
    CautionAreaHarbourClosed("Caution Area: Harbour closed"),
    /**
     * Caution Area: Risk (define in associated text field)
     */
    CautionAreaRiskDefineInAssociatedTextField("Caution Area: Risk (define in associated text field)"),
    /**
     * Caution Area: Underwater vehicle operation
     */
    CautionAreaUnderwaterVehicleOperation("Caution Area: Underwater vehicle operation"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse("(reserved for future use)"),
    /**
     * Environmental Caution Area: Storm front (line squall)
     */
    EnvironmentalCautionAreaStormFrontLineSquall("Environmental Caution Area: Storm front (line squall)"),
    /**
     * Environmental Caution Area: Hazardous sea ice
     */
    EnvironmentalCautionAreaHazardousSeaIce("Environmental Caution Area: Hazardous sea ice"),
    /**
     * Environmental Caution Area: Storm warning (storm cell or line of storms)
     */
    EnvironmentalCautionAreaStormWarningStormCellOrLineOfStorms("Environmental Caution Area: Storm warning (storm cell or line of storms)"),
    /**
     * Environmental Caution Area: High wind
     */
    EnvironmentalCautionAreaHighWind("Environmental Caution Area: High wind"),
    /**
     * Environmental Caution Area: High waves
     */
    EnvironmentalCautionAreaHighWaves("Environmental Caution Area: High waves"),
    /**
     * Environmental Caution Area: Restricted visibility (fog, rain, etc.)
     */
    EnvironmentalCautionAreaRestrictedVisibilityFogRainEtc("Environmental Caution Area: Restricted visibility (fog, rain, etc.)"),
    /**
     * Environmental Caution Area: Strong currents
     */
    EnvironmentalCautionAreaStrongCurrents("Environmental Caution Area: Strong currents"),
    /**
     * Environmental Caution Area: Heavy icing
     */
    EnvironmentalCautionAreaHeavyIcing("Environmental Caution Area: Heavy icing"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse31("(reserved for future use)"),
    /**
     * Restricted Area: Fishing prohibited
     */
    RestrictedAreaFishingProhibited("Restricted Area: Fishing prohibited"),
    /**
     * Restricted Area: No anchoring.
     */
    RestrictedAreaNoAnchoring("Restricted Area: No anchoring."),
    /**
     * Restricted Area: Entry approval required prior to transit
     */
    RestrictedAreaEntryApprovalRequiredPriorToTransit("Restricted Area: Entry approval required prior to transit"),
    /**
     * Restricted Area: Entry prohibited
     */
    RestrictedAreaEntryProhibited("Restricted Area: Entry prohibited"),
    /**
     * Restricted Area: Active military OPAREA
     */
    RestrictedAreaActiveMilitaryOPAREA("Restricted Area: Active military OPAREA"),
    /**
     * Restricted Area: Firing ἓ danger area.
     */
    RestrictedAreaFiringἛDangerArea("Restricted Area: Firing ἓ danger area."),
    /**
     * Restricted Area: Drifting Mines
     */
    RestrictedAreaDriftingMines("Restricted Area: Drifting Mines"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse39("(reserved for future use)"),
    /**
     * Anchorage Area: Anchorage open
     */
    AnchorageAreaAnchorageOpen("Anchorage Area: Anchorage open"),
    /**
     * Anchorage Area: Anchorage closed
     */
    AnchorageAreaAnchorageClosed("Anchorage Area: Anchorage closed"),
    /**
     * Anchorage Area: Anchorage prohibited
     */
    AnchorageAreaAnchorageProhibited("Anchorage Area: Anchorage prohibited"),
    /**
     * Anchorage Area: Deep draft anchorage
     */
    AnchorageAreaDeepDraftAnchorage("Anchorage Area: Deep draft anchorage"),
    /**
     * Anchorage Area: Shallow draft anchorage
     */
    AnchorageAreaShallowDraftAnchorage("Anchorage Area: Shallow draft anchorage"),
    /**
     * Anchorage Area: Vessel transfer operations
     */
    AnchorageAreaVesselTransferOperations("Anchorage Area: Vessel transfer operations"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse46("(reserved for future use)"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse47("(reserved for future use)"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse48("(reserved for future use)"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse49("(reserved for future use)"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse50("(reserved for future use)"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse51("(reserved for future use)"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse52("(reserved for future use)"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse53("(reserved for future use)"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse54("(reserved for future use)"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse55("(reserved for future use)"),
    /**
     * Security Alert - Level 1
     */
    SecurityAlertLevel1("Security Alert - Level 1"),
    /**
     * Security Alert - Level 2
     */
    SecurityAlertLevel2("Security Alert - Level 2"),
    /**
     * Security Alert - Level 3
     */
    SecurityAlertLevel3("Security Alert - Level 3"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse59("(reserved for future use)"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse60("(reserved for future use)"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse61("(reserved for future use)"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse62("(reserved for future use)"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse63("(reserved for future use)"),
    /**
     * Distress Area: Vessel disabled and adrift
     */
    DistressAreaVesselDisabledAndAdrift("Distress Area: Vessel disabled and adrift"),
    /**
     * Distress Area: Vessel sinking
     */
    DistressAreaVesselSinking("Distress Area: Vessel sinking"),
    /**
     * Distress Area: Vessel abandoning ship
     */
    DistressAreaVesselAbandoningShip("Distress Area: Vessel abandoning ship"),
    /**
     * Distress Area: Vessel requests medical assistance
     */
    DistressAreaVesselRequestsMedicalAssistance("Distress Area: Vessel requests medical assistance"),
    /**
     * Distress Area: Vessel flooding
     */
    DistressAreaVesselFlooding("Distress Area: Vessel flooding"),
    /**
     * Distress Area: Vessel fire/explosion
     */
    DistressAreaVesselFireExplosion("Distress Area: Vessel fire/explosion"),
    /**
     * Distress Area: Vessel grounding
     */
    DistressAreaVesselGrounding("Distress Area: Vessel grounding"),
    /**
     * Distress Area: Vessel collision
     */
    DistressAreaVesselCollision("Distress Area: Vessel collision"),
    /**
     * Distress Area: Vessel listing/capsizing
     */
    DistressAreaVesselListingCapsizing("Distress Area: Vessel listing/capsizing"),
    /**
     * Distress Area: Vessel under assault
     */
    DistressAreaVesselUnderAssault("Distress Area: Vessel under assault"),
    /**
     * Distress Area: Person overboard
     */
    DistressAreaPersonOverboard("Distress Area: Person overboard"),
    /**
     * Distress Area: SAR area
     */
    DistressAreaSARArea("Distress Area: SAR area"),
    /**
     * Distress Area: Pollution response area
     */
    DistressAreaPollutionResponseArea("Distress Area: Pollution response area"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse77("(reserved for future use)"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse78("(reserved for future use)"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse79("(reserved for future use)"),
    /**
     * Instruction: Contact VTS at this point/juncture
     */
    InstructionContactVTSAtThisPointJuncture("Instruction: Contact VTS at this point/juncture"),
    /**
     * Instruction: Contact Port Administration at this point/juncture
     */
    InstructionContactPortAdministrationAtThisPointJuncture("Instruction: Contact Port Administration at this point/juncture"),
    /**
     * Instruction: Do not proceed beyond this point/juncture
     */
    InstructionDoNotProceedBeyondThisPointJuncture("Instruction: Do not proceed beyond this point/juncture"),
    /**
     * Instruction: Await instructions prior to proceeding beyond this
     * point/juncture
     */
    InstructionAwaitInstructionsPriorToProceedingBeyondThisPointJuncture("Instruction: Await instructions prior to proceeding beyond this point/juncture"),
    /**
     * Proceed to this location ἓ await instructions
     */
    ProceedToThisLocationἛAwaitInstructions("Proceed to this location ἓ await instructions"),
    /**
     * Clearance granted ἓ proceed to berth
     */
    ClearanceGrantedἛProceedToBerth("Clearance granted ἓ proceed to berth"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse86("(reserved for future use)"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse87("(reserved for future use)"),
    /**
     * Information: Pilot boarding position
     */
    InformationPilotBoardingPosition("Information: Pilot boarding position"),
    /**
     * Information: Icebreaker waiting area
     */
    InformationIcebreakerWaitingArea("Information: Icebreaker waiting area"),
    /**
     * Information: Places of refuge
     */
    InformationPlacesOfRefuge("Information: Places of refuge"),
    /**
     * Information: Position of icebreakers
     */
    InformationPositionOfIcebreakers("Information: Position of icebreakers"),
    /**
     * Information: Location of response units
     */
    InformationLocationOfResponseUnits("Information: Location of response units"),
    /**
     * VTS active target
     */
    VTSActiveTarget("VTS active target"),
    /**
     * Rogue or suspicious vessel
     */
    RogueOrSuspiciousVessel("Rogue or suspicious vessel"),
    /**
     * Vessel requesting non-distress assistance
     */
    VesselRequestingNonDistressAssistance("Vessel requesting non-distress assistance"),
    /**
     * Chart Feature: Sunken vessel
     */
    ChartFeatureSunkenVessel("Chart Feature: Sunken vessel"),
    /**
     * Chart Feature: Submerged object
     */
    ChartFeatureSubmergedObject("Chart Feature: Submerged object"),
    /**
     * Chart Feature:Semi-submerged object
     */
    ChartFeatureSemiSubmergedObject("Chart Feature:Semi-submerged object"),
    /**
     * Chart Feature: Shoal area
     */
    ChartFeatureShoalArea("Chart Feature: Shoal area"),
    /**
     * Chart Feature: Shoal area due north
     */
    ChartFeatureShoalAreaDueNorth("Chart Feature: Shoal area due north"),
    /**
     * Chart Feature: Shoal area due east
     */
    ChartFeatureShoalAreaDueEast("Chart Feature: Shoal area due east"),
    /**
     * Chart Feature: Shoal area due south
     */
    ChartFeatureShoalAreaDueSouth("Chart Feature: Shoal area due south"),
    /**
     * Chart Feature: Shoal area due west
     */
    ChartFeatureShoalAreaDueWest("Chart Feature: Shoal area due west"),
    /**
     * Chart Feature: Channel obstruction
     */
    ChartFeatureChannelObstruction("Chart Feature: Channel obstruction"),
    /**
     * Chart Feature: Reduced vertical clearance
     */
    ChartFeatureReducedVerticalClearance("Chart Feature: Reduced vertical clearance"),
    /**
     * Chart Feature: Bridge closed
     */
    ChartFeatureBridgeClosed("Chart Feature: Bridge closed"),
    /**
     * Chart Feature: Bridge partially open
     */
    ChartFeatureBridgePartiallyOpen("Chart Feature: Bridge partially open"),
    /**
     * Chart Feature: Bridge fully open
     */
    ChartFeatureBridgeFullyOpen("Chart Feature: Bridge fully open"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse109("(reserved for future use)"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse110("(reserved for future use)"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse111("(reserved for future use)"),
    /**
     * Report from ship: Icing info
     */
    ReportFromShipIcingInfo("Report from ship: Icing info"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse113("(reserved for future use)"),
    /**
     * Report from ship: Miscellaneous information ἓ define in associated text
     * field
     */
    ReportFromShipMiscellaneousInformationἛDefineInAssociatedTextField("Report from ship: Miscellaneous information ἓ define in associated text field"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse115("(reserved for future use)"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse116("(reserved for future use)"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse117("(reserved for future use)"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse118("(reserved for future use)"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse119("(reserved for future use)"),
    /**
     * Route: Recommended route
     */
    RouteRecommendedRoute("Route: Recommended route"),
    /**
     * Route: Alternative route
     */
    RouteAlternativeRoute("Route: Alternative route"),
    /**
     * Route: Recommended route through ice
     */
    RouteRecommendedRouteThroughIce("Route: Recommended route through ice"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse123("(reserved for future use)"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse124("(reserved for future use)"),
    /**
     * Other ἓ Define in associated text field
     */
    OtherἛDefineInAssociatedTextField("Other ἓ Define in associated text field"),
    /**
     * Cancellation ἓ cancel area as identified by Message Linkage ID
     */
    CancellationἛCancelAreaAsIdentifiedByMessageLinkageID("Cancellation ἓ cancel area as identified by Message Linkage ID"),
    /**
     * Undefined (default)
     */
    UndefinedDefault("Undefined (default)");
    private String description;

    AreaNoticeDescription(String description)
    {
        this.description = description;
    }

    @Override
    public String toString()
    {
        return description;
    }
}
