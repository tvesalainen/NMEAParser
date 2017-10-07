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

import org.vesalainen.nmea.router.endpoint.Endpoint;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import org.vesalainen.comm.channel.SerialChannel;
import org.vesalainen.nmea.jaxb.router.EndpointType;
import org.vesalainen.nmea.jaxb.router.Nmea0183HsType;
import org.vesalainen.nmea.jaxb.router.Nmea0183Type;
import org.vesalainen.nmea.jaxb.router.RouteType;
import org.vesalainen.nmea.jaxb.router.SeatalkType;
import org.vesalainen.nmea.jaxb.router.SerialType;
import static org.vesalainen.nmea.router.ThreadPool.POOL;
import org.vesalainen.nmea.router.endpoint.EndpointFactory;
import org.vesalainen.nmea.router.scanner.PortScanner;
import org.vesalainen.nmea.script.RouterEngine;
import org.vesalainen.util.DistinguishMap;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Router extends JavaLogging implements RouterEngine
{
    private final RouterConfig config;
    private Map<String,Endpoint> sources = new HashMap<>();
    private Map<Future,Endpoint> futureMap = new HashMap<>();
    private Map<String,EndpointType> nameMap = new HashMap<>();
    private Map<EndpointType,Set<String>> fingerPrintMap = new HashMap<>();
    private DistinguishMap<String,EndpointType> distinguishMap = new DistinguishMap<>();
    private Set<PortType> scanChoises = EnumSet.noneOf(PortType.class);
    private PortScanner scanner;
    private final int ringBufferSize;
    private final long checkDelay;
    private final long closeDelay;
    private final long fingerPrintDelay;

    public Router(RouterConfig config)
    {
        super(Router.class);
        this.config = config;
        ringBufferSize = config.getRingBufferSize();
        checkDelay = config.getCheckDelay();
        closeDelay = config.getCloseDelay();
        fingerPrintDelay = config.getFingerPrintDelay();
    }

    public void start()
    {
        scanner = new PortScanner();
        takeFingerPrints();
        startNonSerial();
        List<String> allDevices = config.getAllDevices();
        List<String> allPorts = SerialChannel.getAllPorts();
        if (allPorts.equals(allDevices))
        {
            startSerial();
        }
        else
        {
            fingerPrintMap.forEach((endpoint,fingerPrint)->distinguishMap.add(fingerPrint, endpoint));
        }
    }
    
    private void takeFingerPrints()
    {
        for (EndpointType endpointType : config.getRouterEndpoints())
        {
            nameMap.put(endpointType.getName(), endpointType);
            Set<String> fingerPrint = new HashSet<>();
            if (endpointType instanceof SerialType)
            {
                SerialType serialType = (SerialType) endpointType;
                for (RouteType route : serialType.getRoute())
                {
                    fingerPrint.add(route.getPrefix());
                }
                fingerPrintMap.put(endpointType, fingerPrint);
                if (endpointType instanceof SeatalkType)
                {
                    scanChoises.add(PortType.SEA_TALK);
                }
                else
                {
                    if (endpointType instanceof Nmea0183Type)
                    {
                        scanChoises.add(PortType.NMEA);
                    }
                    else
                    {
                        if (endpointType instanceof Nmea0183HsType)
                        {
                            scanChoises.add(PortType.NMEA_HS);
                        }
                    }
                }
            }
        }
    }

    private void startSerial()
    {
        for (EndpointType endpointType : config.getRouterEndpoints())
        {
            if (endpointType instanceof SerialType)
            {
                Endpoint endpoint = EndpointFactory.getInstance(endpointType, this, ringBufferSize);
                Future<?> future = POOL.submit(endpoint);
                futureMap.put(future, endpoint);
            }
        }
    }

    private void startNonSerial()
    {
        for (EndpointType endpointType : config.getRouterEndpoints())
        {
            if (!(endpointType instanceof SerialType))
            {
                Endpoint endpoint = EndpointFactory.getInstance(endpointType, this, ringBufferSize);
                Future<?> future = POOL.submit(endpoint);
                futureMap.put(future, endpoint);
            }
        }
    }

    public void addSource(String src, Endpoint endpoint)
    {
        sources.put(src, endpoint);
    }
    @Override
    public int send(String target, ByteBuffer bb) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean kill(String target)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private class ThreadMonitor implements Runnable
    {

        @Override
        public void run()
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
}
