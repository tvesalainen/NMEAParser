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
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import org.vesalainen.nio.RingByteBuffer;
import org.vesalainen.nmea.jaxb.router.SerialType;
import org.vesalainen.nmea.router.Router;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <E>
 * @param <T>
 */
public abstract class SerialEndpoint<E extends SerialType, T extends ScatteringByteChannel & GatheringByteChannel> extends Endpoint<E,T>
{

    public SerialEndpoint(E endpointType, Router router)
    {
        super(endpointType, router);
    }
    
    public SerialEndpoint(E endpointType, Router router, String ext)
    {
        super(endpointType, router, ext);
        routing = false;
    }

    @Override
    protected void onStart() throws IOException
    {
    }
    
    @Override
    protected void onOk(RingByteBuffer ring, long timestamp) throws IOException
    {
        super.onOk(ring, timestamp);
        if (!routing)
        {
            routing = true;
            endpointMap.put(name, this);
            if (scriptEngine != null)
            {
                scriptEngine.start();
            }
            config("%s is routing", name);
        }
        
    }

}
