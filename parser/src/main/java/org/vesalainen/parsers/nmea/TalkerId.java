/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public enum TalkerId
{
    /**
     * Independent AIS Base Station
     */
    AB("Independent AIS Base Station"),
    /**
     * Dependent AIS Base Station
     */
    AD("Dependent AIS Base Station"),
    /**
     * Autopilot - General
     */
    AG("Autopilot - General"),
    /**
     * Mobile Class A or B AIS Station
     */
    AI("AIS"),
    /**
     * AIS Aids to Navigation Station
     */
    AN("AIS Aids to Navigation Station"),
    /**
     * Autopilot - Magnetic
     */
    AP("Autopilot - Magnetic"),
    /**
     * AIS Receiving Station
     */
    AR("AIS Receiving Station"),
    /**
     * AIS Station (ITU_R M1371, (“Limited Base Station’)
     */
    AS("AIS Station (ITU_R M1371, (“Limited Base Station’)"),
    /**
     * AIS Transmitting Station
     */
    AT("AIS Transmitting Station"),
    /**
     * AIS Simplex Repeater Station
     */
    AX("AIS Simplex Repeater Station"),
    /**
     * Bridge navigational watch alarm system
     */
    BN("Bridge navigational watch alarm system"),
    /**
     * Computer - Programmed Calculator (obsolete)
     */
    CC("Computer - Programmed Calculator (obsolete)"),
    /**
     * Communications - Digital Selective Calling (DSC)
     */
    CD("Communications - Digital Selective Calling (DSC)"),
    /**
     * Computer - Memory Data (obsolete)
     */
    CM("Computer - Memory Data (obsolete)"),
    /**
     * Communications - Satellite
     */
    CS("Communications - Satellite"),
    /**
     * Communications - Radio-Telephone (MF/HF)
     */
    CT("Communications - Radio-Telephone (MF/HF)"),
    /**
     * Communications - Radio-Telephone (VHF)
     */
    CV("Communications - Radio-Telephone (VHF)"),
    /**
     * Communications - Radio-Telephone (VHF)
     */
    CX("Communications - Radio-Telephone (VHF)"),
    /**
     * DECCA Navigation (obsolete)
     */
    DE("DECCA Navigation (obsolete)"),
    /**
     * Direction Finder
     */
    DF("Direction Finder"),
    /**
     * Duplex repeater station
     */
    DU("Duplex repeater station"),
    /**
     * Electronic Chart Display & Information System (ECDIS)
     */
    EC("Electronic Chart Display & Information System (ECDIS)"),
    /**
     * Emergency Position Indicating Beacon (EPIRB)
     */
    EP("Emergency Position Indicating Beacon (EPIRB)"),
    /**
     * Engine Room Monitoring Systems
     */
    ER("Engine Room Monitoring Systems"),
    /**
     * Global Navigation Satellite System (GNSS)
     */
    GN("Global Navigation Satellite System (GNSS)"),
    /**
     * Global Positioning System (GPS)
     */
    GP("Global Positioning System (GPS)"),
    /**
     * Heading - Magnetic Compass
     */
    HC("Heading - Magnetic Compass"),
    /**
     * Heading - North Seeking Gyro
     */
    HE("Heading - North Seeking Gyro"),
    /**
     * Heading - Non North Seeking Gyro
     */
    HN("Heading - Non North Seeking Gyro"),
    /**
     * Integrated Instrumentation
     */
    II("Integrated Instrumentation"),
    /**
     * Integrated Navigation
     */
    IN("Integrated Navigation"),
    /**
     * Loran A (obsolete)
     */
    LA("Loran A (obsolete)"),
    /**
     * Loran C (obsolete)
     */
    LC("Loran C (obsolete)"),
    /**
     * Microwave Positioning System (obsolete)
     */
    MP("Microwave Positioning System (obsolete)"),
    /**
     * Navigation light controller
     */
    NL("Navigation light controller"),
    /**
     * OMEGA Navigation System (obsolete)
     */
    OM("OMEGA Navigation System (obsolete)"),
    /**
     * Distress Alarm System (obsolete)
     */
    OS("Distress Alarm System (obsolete)"),
    /**
     * RADAR and/or ARPA
     */
    RA("RADAR and/or ARPA"),
    /**
     * Sounder, Depth
     */
    SD("Sounder, Depth"),
    /**
     * Electronic Positioning System, other/general
     */
    SN("Electronic Positioning System, other/general"),
    /**
     * Sounder, Scanning
     */
    SS("Sounder, Scanning"),
    /**
     * Turn Rate Indicator
     */
    TI("Turn Rate Indicator"),
    /**
     * TRANSIT Navigation System
     */
    TR("TRANSIT Navigation System"),
    /**
     * User Configured 0
     */
    U0("User Configured 0"),
    /**
     * User Configured 1
     */
    U1("User Configured 1"),
    /**
     * User Configured 2
     */
    U2("User Configured 2"),
    /**
     * User Configured 3
     */
    U3("User Configured 3"),
    /**
     * User Configured 4
     */
    U4("User Configured 4"),
    /**
     * User Configured 5
     */
    U5("User Configured 5"),
    /**
     * User Configured 6
     */
    U6("User Configured 6"),
    /**
     * User Configured 7
     */
    U7("User Configured 7"),
    /**
     * User Configured 8
     */
    U8("User Configured 8"),
    /**
     * User Configured 9
     */
    U9("User Configured 9"),
    /**
     * Microprocessor controller
     */
    UP("Microprocessor controller"),
    /**
     * Velocity Sensor, Doppler, other/general
     */
    VD("Velocity Sensor, Doppler, other/general"),
    /**
     * Velocity Sensor, Speed Log, Water, Magnetic
     */
    DM("Velocity Sensor, Speed Log, Water, Magnetic"),
    /**
     * Velocity Sensor, Speed Log, Water, Mechanical
     */
    VW("Velocity Sensor, Speed Log, Water, Mechanical"),
    /**
     * Weather Instruments
     */
    WI("Weather Instruments"),
    /**
     * Transducer - Temperature (obsolete)
     */
    YC("Transducer - Temperature (obsolete)"),
    /**
     * Transducer - Displacement, Angular or Linear (obsolete)
     */
    YD("Transducer - Displacement, Angular or Linear (obsolete)"),
    /**
     * Transducer - Frequency (obsolete)
     */
    YF("Transducer - Frequency (obsolete)"),
    /**
     * Transducer - Level (obsolete)
     */
    YL("Transducer - Level (obsolete)"),
    /**
     * Transducer - Pressure (obsolete)
     */
    YP("Transducer - Pressure (obsolete)"),
    /**
     * Transducer - Flow Rate (obsolete)
     */
    YR("Transducer - Flow Rate (obsolete)"),
    /**
     * Transducer - Tachometer (obsolete)
     */
    YT("Transducer - Tachometer (obsolete)"),
    /**
     * Transducer - Volume (obsolete)
     */
    YV("Transducer - Volume (obsolete)"),
    /**
     * Transducer
     */
    YX("Transducer"),
    /**
     * Timekeeper - Atomic Clock
     */
    ZA("Timekeeper - Atomic Clock"),
    /**
     * Timekeeper - Chronometer
     */
    ZC("Timekeeper - Chronometer"),
    /**
     * Timekeeper - Quartz
     */
    ZQ("Timekeeper - Quartz"),
    /**
     * Timekeeper - Radio Update, WWV or WWVH
     */
    ZV("Timekeeper - Radio Update, WWV or WWVH");

    private final String description;


    private TalkerId(String description)
    {
        this.description = description;
    }   
}
