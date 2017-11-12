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
import org.vesalainen.nio.channels.PipeChannel;
import org.vesalainen.nmea.jaxb.router.ProcessorType;
import org.vesalainen.nmea.processor.Processor;
import org.vesalainen.nmea.router.Router;
import static org.vesalainen.nmea.router.RouterManager.POOL;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
class ProcessorEndpoint extends Endpoint<ProcessorType,PipeChannel>
{

    private Processor processor;

    public ProcessorEndpoint(ProcessorType processorType, Router router)
    {
        super(processorType, router);
    }

    @Override
    public PipeChannel createChannel() throws IOException
    {
        PipeChannel[] peers = PipeChannel.createPeers();
        PipeChannel pc1 = peers[0];
        PipeChannel pc2 = peers[1];
        processor = new Processor(endpointType, pc2, pc2, POOL);
        processor.start();
        return pc1;
    }

    @Override
    public void run()
    {
        try
        {
            super.run();
        }
        finally
        {
            processor.stop();
        }
    }
    
}
