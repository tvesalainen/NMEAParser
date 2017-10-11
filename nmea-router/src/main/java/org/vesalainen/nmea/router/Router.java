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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import static java.util.logging.Level.*;
import org.vesalainen.comm.channel.SerialChannel;
import org.vesalainen.math.Sets;
import org.vesalainen.math.SymmetricDifferenceMap;
import org.vesalainen.nmea.jaxb.router.EndpointType;
import org.vesalainen.nmea.jaxb.router.RouteType;
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
    private final Map<String,Endpoint> sources = new HashMap<>();
    private final Map<Future,Endpoint> futureMap = new ConcurrentHashMap<>();
    private final Map<String,EndpointType> nameMap = new HashMap<>();
    private final Map<SerialType,Set<String>> fingerPrintMap = new HashMap<>();
    private final SymmetricDifferenceMap<String,SerialType> uniqueMap = new SymmetricDifferenceMap<>();
    private final MapSet<PortType,SerialType> scanChoises = new EnumMapSet<>(PortType.class);
    private final Set<SerialType> lostSet = new HashSet<>();
    private PortScanner portScanner;
    private final long monitorDelay;
    private final long closeDelay;
    private ScheduledFuture<?> monitorFuture;

    public Router(RouterConfig config)
    {
        super(Router.class);
        this.config = config;
        monitorDelay = config.getMonitorDelay();
        closeDelay = config.getCloseDelay();
    }

    public void start() throws IOException
    {
        portScanner = new PortScanner();
        takeFingerPrints();
        startNonSerial();
        createScanChoises();
        portScanner.setChannelSuppliers(scanChoises.keySet());
        portScanner.setCheckDelay(monitorDelay);
        portScanner.setCloseDelay(closeDelay);
        portScanner.setFingerPrintDelay(Long.MAX_VALUE);
        List<String> allDevices = config.getAllDevices();
        List<String> allPorts = SerialChannel.getAllPorts();
        if (allPorts.equals(allDevices))
        {
            startSerial();
            QuickStartMonitor monitor = new QuickStartMonitor();
            monitorFuture = POOL.schedule(monitor, monitorDelay, TimeUnit.MILLISECONDS);
        }
        else
        {
            fingerPrintMap.forEach((k,s)->uniqueMap.add(s, k));
            portScanner.setPorts(SerialChannel.getFreePorts());
            ensureScanning();
        }
        Restarter restarter = new Restarter();
        ScheduledFuture<?> restarterFuture = POOL.scheduleWithFixedDelay(restarter, monitorDelay, monitorDelay, TimeUnit.MILLISECONDS);
        try
        {
            restarterFuture.get();
        }
        catch (InterruptedException | ExecutionException ex)
        {
            throw new IOException(ex);
        }
    }
    private void ensureScanning() throws IOException
    {
        if (!portScanner.isScanning())
        {
            portScanner.scan(this::foundPort, uniqueMap);
        }
    }
    private void foundPort(ScanResult scanResult)
    {
        try
        {
            config.changeDevice(scanResult.getSerialType(), scanResult.getPort());
            SerialType serialType = scanResult.getSerialType();
            config("found port %s", serialType);
            startEndpoint(serialType);
            scanChoises.removeItem(scanResult.getPortType(), serialType);
            lostSet.remove(serialType);
            Set<String> set = fingerPrintMap.get(serialType);
            uniqueMap.remove(set, serialType);
        }
        catch (IOException ex)
        {
            log(SEVERE, ex, "%s %s", scanResult, ex.getMessage());
        }
    }
    private void createScanChoises()
    {
        for (SerialType serialType : lostSet)
        {
            scanChoises.add(PortType.getPortType(serialType), serialType);
        }
        config("scan choises %s", scanChoises);
    }
    private void takeFingerPrints()
    {
        for (EndpointType endpointType : config.getRouterEndpoints())
        {
            nameMap.put(endpointType.getName(), endpointType);
            if (endpointType instanceof SerialType)
            {
                Set<String> fingerPrint = new HashSet<>();
                SerialType serialType = (SerialType) endpointType;
                for (RouteType route : serialType.getRoute())
                {
                    fingerPrint.add(route.getPrefix());
                }
                fingerPrintMap.put(serialType, fingerPrint);
            }
        }
        lostSet.addAll(fingerPrintMap.keySet());
    }

    private void startSerial()
    {
        for (EndpointType endpointType : config.getRouterEndpoints())
        {
            if (endpointType instanceof SerialType)
            {
                startEndpoint(endpointType);
                lostSet.remove(endpointType);
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
        Endpoint endpoint = EndpointFactory.getInstance(endpointType, this);
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

    private class QuickStartMonitor implements Runnable
    {

        @Override
        public void run()
        {
            config("started QuickStartMonitor");
            try
            {
                synchronized(futureMap)
                {
                    boolean allRunning = true;
                    Iterator<Entry<Future, Endpoint>> iterator = futureMap.entrySet().iterator();
                    while (iterator.hasNext())
                    {
                        Entry<Future, Endpoint> entry = iterator.next();
                        Future future = entry.getKey();
                        Endpoint endpoint = entry.getValue();
                        if (endpoint.getEndpointType() instanceof SerialType)
                        {
                            config("checking %s", endpoint);
                            if (!future.isDone() && handleSerial(future, endpoint))
                            {
                                iterator.remove();
                                allRunning = false;
                            }
                            else
                            {
                                config("ok %s", endpoint);
                            }
                        }
                    }
                    if (allRunning)
                    {
                        config("all serial ports running");
                        portScanner.stop();
                        portScanner = null;
                    }
                }
            }
            catch (IOException ex)
            {
                log(SEVERE, ex, "monitor: %s", ex);
            }
        }

        private boolean handleSerial(Future future, Endpoint endpoint) throws IOException
        {
            SerialType serialType = (SerialType) endpoint.getEndpointType();
            Set<String> exp = fingerPrintMap.get(serialType);
            Set<String> got = endpoint.getFingerPrint();
            if (!Sets.intersect(exp, got))
            {
                config("%s: %s doesn't intersect with %s", endpoint, got, exp);
                future.cancel(true);
                lostSet.add(serialType);
                uniqueMap.add(exp, serialType);
                portScanner.addPort(serialType.getDevice());
                scanChoises.add(PortType.getPortType(serialType), serialType);
                ensureScanning();
                return true;
            }
            return false;
        }

    }
    private class Restarter implements Runnable
    {

        @Override
        public void run()
        {
            synchronized(futureMap)
            {
                Iterator<Entry<Future, Endpoint>> iterator = futureMap.entrySet().iterator();
                while (iterator.hasNext())
                {
                    Entry<Future, Endpoint> entry = iterator.next();
                    Future future = entry.getKey();
                    Endpoint endpoint = entry.getValue();
                    if (future.isDone())
                    {
                        startEndpoint(endpoint.getEndpointType());
                        iterator.remove();
                        config("restarted %s", endpoint);
                    }
                }
            }
        }
    }
}
