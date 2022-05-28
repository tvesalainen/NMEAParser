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
package org.vesalainen.nmea.router.endpoint.n2kgw;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.channels.WritableByteChannel;
import java.time.LocalDateTime;
import static java.util.logging.Level.*;
import org.vesalainen.can.j1939.PGN;
import org.vesalainen.code.AnnotatedPropertyStore;
import org.vesalainen.code.Property;
import static org.vesalainen.math.UnitType.METERS_PER_SECOND;
import org.vesalainen.parsers.mmsi.MMSIType;
import static org.vesalainen.parsers.mmsi.MMSIType.CraftAssociatedWithParentShip;
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
    private @Property int aidType;
    private @Property int offPosition;
    private @Property int regional;
    private @Property int virtualAid;
    private @Property byte[] data;
    private @Property int sourceMmsi;
    private @Property int dataBits;
    
    private final WritableByteChannel channel;
    private int ownMmsi;
    
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
        if (ownMmsi == mmsi)
        {
            return true;
        }
        switch (transceiverInformation)
        {
            case 0:
            case 1:
                return false;
            default:
                ownMmsi = mmsi; // NAIS 400 doesn't set transceiver information correctly in every message
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
        if (message > 3)
        {
            throw new IllegalArgumentException(message+" not ok");
        }
        double crs = course < 360.0 ? course : 0;
        int hdt = (int) (heading < 360 ? Math.round(heading) : 511);
        //info("mmsi %d rateOfTurn %f", mmsi, rateOfTurn);
        return new AISBuilder(message, repeat, mmsi)
            .transceiver(getTransceiver())
            .ownMessage(isOwnMessage())
            .integer(4, navigationStatus)
            .rot((float) rateOfTurn)
            .decimal(10, speed, 10, METERS_PER_SECOND)
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
        if (message != 18)
        {
            throw new IllegalArgumentException(message+" not ok");
        }
        double crs = course < 360.0 ? course : 0;
        int hdt = (int) (heading < 360 ? Math.round(heading) : 511);
        return new AISBuilder(message, repeat, mmsi)
            .transceiver(getTransceiver())
            .ownMessage(isOwnMessage())
            .spare(8)
            .decimal(10, speed, 10, METERS_PER_SECOND)
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
        if (message != 5)
        {
            throw new IllegalArgumentException(message+" not ok");
        }
        LocalDateTime dt = LocalDateTime.now(eta);
        return new AISBuilder(message, repeat, mmsi)
            .transceiver(getTransceiver())
            .ownMessage(isOwnMessage())
            .integer(2, aisVersion)
            .integer(30, imoNumber)
            .string(42, callSign)
            .string(120, vesselName)
            .integer(8, shipType)
            .integer(9, dimensionToBow())
            .integer(9, dimensionToStern())
            .integer(6, dimensionToPort())
            .integer(6, dimensionToStarboard())
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
    private NMEASentence[] getAidsToNavigationReport()
    {
        return new AISBuilder(message, repeat, mmsi)
            .transceiver(getTransceiver())
            .ownMessage(isOwnMessage())
            .integer(5, aidType)
            .string(120, substring(vesselName, 0, 20))
            .integer(1, positionAccuracy)
            .decimal(28, longitude, 600000)
            .decimal(27, latitude, 600000)
            .integer(9, dimensionToBow())
            .integer(9, dimensionToStern())
            .integer(6, dimensionToPort())
            .integer(6, dimensionToStarboard())
            .integer(4, epfd)
            .integer(6, second)
            .integer(1, offPosition)
            .integer(8, regional)
            .integer(1, raim)
            .integer(1, virtualAid)
            .integer(1, assignedMode)
            .spare(1)
            .string(84, substring(vesselName, 20), false)
            .build();
    }
    private NMEASentence[] getClassBStaticDataReportPartA()
    {
        if (message != 24)
        {
            throw new IllegalArgumentException(message+" not ok");
        }
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
        if (message != 24)
        {
            throw new IllegalArgumentException(message+" not ok");
        }
        AISBuilder b24 = new AISBuilder(message, repeat, mmsi)
            .transceiver(getTransceiver())
            .ownMessage(isOwnMessage())
            .integer(2, 1)
            .integer(8, shipType)
            .string(18, vendorId)
            .integer(4, unitModelCode)
            .integer(20, serialNumber)
            .string(42, callSign);
        if (MMSIType.getType(mmsi) == CraftAssociatedWithParentShip)
        {
            b24.integer(30, mothershipMMSI);
        }
        else
        {
            b24
            .integer(9, dimensionToBow())
            .integer(9, dimensionToStern())
            .integer(6, dimensionToPort())
            .integer(6, dimensionToStarboard());
        }
        return b24.build();
    }
    private NMEASentence[] getAisBinaryBroadcastMessage()
    {
        if (message != 8)
        {
            throw new IllegalArgumentException(message+" not ok");
        }
        if (dataBits % 8 != 0)
        {
            throw new IllegalArgumentException(dataBits+" dataBits not ok");
        }
        return new AISBuilder(message, repeat, sourceMmsi)
            .transceiver(getTransceiver())
            .ownMessage(isOwnMessage())
            .integer(2, 0)
            .binary(data, 8, dataBits/8)
            .build();
    }
    private int dimensionToBow()
    {
        if (shipLength > 511)
        {
            return 0;
        }
        return (int) positionReferencePointAftOfShipSBow;
    }
    private int dimensionToStern()
    {
        if (shipLength > 511)
        {
            return 0;
        }
        return (int) (shipLength-positionReferencePointAftOfShipSBow);
    }
    private int dimensionToPort()
    {
        if (shipBeam > 63)
        {
            return 0;
        }
        return (int) (shipBeam-positionReferencePointFromStarboard);
    }
    private int dimensionToStarboard()
    {
        if (shipBeam > 63)
        {
            return 0;
        }
        return (int) positionReferencePointFromStarboard;
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
            case 129041:
                arr = getAidsToNavigationReport();
                break;
            case 129797:    // Ais_Binary_Broadcast_Message
                arr = getAisBinaryBroadcastMessage();
                break;
            case 129040:    // Ais_Class_B_Extended_Position_Report
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

    private String substring(String text, int begin, int end)
    {
        if (end <= text.length())
        {
            return text.substring(begin, end);
        }
        else
        {
            return text;
        }
    }

    private String substring(String text, int begin)
    {
        if (begin < text.length())
        {
            return text.substring(begin);
        }
        else
        {
            return "";
        }
    }
    
}
