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

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.channels.WritableByteChannel;
import java.time.LocalDateTime;
import static java.util.logging.Level.*;
import org.vesalainen.can.j1939.PGN;
import org.vesalainen.code.AnnotatedPropertyStore;
import org.vesalainen.code.Property;
import org.vesalainen.parsers.nmea.NMEASentence;
import org.vesalainen.parsers.nmea.ais.AISBuilder;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AISSender extends AnnotatedPropertyStore
{
    private @Property N2KClock eta = new N2KClock();
    private @Property long millis;
    private @Property int canId;
    private @Property int message;
    private @Property int repeat;
    private @Property int mmsi;
    private @Property int navigationStatus;
    private @Property double longitude;
    private @Property double latitude;
    private @Property double rateOfTurn;
    private @Property double speed;
    private @Property int positionAccuracy;
    private @Property double course;
    private @Property double heading;
    private @Property int second;
    private @Property int maneuver;
    private @Property int raim;
    private @Property int radioStatus;
    private @Property int aisVersion;
    private @Property int imoNumber;
    private @Property String callSign;
    private @Property String vesselName;
    private @Property int shipType;
    private @Property int epfd;
    private @Property double draught;
    private @Property String destination;
    private @Property int dte;
    private @Property double shipLength;
    private @Property double shipBeam;
    private @Property double positionReferencePointFromStarboard;
    private @Property double positionReferencePointAftOfShipSBow;
    private @Property int csUnit;
    private @Property int display;
    private @Property int dsc;
    private @Property int band;
    private @Property int msg22;
    private @Property int assignedMode;
    private @Property int transceiverInformation;
    private @Property String vendorId;
    private @Property int unitModelCode;
    private @Property int serialNumber;
    private @Property int mothershipMMSI;
    
    private final WritableByteChannel channel;
    
    public AISSender(WritableByteChannel channel)
    {
        super(MethodHandles.lookup());
        this.channel = channel;
    }
    private String getTransceiver()
    {
        switch (transceiverInformation)
        {
            case 0:
            case 2:
                return "A";
            case 1:
            case 3:
                return "B";
            default:
                return "";
        }
    }
    private boolean isOwnMessage()
    {
        switch (transceiverInformation)
        {
            case 0:
            case 1:
                return false;
            default:
                return true;
        }
    }
    private @Property void setEstimatedDateOfArrival(int days)
    {
        eta.setDays(days);
    }
    private @Property void setEstimatedTimeOfArrival(long micros)
    {
        eta.setMicros(micros);
    }
    private NMEASentence[] getClassAPositionReport()
    {
        double crs = course < 360.0 ? course : 0;
        int hdt = (int) (heading < 360 ? heading : 511);
        return new AISBuilder(message, repeat, mmsi)
            .transceiver(getTransceiver())
            .ownMessage(isOwnMessage())
            .integer(4, navigationStatus)
            .rot((float) rateOfTurn)
            .decimal(10, speed, 10)
            .integer(1, positionAccuracy)
            .decimal(28, longitude, 600000)
            .decimal(27, latitude, 600000)
            .decimal(12, crs, 10)
            .integer(9, hdt)
            .integer(6, second)
            .integer(2, maneuver)
            .spare(3)
            .integer(1, raim)
            .integer(19, radioStatus)
            .build();
    }
    private NMEASentence[] getClassBPositionReport()
    {
        double crs = course < 360.0 ? course : 0;
        int hdt = (int) (heading < 360 ? heading : 511);
        return new AISBuilder(message, repeat, mmsi)
            .transceiver(getTransceiver())
            .ownMessage(isOwnMessage())
            .spare(8)
            .decimal(10, speed, 10)
            .integer(1, positionAccuracy)
            .decimal(28, longitude, 600000)
            .decimal(27, latitude, 600000)
            .decimal(12, crs, 10)
            .integer(9, hdt)
            .integer(6, second)
            .spare(2)
            .integer(1, csUnit)
            .integer(1, display)
            .integer(1, dsc)
            .integer(1, band)
            .integer(1, msg22)
            .integer(1, assignedMode)
            .integer(1, raim)
            .integer(20, radioStatus)
            .build();
    }
    private NMEASentence[] getClassAStaticAndVoyageRelatedData()
    {
        LocalDateTime dt = LocalDateTime.now(eta);
        return new AISBuilder(message, repeat, mmsi)
            .transceiver(getTransceiver())
            .ownMessage(isOwnMessage())
            .integer(2, aisVersion)
            .integer(30, imoNumber)
            .string(42, callSign)
            .string(120, vesselName)
            .integer(8, shipType)
            .integer(9, (int) positionReferencePointAftOfShipSBow)         // dimensionToBow
            .integer(9, (int) (shipLength-positionReferencePointAftOfShipSBow))       // dimensionToStern
            .integer(6, (int) (shipBeam-positionReferencePointFromStarboard))        // dimensionToPort
            .integer(6, (int) positionReferencePointFromStarboard)   // dimensionToStarboard
            .integer(4, epfd)
            .integer(4, dt.getMonthValue())
            .integer(5, dt.getDayOfMonth())
            .integer(5, dt.getHour())
            .integer(6, dt.getMinute())
            .decimal(8, draught, 10)
            .string(120, destination)
            .integer(1, dte)
            .spare(1)
            .build();
    }
    private NMEASentence[] getClassBStaticDataReportPartA()
    {
        return new AISBuilder(message, repeat, mmsi)
            .transceiver(getTransceiver())
            .ownMessage(isOwnMessage())
            .integer(2, 0)
            .string(120, vesselName)
            .spare(8)
            .build();
    }
    private NMEASentence[] getClassBStaticDataReportPartB()
    {
        AISBuilder b24 = new AISBuilder(message, repeat, mmsi)
            .transceiver(getTransceiver())
            .ownMessage(isOwnMessage())
            .integer(2, 1)
            .integer(8, shipType)
            .string(18, vendorId)
            .integer(4, unitModelCode)
            .integer(20, serialNumber)
            .string(42, callSign);
        if (mothershipMMSI != 0)
        {
            b24.integer(30, mothershipMMSI);
        }
        else
        {
            b24.integer(9, (int) positionReferencePointAftOfShipSBow)         // dimensionToBow
            .integer(9, (int) (shipLength-positionReferencePointAftOfShipSBow))       // dimensionToStern
            .integer(6, (int) (shipBeam-positionReferencePointFromStarboard))        // dimensionToPort
            .integer(6, (int) positionReferencePointFromStarboard);   // dimensionToStarboard
        }
        return b24.build();
    }
    @Override
    public void commit(String reason)
    {
        NMEASentence[] arr = null;
        int pgn = PGN.pgn(canId);
        switch (pgn)
        {
            case 129038:
                arr = getClassAPositionReport();
                break;
            case 129794:
                arr = getClassAStaticAndVoyageRelatedData();
                break;
            case 129039:
                arr = getClassBPositionReport();
                break;
            case 129809:
                arr = getClassBStaticDataReportPartA();
                break;
            case 129810:
                arr = getClassBStaticDataReportPartB();
                break;
            default:
                warning("pgn %d not supported", pgn);
        }
        if (arr != null)
        {
            for (NMEASentence s : arr)
            {
                try
                {
                    s.writeTo(channel);
                }
                catch (IOException ex)
                {
                    log(SEVERE, ex, "commit(%d)", pgn);
                }
            }
        }
    }
    
}
