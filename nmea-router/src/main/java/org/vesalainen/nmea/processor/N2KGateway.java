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
import org.vesalainen.can.AbstractCanService;
import org.vesalainen.nmea.jaxb.router.N2KGatewayType;
import org.vesalainen.nmea.util.Stoppable;
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
    private final AISSender aisSender;

    public N2KGateway(N2KGatewayType type, WritableByteChannel out, CachedScheduledThreadPool executor) throws IOException
    {
        this.out = out;
        this.executor = executor;
        this.nmeaSender = new NMEASender(Clock.systemUTC(), out);
        this.aisSender = new AISSender(out);
        this.canService = AbstractCanService.openSocketCand(type.getBus(), executor, new N2KMessageFactory(nmeaSender, aisSender));
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
    }
    
    @Override
    public void stop()
    {
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

}
