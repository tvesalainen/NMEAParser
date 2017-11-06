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
package org.vesalainen.nmea.router;

import java.nio.channels.ScatteringByteChannel;
import org.vesalainen.comm.channel.SerialChannel;
import org.vesalainen.nio.channels.FilterChannel;
import org.vesalainen.nmea.jaxb.router.Nmea0183HsType;
import org.vesalainen.nmea.jaxb.router.Nmea0183Type;
import org.vesalainen.nmea.jaxb.router.SeatalkType;
import org.vesalainen.nmea.jaxb.router.SerialType;
import org.vesalainen.nmea.router.seatalk.SeaTalkInputStream;
import org.vesalainen.util.function.IOFunction;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public enum PortType
{
    NMEA((port)->SerialChannel.builder(port)
            .setSpeed(SerialChannel.Speed
            .B4800)
            .setCanonical(true)
            .build()
    ),
    NMEA_HS((port)->SerialChannel
            .builder(port)
            .setSpeed(SerialChannel
            .Speed.B38400)
            .setCanonical(true)
            .build()
    ),
    SEA_TALK((String port)->
    {
        SerialChannel sc = SerialChannel
                .builder(port)
                .setSpeed(SerialChannel.Speed.B4800)
                .setParity(SerialChannel.Parity.SPACE)
                .setMin(15)
                .setTime(1)
                .build();
        return new FilterChannel(sc, 128, 0, SeaTalkInputStream::new, null);
    });
    
    private IOFunction<String,ScatteringByteChannel> channelFactory;

    private PortType(IOFunction<String, ScatteringByteChannel> supplier)
    {
        this.channelFactory = supplier;
    }

    public IOFunction<String, ScatteringByteChannel> getChannelFactory()
    {
        return channelFactory;
    }
    
    public static PortType getPortType(SerialType serialType)
    {
        if (serialType instanceof SeatalkType)
        {
            return PortType.SEA_TALK;
        }
        else
        {
            if (serialType instanceof Nmea0183Type)
            {
                return PortType.NMEA;
            }
            else
            {
                if (serialType instanceof Nmea0183HsType)
                {
                    return PortType.NMEA_HS;
                }
            }
        }
        throw new IllegalArgumentException(serialType+" conflicts with PortType");
    }
}
