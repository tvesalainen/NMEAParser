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

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.channels.WritableByteChannel;
import java.time.Clock;
import java.time.LocalDateTime;
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
    private @Property int pgn;
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
    
    private final WritableByteChannel channel;
    
    public AISSender(WritableByteChannel channel)
    {
        super(MethodHandles.lookup());
        this.channel = channel;
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
        return new AISBuilder(message, repeat, mmsi)
            .integer(4, navigationStatus)
            .rot((float) rateOfTurn)
            .decimal(10, speed, 10)
            .integer(1, positionAccuracy)
            .decimal(28, longitude, 600000)
            .decimal(27, latitude, 600000)
            .decimal(12, course, 10)
            .integer(9, (int) heading)
            .integer(6, second)
            .integer(2, maneuver)
            .spare(3)
            .integer(1, raim)
            .integer(19, radioStatus)
            .build();
    }
    private NMEASentence[] getClassAStaticAndVoyageRelatedData()
    {
        LocalDateTime dt = LocalDateTime.now(eta);
        return new AISBuilder(message, repeat, mmsi)
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
    @Override
    public void commit(String reason)
    {
        NMEASentence[] arr = null;
        switch (pgn)
        {
            case 129038:
                arr = getClassAPositionReport();
                break;
            case 129794:
                arr = getClassAStaticAndVoyageRelatedData();
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
                    log(DEBUG, ex, "commit(%d)", pgn);
                }
            }
        }
    }
    
}
