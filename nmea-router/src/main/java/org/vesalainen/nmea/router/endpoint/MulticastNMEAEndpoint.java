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
import static java.lang.Thread.NORM_PRIORITY;
import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import org.vesalainen.lang.Primitives;
import org.vesalainen.nio.channels.UnconnectedDatagramChannel;
import org.vesalainen.nmea.jaxb.router.MulticastNMEAType;
import org.vesalainen.nmea.router.NMEAReader;
import org.vesalainen.nmea.router.Router;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MulticastNMEAEndpoint extends Endpoint<MulticastNMEAType,UnconnectedDatagramChannel>
{

    private boolean loop;

    public MulticastNMEAEndpoint(MulticastNMEAType multicastNMEAType, Router router)
    {
        super(multicastNMEAType, router);
    }

    @Override
    public UnconnectedDatagramChannel createChannel() throws IOException
    {
        String address = endpointType.getAddress();
        return UnconnectedDatagramChannel.open(address, 10110, bufferSize, true, loop);
    }

    @Override
    public void run()
    {
        this.loop = Primitives.getBoolean(endpointType.isLoop(), false);
        if (loop)
        {
            try
            {
                config("Multicast in loop mode");
                config("registerMBean %s", objectName);
                ManagementFactory.getPlatformMBeanServer().registerMBean(this, objectName);
                onStart();
                Thread.sleep(Long.MAX_VALUE);
            }
            catch (Throwable ex)
            {
                warning("%s stopped because of %s", name, ex);
                log(SEVERE, ex, "%s stopped because of %s", name, ex);
            }
            finally
            {
                try
                {
                    onStop();
                    ManagementFactory.getPlatformMBeanServer().unregisterMBean(objectName);
                    config("unregisterMBean %s", objectName);
                }
                catch (IOException | InstanceNotFoundException | MBeanRegistrationException ex)
                {
                    log(Level.SEVERE, null, ex);
                }
            }
        }
        else
        {
            super.run();
        }
    }
    
}
