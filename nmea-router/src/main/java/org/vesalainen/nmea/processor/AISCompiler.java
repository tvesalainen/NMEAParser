/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nmea.processor;

import org.vesalainen.code.AnnotatedPropertyStore;
import org.vesalainen.parsers.nmea.NMEAPGN;
import static org.vesalainen.parsers.nmea.NMEAPGN.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AISCompiler extends AbstractNMEACompiler
{
    
    public <T extends AnnotatedPropertyStore> AISCompiler(T store)
    {
        super(store);
        
        addPgnSetter(AIS_CLASS_A_POSITION_REPORT, "Message_Id", "message");
        addPgnSetter(AIS_CLASS_A_POSITION_REPORT, "Repeat_Indicator", "repeat");
        addPgnSetter(AIS_CLASS_A_POSITION_REPORT, "User_Id", "mmsi");
        addPgnSetter(AIS_CLASS_A_POSITION_REPORT, "Longitude", "longitude");
        addPgnSetter(AIS_CLASS_A_POSITION_REPORT, "Latitude", "latitude");
        addPgnSetter(AIS_CLASS_A_POSITION_REPORT, "Position_Accuracy", "positionAccuracy");
        addPgnSetter(AIS_CLASS_A_POSITION_REPORT, "Raim_Flag", "raim");
        addPgnSetter(AIS_CLASS_A_POSITION_REPORT, "Time_Stamp", "second");
        addPgnSetter(AIS_CLASS_A_POSITION_REPORT, "Reserved_For_Regional_Applications", "maneuver");
        addPgnSetter(AIS_CLASS_A_POSITION_REPORT, "Cog", "course");
        addPgnSetter(AIS_CLASS_A_POSITION_REPORT, "Sog", "speed");
        addPgnSetter(AIS_CLASS_A_POSITION_REPORT, "Communication_State", "radioStatus");
        addPgnSetter(AIS_CLASS_A_POSITION_REPORT, "True_Heading", "heading");
        addPgnSetter(AIS_CLASS_A_POSITION_REPORT, "Rate_Of_Turn", "rateOfTurn");
        addPgnSetter(AIS_CLASS_A_POSITION_REPORT, "Navigational_Status", "navigationStatus");
        
        addPgnSetter(AIS_CLASS_A_STATIC_AND_VOYAGE_RELATED_DATA, "Message_Id", "message");
        addPgnSetter(AIS_CLASS_A_STATIC_AND_VOYAGE_RELATED_DATA, "Repeat_Indicator", "repeat");
        addPgnSetter(AIS_CLASS_A_STATIC_AND_VOYAGE_RELATED_DATA, "User_Id", "mmsi");
        addPgnSetter(AIS_CLASS_A_STATIC_AND_VOYAGE_RELATED_DATA, "Imo", "imoNumber");
        addPgnSetter(AIS_CLASS_A_STATIC_AND_VOYAGE_RELATED_DATA, "Call_Sign", "callSign");
        addPgnSetter(AIS_CLASS_A_STATIC_AND_VOYAGE_RELATED_DATA, "Name", "vesselName");
        addPgnSetter(AIS_CLASS_A_STATIC_AND_VOYAGE_RELATED_DATA, "Ship_Cargo_Type", "shipType");
        addPgnSetter(AIS_CLASS_A_STATIC_AND_VOYAGE_RELATED_DATA, "Ship_Length", "shipLength");
        addPgnSetter(AIS_CLASS_A_STATIC_AND_VOYAGE_RELATED_DATA, "Ship_Beam", "shipBeam");
        addPgnSetter(AIS_CLASS_A_STATIC_AND_VOYAGE_RELATED_DATA, "Position_Reference_Point_From_Starboard", "positionReferencePointFromStarboard");
        addPgnSetter(AIS_CLASS_A_STATIC_AND_VOYAGE_RELATED_DATA, "Position_Reference_Point_Aft_Of_Ship_S_Bow", "positionReferencePointAftOfShipSBow");
        addPgnSetter(AIS_CLASS_A_STATIC_AND_VOYAGE_RELATED_DATA, "Estimated_Date_Of_Arrival", "estimatedDateOfArrival");
        addPgnSetter(AIS_CLASS_A_STATIC_AND_VOYAGE_RELATED_DATA, "Estimated_Time_Of_Arrival", "estimatedTimeOfArrival");
        addPgnSetter(AIS_CLASS_A_STATIC_AND_VOYAGE_RELATED_DATA, "Draft", "draught");
        addPgnSetter(AIS_CLASS_A_STATIC_AND_VOYAGE_RELATED_DATA, "Destination", "destination");
        addPgnSetter(AIS_CLASS_A_STATIC_AND_VOYAGE_RELATED_DATA, "Ais_Version", "aisVersion");
        addPgnSetter(AIS_CLASS_A_STATIC_AND_VOYAGE_RELATED_DATA, "Type_Of_Electronic_Positioning_Device", "epfd");
        addPgnSetter(AIS_CLASS_A_STATIC_AND_VOYAGE_RELATED_DATA, "Data_Terminal_Equipment_Dte", "dte");
        //addPgnSetter(AIS_CLASS_A_STATIC_AND_VOYAGE_RELATED_DATA, "Ais_Transceiver_Information", "");
        
    }
    
}
