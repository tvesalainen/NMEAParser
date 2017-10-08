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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Future;
import org.vesalainen.comm.channel.SerialChannel;
import org.vesalainen.math.Sets;
import org.vesalainen.nmea.jaxb.router.EndpointType;
import org.vesalainen.nmea.jaxb.router.Nmea0183HsType;
import org.vesalainen.nmea.jaxb.router.Nmea0183Type;
import org.vesalainen.nmea.jaxb.router.RouteType;
import org.vesalainen.nmea.jaxb.router.SeatalkType;
import org.vesalainen.nmea.jaxb.router.SerialType;
import static org.vesalainen.nmea.router.ThreadPool.POOL;
import org.vesalainen.nmea.router.endpoint.EndpointFactory;
import org.vesalainen.nmea.router.scanner.PortScanner;
import org.vesalainen.nmea.router.scanner.PortScanner.ScanResult;
import org.vesalainen.nmea.script.RouterEngine;
import org.vesalainen.util.EnumMapSet;
import org.vesalainen.util.MapSet;
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
    private Map<SerialType,Set<String>> fingerPrintMap = new HashMap<>();
    private Set<String> differenceSet = new HashSet<>();
    private MapSet<PortType,SerialType> scanChoises = new EnumMapSet<>(PortType.class);
    private Map<SerialType,Set<String>> lostMap = new HashMap<>();
    private PortScanner portScanner;
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

    public void start() throws IOException
    {
        portScanner = new PortScanner();
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
            Sets.assign(Sets.symmetricDifference(fingerPrintMap.values()), differenceSet);
        }
        createScanChoises();
        portScanner.setPorts(SerialChannel.getFreePorts());
        portScanner.setChannelSuppliers(scanChoises.keySet());
        portScanner.scan(this::foundPort, differenceSet);
    }
    
    private void foundPort(ScanResult scanResult)
    {
        SerialType serialType = null;
        for (Entry<SerialType,Set<String>> entry : fingerPrintMap.entrySet())
        {
            if (Sets.intersect(scanResult.getFingerPrint(), entry.getValue()))
            {
                serialType = entry.getKey();
                break;
            }
        }
        if (serialType != null)
        {
            startEndpoint(serialType);
            scanChoises.removeItem(scanResult.getPortType(), serialType);
            lostMap.remove(serialType);
            Sets.assign(Sets.symmetricDifference(lostMap.values()), differenceSet);
        }
        else
        {
            severe("no port for %s", scanResult);
        }
    }
    private void createScanChoises()
    {
        for (SerialType serialType : lostMap.keySet())
        {
            scanChoises.add(getPortType(serialType), serialType);
        }
        config("scan choises %s", scanChoises);
    }
    private PortType getPortType(SerialType serialType)
    {
        if (serialType instanceof SeatalkType)
        {
            return PortType.SEA_TALK;
        }
        else
        {
            if (serialType instanceof Nmea0183Type)
            {
                return PortType.NMEA;
            }
            else
            {
                if (serialType instanceof Nmea0183HsType)
                {
                    return PortType.NMEA_HS;
                }
            }
        }
        throw new IllegalArgumentException(serialType+" conflicts with PortType");
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
                fingerPrintMap.put(serialType, fingerPrint);
            }
        }
        lostMap.putAll(fingerPrintMap);
    }

    private void startSerial()
    {
        for (EndpointType endpointType : config.getRouterEndpoints())
        {
            if (endpointType instanceof SerialType)
            {
                startEndpoint(endpointType);
                lostMap.remove(endpointType);
            }
        }
    }

    private void startNonSerial()
    {
        for (EndpointType endpointType : config.getRouterEndpoints())
        {
            if (!(endpointType instanceof SerialType))
            {
                startEndpoint(endpointType);
            }
        }
    }

    private void startEndpoint(EndpointType endpointType)
    {
        Endpoint endpoint = EndpointFactory.getInstance(endpointType, this, ringBufferSize);
        Future<?> future = POOL.submit(endpoint);
        futureMap.put(future, endpoint);
        config("started %s", endpoint);
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
