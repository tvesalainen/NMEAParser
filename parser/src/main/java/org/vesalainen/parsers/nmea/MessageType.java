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

package org.vesalainen.parsers.nmea;

/**
 *
 * @author Timo Vesalainen
 */
public enum MessageType
{
    AAM("Waypoint Arrival Alarm"),
    ABK("UAIS Addressed and binary broadcast acknowledgement"),
    ACA("UAIS Regional Channel Assignment Message"),
    ACS("UAIS Channel management information Source"),
    AIR("UAIS Interrogation Request"),
    ALM("GPS Almanac Data"),
    ALR("Set Alarm State"),
    APA("Heading/Track Controller (Autopilot) Sentence “A”"),
    APB("Heading/Track Controller (Autopilot) Sentence “B”"),
    BEC("Bearing and Distance to Waypoint - Dead Reckoning"),
    BOD("Bearing - Origin to Destination"),
    BWC("Bearing and Distance to Waypoint"),
    BWR("Bearing and Distance to Waypoint - Rhumb Line"),
    BWW("Bearing - Waypoint to Waypoint"),
    CUR("Water Current Layer"),
    DBK("Depth Below Keel"),
    DBS("Depth Below Surface"),
    DBT("Depth Below Transducer"),
    DCN("Decca Position"),
    DPT("Depth"),
    DSC("Digital Selective Calling Information"),
    DSE("Expanded Digital Selective Calling"),
    DSI("DSC Transponder Initialize"),
    DSR("DCS Transponder Response"),
    DTM("Datum Reference"),
    FSI("Frequency Set Information"),
    GBS("GNSS Satellite Fault Detection"),
    GGA("Global Positioning System Fix Data"),
    GLC("Geographic Position - Loran C"),
    GLL("Geographic Position - Latitude/Longitude"),
    GMP("GNSS Map Projection Fix Data"),
    GNS("GNSS Fix Data"),
    GRS("GNSS Range Residues"),
    GSA("GNSS DOP and Active Satellites"),
    GST("GNSS Pseudo range Error Statistics"),
    GSV("GNSS Satellites in View"),
    GTD("Geographic Location in Time Differences"),
    GXA("TRANSIT Position - Latitude/Longitude"),
    HDG("Heading, Deviation and Variation"),
    HDM("Heading, Magnetic"),
    HDT("Heading, True"),
    HFB("Trawl Headrope to Footrope and Bottom"),
    HMR("Heading Monitor Receive"),
    HMS("Heading Monitor Set"),
    HSC("Heading Steering Command"),
    HTC("Heading/Track Control Command"),
    HTD("Heading/Track Control Data"),
    ITS("Trawl Door Spread 2 Distance"),
    LCD("Loran-C Signal Data"),
    LRF("UAIS Long-Range Function"),
    LTI("UAIS Long-Range Interrogation"),
    LR1("UAIS Long-Range Reply Sentence 1"),
    LR2("UAIS Long-Range Reply Sentence 2"),
    LR3("UAIS Long-Range Reply Sentence 3"),
    MLA("GLONASS Almanac Data"),
    MSK("MSK Receiver Interface"),
    MSS("MSK Receiver Signal"),
    MTW("Water Temperature"),
    MWD("Wind Direction and Speed"),
    MWV("Wind speed and Angle"),
    OLN("Omega Lane Numbers"),
    OSD("Own Ship Data"),
    R00("Waypoints in active route"),
    RMA("Recommended Minimum Specific Loran-C Data"),
    RMB("Recommended Minimum Navigation Information"),
    RMC("Recommended Minimum Specific GNSS Data"),
    ROT("Rate of Turn"),
    RPM("Revolutions"),
    RSA("Rudder Sensor Angle"),
    RSD("Radar System Data"),
    RTE("Routes RTE - Routes"),
    SFI("Scanning Frequency Information"),
    SSD("UAIS Ship Static Data"),
    STN("Multiple Data ID"),
    TDS("Trawl Door Spread Distance"),
    TFI("Trawl Filling Indicator"),
    THS("True Heading"),
    TLB("Target Label"),
    TPC("Trawl Position Cartesian Coordinates"),
    TPR("Trawl Position Relative Vessel"),
    TPT("Trawl Position True"),
    TLL("Target Latitude and Longitude"),
    TRF("TRANSIT Fix Data"),
    TTM("Tracked Target Message"),
    TUT("Transmission of Multi-language Text"),
    TXT("Text Transmission"),
    VBW("Dual Ground/Water Speed"),
    VDR("Set and Drift"),
    VHW("Water Speed and Heading"),
    VLW("Dual Ground/Water Distance"),
    VPW("Speed - Measured Parallel to Wind"),
    VSD("UAIS Voyage Static Data"),
    VTG("Course Over Ground and Ground Speed"),
    VWR("Relative Wind Speed and Angle"),
    WCV("Waypoint Closure Velocity"),
    WNC("Distance - Waypoint to Waypoint"),
    WPL("Waypoint Location"),
    XDR("Transducer Measurements"),
    XTE("Cross-Track Error, Measured"),
    XTR("Cross-Track Error - Dead Reckoning"),
    ZDA("Time and Date"),
    ZDL("Time and Distance to Variable Point "),
    ZFO("UTC and Time from Origin Waypoint"),
    ZTG("UTC and Time to Destination Waypoint Encapsulation "),
    ABM("UAIS Addressed binary and safety related message"),
    BBM("UAIS Broadcast Binary Message"),
    VDM("UAIS VHF Data-link Message"),
    VDO("UAIS VHF Data-link Own-vessel report");
    
    private final String description;

    private MessageType(String description)
    {
        this.description = description;
    }

    public String getDescription()
    {
        return description;
    }
    
}
