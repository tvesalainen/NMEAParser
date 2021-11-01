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
package org.vesalainen.nmea.router.endpoint;

import java.io.IOException;
import static java.util.logging.Level.SEVERE;
import org.vesalainen.nio.channels.PipeChannel;
import org.vesalainen.nmea.jaxb.router.LogEndpointType;
import org.vesalainen.nmea.jaxb.router.N2KGatewayType;
import org.vesalainen.nmea.router.Router;
import static org.vesalainen.nmea.router.RouterManager.POOL;
import org.vesalainen.nmea.router.endpoint.n2kgw.N2KGateway;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class N2KGatewayEndpoint extends Endpoint<N2KGatewayType,PipeChannel>
{

    private N2KGateway n2kGateway;

    public N2KGatewayEndpoint(N2KGatewayType endpointType, Router router)
    {
        super(endpointType, router);
    }

    @Override
    public PipeChannel createChannel() throws IOException
    {
        PipeChannel[] peers = PipeChannel.createPeers();
        PipeChannel pc1 = peers[0];
        PipeChannel pc2 = peers[1];
        this.n2kGateway = N2KGateway.getInstance(endpointType, pc2, POOL);
        n2kGateway.start();
        return pc1;
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
            log(SEVERE, thr, "n2kGateway stopped %s", thr.getMessage());
        }
        finally
        {
            n2kGateway.stop();
        }
    }
    
}
