/*
 * Copyright (C) 2020 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import d3.env.TSAGeoMag;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.channels.WritableByteChannel;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntConsumer;
import java.util.logging.Level;
import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;
import org.vesalainen.can.j1939.PGN;
import org.vesalainen.code.AnnotatedPropertyStore;
import org.vesalainen.code.Property;
import org.vesalainen.math.UnitType;
import org.vesalainen.parsers.nmea.NMEAPGN;
import static org.vesalainen.parsers.nmea.NMEAPGN.*;
import org.vesalainen.parsers.nmea.NMEASentence;
import org.vesalainen.time.SimpleClock;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class NMEASender extends AnnotatedPropertyStore
{
    private @Property long millis;
    private @Property int canId;
    private @Property double latitude;
    private @Property double longitude;
    private @Property float depthOfWater;
    private @Property float transducerOffset;
    private @Property float waterTemperature;
    private @Property float waterSpeed;
    private @Property float trueHeading;
    private @Property float relativeWindAngle;
    private @Property float relativeWindSpeed;
    private @Property float pitch;
    private @Property float roll;
    private @Property float speedOverGround;
    private @Property float trackMadeGood;
    
    private Clock frameClock;
    private N2KClock positionClock;
    private final WritableByteChannel channel;
    private final TSAGeoMag geoMag = new TSAGeoMag();
    
    private final Map<Integer,IntConsumer> pgnMap = new HashMap<>();
    private final NMEASentence rmc;
    private final NMEASentence rmc2;
    private final NMEASentence dbt;
    private final NMEASentence hdt;
    private final NMEASentence mtw;
    private final NMEASentence mwv;
    private final NMEASentence vhw;
    
    public NMEASender(WritableByteChannel channel)
    {
        super(MethodHandles.lookup());
        this.frameClock = new SimpleClock(()->millis);
        this.positionClock = new N2KClock();
        this.channel = channel;
        
        this.rmc = NMEASentence.rmc(
                ()->frameClock, 
                ()->latitude, 
                ()->longitude, 
                ()->speedOverGround, 
                ()->trackMadeGood, 
                ()->magneticVariation(frameClock));
        this.rmc2 = NMEASentence.rmc(
                ()->positionClock, 
                ()->latitude, 
                ()->longitude, 
                ()->speedOverGround, 
                ()->trackMadeGood, 
                ()->magneticVariation(positionClock));
        this.dbt = NMEASentence.dbt(()->depthOfWater+transducerOffset, UnitType.METER);
        this.hdt = NMEASentence.hdt(()->trueHeading);
        this.mtw = NMEASentence.mtw(()->waterTemperature, UnitType.CELSIUS);
        this.mwv = NMEASentence.mwv(()->relativeWindAngle, ()->relativeWindSpeed, UnitType.METERS_PER_SECOND, false);
        this.vhw = NMEASentence.vhw(()->waterSpeed, UnitType.METERS_PER_SECOND);
    }

    public void add(String prefix)
    {
        switch (prefix)
        {
            case "RMC":
                IntConsumer ic = createPgnConsumer(GNSS_POSITION_DATA, POSITION_RAPID_UPDATE);
                pgnMap.put(GNSS_POSITION_DATA.getPGN(), ic);
                pgnMap.put(POSITION_RAPID_UPDATE.getPGN(), ic);
                break;
            case "DBT":
                pgnMap.put(WATER_DEPTH.getPGN(), createPgnConsumer(WATER_DEPTH));
                break;
            case "HDT":
                pgnMap.put(VESSEL_HEADING.getPGN(), createPgnConsumer(VESSEL_HEADING));
                break;
            case "MTW":
                pgnMap.put(ENVIRONMENTAL_PARAMETERS.getPGN(), createPgnConsumer(ENVIRONMENTAL_PARAMETERS));
                break;
            case "MWV":
                pgnMap.put(WIND_DATA.getPGN(), createPgnConsumer(WIND_DATA));
                break;
            case "VHW":
                pgnMap.put(SPEED_WATER_REFERENCED.getPGN(), createPgnConsumer(SPEED_WATER_REFERENCED));
                break;
        }
    }
    private IntConsumer createPgnConsumer(NMEAPGN... pgns)
    {
        Priorizer.Builder builder = Priorizer.builder()
                .setMillisSupplier(()->millis);
        for (NMEAPGN pgn : pgns)
        {
            builder.addPgn(pgn.getPGN(), ()->write(pgn));
        }
        return builder.build();
    }
    private void write(NMEAPGN pgn)
    {
        try
        {
            switch (pgn)
            {
                case GNSS_POSITION_DATA:
                    rmc2.writeTo(channel);
                    break;
                case POSITION_RAPID_UPDATE:
                    rmc.writeTo(channel);
                    break;
                case WATER_DEPTH:
                    dbt.writeTo(channel);
                    break;
                case VESSEL_HEADING:
                    hdt.writeTo(channel);
                    break;
                case ENVIRONMENTAL_PARAMETERS:
                    mtw.writeTo(channel);
                    break;
                case WIND_DATA:
                    mwv.writeTo(channel);
                    break;
                case SPEED_WATER_REFERENCED:
                    vhw.writeTo(channel);
                    break;
                default:
                    throw new UnsupportedOperationException(pgn+" not supported");
            }
        }
        catch (IOException ex)
        {
            log(SEVERE, ex, "write(%s)", pgn);
        }
    }
    public double magneticVariation(Clock clock)
    {
        ZonedDateTime now = ZonedDateTime.now(clock);
        return geoMag.getDeclination(latitude, longitude, (double)(now.getYear()+now.getDayOfYear()/365.0), 0);
    }
    @Override
    public void commit(String reason)
    {
        IntConsumer ic = pgnMap.get(PGN.pgn(canId));
        if (ic != null)
        {
            ic.accept(canId);
        }
    }
    private @Property void setPositionDate(int days)
    {
        positionClock.setDays(days);
    }
    private @Property void setPositionTime(long micros)
    {
        positionClock.setMicros(micros);
    }
    
}
