/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nmea.router.endpoint;

import java.io.IOException;
import org.vesalainen.comm.channel.SerialChannel;
import org.vesalainen.nio.channels.FilterChannel;
import org.vesalainen.nmea.jaxb.router.SeatalkType;
import org.vesalainen.nmea.router.Router;
import org.vesalainen.nmea.router.seatalk.SeaTalkInputStream;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SeaTalkEndpoint extends Endpoint<SeatalkType,FilterChannel>
{

    public SeaTalkEndpoint(SeatalkType seatalkType, Router router)
    {
        super(seatalkType, router);
    }

    @Override
    public FilterChannel createChannel() throws IOException
    {
        String device = endpointType.getDevice();
        SerialChannel sc = new SerialChannel
                .Builder(device, SerialChannel.Speed.B4800)
                .setParity(SerialChannel.Parity.SPACE)
                .get();
        return new FilterChannel(sc, 15, 0, SeaTalkInputStream::new, null);
    }
    
}
