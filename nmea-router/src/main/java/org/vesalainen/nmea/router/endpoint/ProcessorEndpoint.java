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
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import static java.util.logging.Level.SEVERE;
import org.vesalainen.nio.RingByteBuffer;
import org.vesalainen.nio.channels.ByteBufferChannel;
import org.vesalainen.nmea.jaxb.router.ProcessorType;
import org.vesalainen.nmea.processor.Processor;
import org.vesalainen.nmea.router.Router;
import static org.vesalainen.nmea.router.RouterManager.POOL;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
class ProcessorEndpoint extends Endpoint<ProcessorType,ByteBufferChannel>
{
    private Processor processor;

    public ProcessorEndpoint(ProcessorType processorType, Router router)
    {
        super(processorType, router);
    }

    @Override
    public ByteBufferChannel createChannel() throws IOException
    {
        ByteBufferChannel[] peers = ByteBufferChannel.open(4096, true);
        ByteBufferChannel pc1 = peers[0];
        ByteBufferChannel pc2 = peers[1];
        processor = new Processor(endpointType, pc2, POOL);
        processor.start();
        pc1.setWriteTimeout(0, TimeUnit.MILLISECONDS);
        return pc1;
    }

    @Override
    public int write(ByteBuffer bb) throws IOException
    {
        int rc = super.write(bb);
        if (rc == 0)
        {
            warning("processor not reading");
        }
        return rc;
    }

    @Override
    public int write(Endpoint src, RingByteBuffer ring) throws IOException
    {
        int rc = super.write(src, ring);
        if (rc == 0)
        {
            warning("processor not reading");
        }
        return rc;
    }

    @Override
    public void run()
    {
        try
        {
            super.run();
        }
        catch (Throwable thr)
        {
            log(SEVERE, thr, "processor stopped %s", thr.getMessage());
        }
        finally
        {
            processor.stop();
        }
    }
    
}
