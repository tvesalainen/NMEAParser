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
import java.nio.channels.WritableByteChannel;
import java.time.Clock;
import java.util.concurrent.TimeUnit;
import org.vesalainen.can.AbstractCanService;
import org.vesalainen.code.AnnotatedPropertyStore;
import org.vesalainen.nmea.jaxb.router.N2KGatewayType;
import org.vesalainen.nmea.util.Stoppable;
import org.vesalainen.parsers.nmea.NMEAPGN;
import static org.vesalainen.parsers.nmea.NMEAPGN.*;
import org.vesalainen.parsers.nmea.NMEASender;
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
        this.canService = AbstractCanService.openSocketCan2Udp(type.getAddress(), type.getPort(), new N2KCompiler(nmeaSender));
        canService.addN2K();
        type.getSentence().forEach((s) ->
        {
            nmeaSender.schedule(s.getPrefix(), s.getPeriod(), TimeUnit.MILLISECONDS);
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

    private final class N2KCompiler extends TransactionalPropertyStoreSignalCompiler
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
            addPgnSetter(WIND_DATA, "Apparent_Wind_Angle", "relativeWindAngle");
        }

        public AnnotatedPropertyStoreSignalCompiler addPgnSetter(NMEAPGN nmeaPgn, String source, String target)
        {
            return addPgnSetter(nmeaPgn.getPGN(), source, target);
        }

        
    }
}
