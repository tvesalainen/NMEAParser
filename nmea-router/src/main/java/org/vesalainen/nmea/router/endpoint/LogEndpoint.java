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
import java.util.logging.Level;
import org.vesalainen.nio.RingByteBuffer;
import org.vesalainen.nio.channels.NullChannel;
import org.vesalainen.nmea.jaxb.router.LogEndpointType;
import org.vesalainen.nmea.router.Router;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
class LogEndpoint extends Endpoint<LogEndpointType,NullChannel>
{
    private JavaLogging log;
    private Level level;
    public LogEndpoint(LogEndpointType logEndpointType, Router router)
    {
        super(logEndpointType, router);
        this.log = JavaLogging.getLogger(logEndpointType.getLogName());
        this.level = JavaLogging.parseLevel(logEndpointType.getLogLevel());
    }
    
    @Override
    public NullChannel createChannel() throws IOException
    {
        return new NullChannel();
    }

    @Override
    public int write(Endpoint src, RingByteBuffer ring) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ring);
        log.log(level, "%s: '%s'", src.getName(), sb.substring(0, sb.length()-2));
        return ring.length();
    }

    @Override
    protected void onOk(RingByteBuffer ring, long timestamp) throws IOException
    {
        log.log(level, "%s", ring);
    }

}
