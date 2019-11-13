/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.time.ZonedDateTime;
import java.util.Collection;
import org.vesalainen.math.Circle;
import org.vesalainen.math.Polygon;
import org.vesalainen.parsers.nmea.NMEASentence;
import static org.vesalainen.parsers.nmea.ais.CodesForShipType.*;
import static org.vesalainen.parsers.nmea.ais.MessageTypes.*;
import org.vesalainen.parsers.nmea.ais.areanotice.Area;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AISSentence
{
    public static NMEASentence[] getMsg1(
            int mmsi,
            NavigationStatus navigationStatus,
            double longitude,
            double latitude,
            float rateOfTurn,
            float speed,
            boolean positionAccuracy,
            float course,
            int heading,
            int second,
            ManeuverIndicator maneuver,
            boolean raim,
            int radioStatus
    )
    {
        if (!Double.isNaN(latitude))
        {
            return new AISBuilder(PositionReportClassA, mmsi)
                .integer(4, navigationStatus, NavigationStatus.NotDefinedDefault)
                .rot(rateOfTurn)
                .decimal(10, speed, 10)
                .bool(positionAccuracy)
                .decimal(28, longitude, 600000)
                .decimal(27, latitude, 600000)
                .decimal(12, course, 10)
                .integer(9, heading)
                .integer(6, second)
                .integer(2, maneuver, ManeuverIndicator.NotAvailableDefault)
                .spare(3)
                .bool(raim)
                .integer(19, radioStatus)
                .build();
        }
        else
        {
            return AISBuilder.EMPTY;
        }
    }
    public static NMEASentence[] getMsg5(
            int mmsi,
            int aisVersion,
            int imoNumber,
            String callSign,
            String vesselName,
            CodesForShipType shipType,
            int dimensionToBow,
            int dimensionToStern,
            int dimensionToPort,
            int dimensionToStarboard,
            EPFDFixTypes epfd,
            int etaMonth,
            int etaDay,
            int etaHour,
            int etaMinute,
            float draught,
            String destination,
            boolean dte
    )
    {
        return new AISBuilder(StaticAndVoyageRelatedData, mmsi)
            .integer(2, aisVersion)
            .integer(30, imoNumber)
            .string(42, callSign)
            .string(120, vesselName)
            .integer(8, shipType, NotAvailableDefault)
            .integer(9, dimensionToBow)
            .integer(9, dimensionToStern)
            .integer(6, dimensionToPort)
            .integer(6, dimensionToStarboard)
            .integer(4, epfd, EPFDFixTypes.UndefinedDefault)
            .integer(4, etaMonth)
            .integer(5, etaDay)
            .integer(5, etaHour)
            .integer(6, etaMinute)
            .decimal(8, draught, 10)
            .string(120, destination)
            .bool(dte)
            .spare(1)
            .build();
    }
    public static NMEASentence[] getMsg8(
            int mmsi,
            int dac,
            int linkage,
            AreaNoticeDescription notice,
            ZonedDateTime date,
            int duration,
            Circle circle,
            String text
            )
    {
        AISBuilder bld = binaryBroadcastMessage(mmsi, dac, linkage, notice, date, duration);
        bld.circle(circle);
        if (text != null)
        {
            bld.associatedText(text);
        }
        return bld.build();
    }
    public static NMEASentence[] getMsg8Polygon(
            int mmsi,
            int dac,
            int linkage,
            AreaNoticeDescription notice,
            ZonedDateTime date,
            int duration,
            Polygon polygon,
            String text
            )
    {
        AISBuilder bld = binaryBroadcastMessage(mmsi, dac, linkage, notice, date, duration);
        bld.polygon(polygon);
        if (text != null)
        {
            bld.associatedText(text);
        }
        return bld.build();
    }
    static NMEASentence[] getMsg8(
            int mmsi,
            int dac,
            int linkage,
            AreaNoticeDescription notice,
            ZonedDateTime date,
            int duration,
            Collection<Area> areas
            )
    {
        AISBuilder bld = binaryBroadcastMessage(mmsi, dac, linkage, notice, date, duration);
        for (Area area : areas)
        {
            area.build(bld);
        }
        return bld.build();
    }
    private static AISBuilder binaryBroadcastMessage(
            int mmsi,
            int dac,
            int linkage,
            AreaNoticeDescription notice,
            ZonedDateTime date,
            int duration
    )
    {
        return new AISBuilder(BinaryBroadcastMessage, mmsi)
                .spare(2)
                .integer(10, dac)
                .integer(6, 22)
                .integer(10, linkage)
                .integer(7, notice, AreaNoticeDescription.UndefinedDefault)
                .integer(4, date.getMonthValue())
                .integer(5, date.getDayOfMonth())
                .integer(5, date.getHour())
                .integer(6, date.getMinute())
                .integer(18, duration);
    }
    public static NMEASentence[] getMsg18(
            int mmsi,
            NavigationStatus navigationStatus,
            double longitude,
            double latitude,
            float speed,
            boolean positionAccuracy,
            float course,
            int heading,
            int second,
            boolean csUnit,
            boolean display,
            boolean dsc,
            boolean band,
            boolean msg22,
            boolean assignedMode,
            boolean raim,
            int radioStatus
    )
    {
        if (!Double.isNaN(latitude))
        {
            return new AISBuilder(StandardClassBCSPositionReport, mmsi)
                .spare(8)
                .decimal(10, speed, 10)
                .bool(positionAccuracy)
                .decimal(28, longitude, 600000)
                .decimal(27, latitude, 600000)
                .decimal(12, course, 10)
                .integer(9, heading)
                .integer(6, second)
                .spare(2)
                .bool(csUnit)
                .bool(display)
                .bool(dsc)
                .bool(band)
                .bool(msg22)
                .bool(assignedMode)
                .bool(raim)
                .integer(20, radioStatus)
                .build();
        }
        else
        {
            return AISBuilder.EMPTY;
        }
    }
    public static NMEASentence[] getMsg24(
            int mmsi,
            String callSign,
            String vesselName,
            CodesForShipType shipType,
            int dimensionToBow,
            int dimensionToStern,
            int dimensionToPort,
            int dimensionToStarboard,
            String vendorId,
            int unitModelCode,
            int serialNumber,
            int mothershipMMSI
    )
    {
        NMEASentence[] msg24A = new AISBuilder(StaticDataReport, mmsi)
                .integer(2, 0)
                .string(120, vesselName)
                .spare(8)
                .build();

        AISBuilder b24 = new AISBuilder(StaticDataReport, mmsi)
                .integer(2, 1)
            .integer(8, shipType, NotAvailableDefault)
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
            b24.integer(9, dimensionToBow);
            b24.integer(9, dimensionToStern);
            b24.integer(6, dimensionToPort);
            b24.integer(6, dimensionToStarboard);
        }
        NMEASentence[] msg24B = b24.build();
        return new NMEASentence[]{msg24A[0], msg24B[0]};
    }
}
