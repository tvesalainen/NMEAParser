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
import org.vesalainen.comm.channel.SerialChannel.Builder;
import org.vesalainen.comm.channel.SerialChannel.Configuration;
import org.vesalainen.nmea.jaxb.router.FlowControlType;
import org.vesalainen.nmea.jaxb.router.ParityType;
import org.vesalainen.nmea.jaxb.router.SerialType;
import org.vesalainen.nmea.router.Router;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
class SerialEndpoint<E extends SerialType> extends Endpoint<E, SerialChannel>
{

    public SerialEndpoint(E serialType, Router router)
    {
        super(serialType, router);
        Long mr = serialType.getMaxRead();
        if (mr != null)
        {
            maxRead = mr.intValue();
            if (maxRead > bufferSize)
            {
                bufferSize = maxRead;
            }
        }
    }

    protected Configuration createConfig()
    {
        Configuration configuration = new Configuration();
        Long speed = endpointType.getSpeed();
        if (speed != null)
        {
            configuration.setSpeed(SerialChannel.getSpeed(speed.intValue()));
        }
        Integer bits = endpointType.getBits();
        if (bits != null)
        {
            configuration.setDataBits(SerialChannel.getDataBits(bits));
        }
        ParityType parity = endpointType.getParity();
        if (parity != null)
        {
            configuration.setParity(SerialChannel.getParity(parity.name()));
        }
        Integer stops = endpointType.getStops();
        if (stops != null)
        {
            configuration.setStopBits(SerialChannel.getStopBits(stops));
        }
        FlowControlType flowControl = endpointType.getFlowControl();
        if (flowControl != null)
        {
            configuration.setFlowControl(SerialChannel.getFlowControl(flowControl.name()));
        }
        return configuration;
    }
    @Override
    public SerialChannel createChannel() throws IOException
    {
        String device = endpointType.getDevice();
        Configuration configuration = createConfig();
        Builder builder = new SerialChannel.Builder(device, configuration);
        return builder.get();
    }
    
}
