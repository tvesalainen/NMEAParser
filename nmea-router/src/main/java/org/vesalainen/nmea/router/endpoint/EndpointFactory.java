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

import org.vesalainen.nmea.jaxb.router.BroadcastNMEAType;
import org.vesalainen.nmea.jaxb.router.BroadcastType;
import org.vesalainen.nmea.jaxb.router.DatagramType;
import org.vesalainen.nmea.jaxb.router.EndpointType;
import org.vesalainen.nmea.jaxb.router.MulticastNMEAType;
import org.vesalainen.nmea.jaxb.router.MulticastType;
import org.vesalainen.nmea.jaxb.router.Nmea0183HsType;
import org.vesalainen.nmea.jaxb.router.Nmea0183Type;
import org.vesalainen.nmea.jaxb.router.ProcessorType;
import org.vesalainen.nmea.jaxb.router.SeatalkType;
import org.vesalainen.nmea.jaxb.router.SerialType;
import org.vesalainen.nmea.jaxb.router.TcpEndpointType;
import org.vesalainen.nmea.router.Router;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class EndpointFactory
{
    public static Endpoint getInstance(EndpointType endpointType, Router router, int bufSize)
    {
        if (endpointType instanceof TcpEndpointType)
        {
            return new TCPListenerEndpoint((TcpEndpointType) endpointType, router, bufSize);
        }
        if (endpointType instanceof ProcessorType)
        {
            return new ProcessorEndpoint((ProcessorType) endpointType, router, bufSize);
        }
        if (endpointType instanceof MulticastNMEAType)
        {
            return new MulticastNMEAEndpoint((MulticastNMEAType) endpointType, router, bufSize);
        }
        if (endpointType instanceof MulticastType)
        {
            return new MulticastEndpoint((MulticastType) endpointType, router, bufSize);
        }
        if (endpointType instanceof BroadcastNMEAType)
        {
            return new BroadcastNMEAEndpoint((BroadcastNMEAType) endpointType, router, bufSize);
        }
        if (endpointType instanceof BroadcastType)
        {
            return new BroadcastEndpoint((BroadcastType) endpointType, router, bufSize);
        }
        if (endpointType instanceof DatagramType)
        {
            return new DatagramEndpoint((DatagramType) endpointType, router, bufSize);
        }
        if (endpointType instanceof Nmea0183HsType)
        {
            return new NmeaHsEndpoint((Nmea0183HsType) endpointType, router, bufSize);
        }
        if (endpointType instanceof Nmea0183Type)
        {
            return new NmeaEndpoint((Nmea0183Type) endpointType, router, bufSize);
        }
        if (endpointType instanceof SeatalkType)
        {
            return new SeaTalkEndpoint((SeatalkType) endpointType, router, bufSize);
        }
        if (endpointType instanceof SerialType)
        {
            return new SerialEndpoint((SerialType) endpointType, router, bufSize);
        }
        throw new IllegalArgumentException(endpointType+" unknown");
    }

    
}
