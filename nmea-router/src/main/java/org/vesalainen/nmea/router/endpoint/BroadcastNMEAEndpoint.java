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
import org.vesalainen.nio.channels.UnconnectedDatagramChannel;
import org.vesalainen.nmea.jaxb.router.BroadcastNMEAType;
import org.vesalainen.nmea.router.Router;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BroadcastNMEAEndpoint extends Endpoint<BroadcastNMEAType,UnconnectedDatagramChannel>
{

    public BroadcastNMEAEndpoint(BroadcastNMEAType broadcastNMEAType, Router router, int bufSize)
    {
        super(broadcastNMEAType, router, bufSize);
    }

    @Override
    public UnconnectedDatagramChannel createChannel() throws IOException
    {
        return UnconnectedDatagramChannel.open("255.255.255.255", 10110, bufferSize, true, false);
    }
    
}
