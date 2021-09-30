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
package org.vesalainen.nmea.processor.n2kgw;

import java.util.function.Supplier;
import org.vesalainen.can.dbc.MessageClass;
import org.vesalainen.code.AnnotatedPropertyStore;
import static org.vesalainen.parsers.nmea.NMEAPGN.*;
import org.vesalainen.util.HexUtil;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AISCompiler extends AbstractNMEACompiler
{
    private JavaLogging logger = JavaLogging.getLogger(AISCompiler.class);

    private boolean needCompilation;
    
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
        addPgnSetter(AIS_CLASS_A_POSITION_REPORT, "Ais_Transceiver_Information", "transceiverInformation");
        
        addPgnSetter(AIS_CLASS_B_POSITION_REPORT, "Message_Id", "message");
        addPgnSetter(AIS_CLASS_B_POSITION_REPORT, "Repeat_Indicator", "repeat");
        addPgnSetter(AIS_CLASS_B_POSITION_REPORT, "User_Id", "mmsi");
        addPgnSetter(AIS_CLASS_B_POSITION_REPORT, "Longitude", "longitude");
        addPgnSetter(AIS_CLASS_B_POSITION_REPORT, "Latitude", "latitude");
        addPgnSetter(AIS_CLASS_B_POSITION_REPORT, "Position_Accuracy", "positionAccuracy");
        addPgnSetter(AIS_CLASS_B_POSITION_REPORT, "Raim_Flag", "raim");
        addPgnSetter(AIS_CLASS_B_POSITION_REPORT, "Time_Stamp", "second");
        addPgnSetter(AIS_CLASS_B_POSITION_REPORT, "Cog", "course");
        addPgnSetter(AIS_CLASS_B_POSITION_REPORT, "Sog", "speed");
        addPgnSetter(AIS_CLASS_B_POSITION_REPORT, "Communication_State", "radioStatus");
        addPgnSetter(AIS_CLASS_B_POSITION_REPORT, "True_Heading", "heading");
        addPgnSetter(AIS_CLASS_B_POSITION_REPORT, "Reserved_For_Regional_Applications", "maneuver");
        addPgnSetter(AIS_CLASS_B_POSITION_REPORT, "Class_B_Unit_Flag", "csUnit");
        addPgnSetter(AIS_CLASS_B_POSITION_REPORT, "Class_B_Display_Flag", "display");
        addPgnSetter(AIS_CLASS_B_POSITION_REPORT, "Class_B_Dsc_Flag", "dsc");
        addPgnSetter(AIS_CLASS_B_POSITION_REPORT, "Class_B_Band_Flag", "band");
        addPgnSetter(AIS_CLASS_B_POSITION_REPORT, "Class_B_Msg_22_Flag", "msg22");
        addPgnSetter(AIS_CLASS_B_POSITION_REPORT, "Mode_Flag", "assignedMode");
        //addPgnSetter(AIS_CLASS_B_POSITION_REPORT, "Communication_State_Selector_Flag", "");
        addPgnSetter(AIS_CLASS_B_POSITION_REPORT, "Ais_Transceiver_Information", "transceiverInformation");

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
        addPgnSetter(AIS_CLASS_A_STATIC_AND_VOYAGE_RELATED_DATA, "Ais_Transceiver_Information", "transceiverInformation");
        
        addPgnSetter(AIS_CLASS_B_CS_STATIC_REPORT_PART_A, "Message_Id", "message");
        addPgnSetter(AIS_CLASS_B_CS_STATIC_REPORT_PART_A, "Repeat_Indicator", "repeat");
        addPgnSetter(AIS_CLASS_B_CS_STATIC_REPORT_PART_A, "User_Id", "mmsi");
        addPgnSetter(AIS_CLASS_B_CS_STATIC_REPORT_PART_A, "Name", "vesselName");
        addPgnSetter(AIS_CLASS_B_CS_STATIC_REPORT_PART_A, "Ais_Transceiver_Information", "transceiverInformation");

        addPgnSetter(AIS_CLASS_B_CS_STATIC_REPORT_PART_B, "Message_Id", "message");
        addPgnSetter(AIS_CLASS_B_CS_STATIC_REPORT_PART_B, "Repeat_Indicator", "repeat");
        addPgnSetter(AIS_CLASS_B_CS_STATIC_REPORT_PART_B, "User_Id", "mmsi");
        addPgnSetter(AIS_CLASS_B_CS_STATIC_REPORT_PART_B, "Ship_Cargo_Type", "shipType");
        addPgnSetter(AIS_CLASS_B_CS_STATIC_REPORT_PART_B, "Vendor_Id", "vendorId");
        addPgnSetter(AIS_CLASS_B_CS_STATIC_REPORT_PART_B, "Call_Sign", "callSign");
        addPgnSetter(AIS_CLASS_B_CS_STATIC_REPORT_PART_B, "Ship_Length", "shipLength");
        addPgnSetter(AIS_CLASS_B_CS_STATIC_REPORT_PART_B, "Ship_Beam", "shipBeam");
        addPgnSetter(AIS_CLASS_B_CS_STATIC_REPORT_PART_B, "Position_Reference_Point_From_Starboard", "positionReferencePointFromStarboard");
        addPgnSetter(AIS_CLASS_B_CS_STATIC_REPORT_PART_B, "Position_Reference_Point_Aft_Of_Ship_S_Bow", "positionReferencePointAftOfShipSBow");
        addPgnSetter(AIS_CLASS_B_CS_STATIC_REPORT_PART_B, "Mother_Ship_Mmsi", "mothershipMMSI");
        addPgnSetter(AIS_CLASS_B_CS_STATIC_REPORT_PART_B, "Ais_Transceiver_Information", "transceiverInformation");
    }

    @Override
    public boolean needCompilation(int canId)
    {
        needCompilation = super.needCompilation(canId);
        return true;
    }

    @Override
    public Runnable compileRaw(MessageClass mc, Supplier<byte[]> rawSupplier)
    {
        if (!needCompilation)
        {
            return ()->
            {
                logger.info("New AIS %s %s", mc.getName(), HexUtil.toString(rawSupplier.get()));
            };
        }
        return null;
    }
    
}
