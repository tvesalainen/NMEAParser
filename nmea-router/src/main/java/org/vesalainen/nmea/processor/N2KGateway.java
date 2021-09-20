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

import org.vesalainen.can.AnnotatedPropertyStoreSignalCompiler;
import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.time.Clock;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import org.vesalainen.can.AbstractCanService;
import org.vesalainen.can.dbc.MessageClass;
import org.vesalainen.can.j1939.PGN;
import org.vesalainen.code.AnnotatedPropertyStore;
import org.vesalainen.code.setter.IntSetter;
import org.vesalainen.code.setter.LongSetter;
import org.vesalainen.nmea.jaxb.router.N2KGatewayType;
import org.vesalainen.nmea.util.Stoppable;
import org.vesalainen.parsers.nmea.NMEAPGN;
import static org.vesalainen.parsers.nmea.NMEAPGN.*;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class N2KGateway implements Stoppable
{

    private final WritableByteChannel out;
    private final CachedScheduledThreadPool executor;
    private final AbstractCanService canService;
    private final NMEASender nmeaSender;

    public N2KGateway(N2KGatewayType type, WritableByteChannel out, CachedScheduledThreadPool executor) throws IOException
    {
        this.out = out;
        this.executor = executor;
        this.nmeaSender = new NMEASender(Clock.systemUTC(), out, executor);
        this.canService = AbstractCanService.openSocketCand(type.getBus(), executor, new N2KCompiler(nmeaSender));
        canService.addN2K();
        type.getSentence().forEach((s) ->
        {
            Long pgn = s.getPgn();
            nmeaSender.add(s.getPrefix(), pgn != null ? pgn.intValue() : getPgnFor(s.getPrefix()));
        });
    }

    void start()
    {
        canService.start();
        nmeaSender.start();
    }
    
    @Override
    public void stop()
    {
        nmeaSender.stop();
        canService.stop();
    }

    private int getPgnFor(String prefix)
    {
        switch (prefix)
        {
            case "RMC":
                return POSITION_RAPID_UPDATE.getPGN();
            case "DBT":
                return WATER_DEPTH.getPGN();
            case "HDT":
                return VESSEL_HEADING.getPGN();
            case "MTW":
                return ENVIRONMENTAL_PARAMETERS.getPGN();
            case "MWV":
                return WIND_DATA.getPGN();
            case "VHW":
                return SPEED_WATER_REFERENCED.getPGN();
            default:
                throw new UnsupportedOperationException(prefix+" not supported");
        }
    }

    private final class N2KCompiler extends AnnotatedPropertyStoreSignalCompiler
    {

        public N2KCompiler(AnnotatedPropertyStore store)
        {
            super(store);
            addPgnSetter(VESSEL_HEADING, "True_Heading", "trueHeading");
            addPgnSetter(WATER_DEPTH, "Water_Depth_Transducer", "depthOfWater");
            addPgnSetter(WATER_DEPTH, "Offset", "transducerOffset");
            addPgnSetter(POSITION_RAPID_UPDATE, "Latitude", "latitude");
            addPgnSetter(POSITION_RAPID_UPDATE, "Longitude", "longitude");
            addPgnSetter(COG_SOG_RAPID_UPDATE, "Speed_Over_Ground", "speedOverGround");
            addPgnSetter(COG_SOG_RAPID_UPDATE, "True_Course_Over_Ground", "trackMadeGood");
            addPgnSetter(ENVIRONMENTAL_PARAMETERS, "Sea_Temperature", "waterTemperature");
            addPgnSetter(WIND_DATA, "Apparent_Wind_Speed", "relativeWindSpeed");
            addPgnSetter(WIND_DATA, "Apparent_Wind_Direction", "relativeWindAngle");
            addPgnSetter(SPEED_WATER_REFERENCED, "Speed_Water_Referenced", "waterSpeed");
        }

        public AnnotatedPropertyStoreSignalCompiler addPgnSetter(NMEAPGN nmeaPgn, String source, String target)
        {
            return addPgnSetter(nmeaPgn.getPGN(), source, target);
        }

        @Override
        public Runnable compileBegin(MessageClass mc, LongSupplier millisSupplier)
        {
            IntSetter pgnSetter = nmeaSender.getIntSetter("pgn");
            LongSetter millisSetter = nmeaSender.getLongSetter("millis");
            int pgn = PGN.pgn(mc.getId());
            return ()->
            {
                store.begin(null);
                pgnSetter.set(pgn);
                millisSetter.set(millisSupplier.getAsLong());
            };
        }

        @Override
        public Consumer<Throwable> compileEnd(MessageClass mc)
        {
            return (ex)->
            {
                if (ex == null)
                {
                    store.commit(null);
                }
                else
                {
                    store.rollback(ex.getMessage());
                }
            };
        }

        
    }
}
