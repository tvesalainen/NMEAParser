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
import java.util.logging.Level;
import static java.util.logging.Level.*;
import java.util.logging.Logger;
import org.vesalainen.comm.channel.SerialChannel;
import org.vesalainen.math.SymmetricDifferenceMatcher;
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
    private final Set<SerialType> serialSet = new HashSet<>();
    private final SymmetricDifferenceMatcher<String,SerialType> portMatcher = new SymmetricDifferenceMatcher<>();
    private final MapSet<PortType,SerialType> scanChoises = new EnumMapSet<>(PortType.class);
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
        populateSerialSet();
        populatePortMatcher();
        startNonSerial();
        portScanner.setChannelSuppliers(scanChoises.keySet());
        portScanner.setCheckDelay(monitorDelay);
        portScanner.setCloseDelay(closeDelay);
        portScanner.setFingerPrintDelay(Long.MAX_VALUE);
        List<String> allDevices = config.getAllDevices();
        List<String> allPorts = SerialChannel.getAllPorts();
        if (allPorts.equals(allDevices))
        {
            startAllSerial();
            monitorFuture = POOL.schedule(this::quickStartHandler, monitorDelay, TimeUnit.MILLISECONDS);
        }
        else
        {
            portScanner.setPorts(SerialChannel.getFreePorts());
            ensureScanning();
        }
        ScheduledFuture<?> restarterFuture = POOL.scheduleWithFixedDelay(this::restarter, monitorDelay, monitorDelay, TimeUnit.MILLISECONDS);
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
            portScanner.scan(this::foundPort, portMatcher);
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
        }
        catch (IOException ex)
        {
            log(SEVERE, ex, "%s %s", scanResult, ex.getMessage());
        }
    }
    private void populateSerialSet()
    {
        for (EndpointType endpointType : config.getRouterEndpoints())
        {
            if (endpointType instanceof SerialType)
            {
                SerialType serialType = (SerialType) endpointType;
                serialSet.add(serialType);
            }
        }
    }
    private void populatePortMatcher()
    {
        scanChoises.clear();
        for (SerialType serialType : serialSet)
        {
            for (RouteType route : serialType.getRoute())
            {
                portMatcher.map(route.getPrefix(), serialType);
            }
            scanChoises.add(PortType.getPortType(serialType), serialType);
        }
    }

    private void startAllSerial()
    {
        for (SerialType serialType : serialSet)
        {
            startEndpoint(serialType);
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

    private void quickStartHandler()
    {
        config("started quickStartHandler");
        Map<Endpoint,Set<String>> fingerPrints = new HashMap<>();
        Map<SerialType,Future> cancelMap = new HashMap<>();
        try
        {
            synchronized(futureMap)
            {
                Iterator<Entry<Future, Endpoint>> iterator = futureMap.entrySet().iterator();
                while (iterator.hasNext())
                {
                    Entry<Future, Endpoint> entry = iterator.next();
                    Future future = entry.getKey();
                    Endpoint endpoint = entry.getValue();
                    EndpointType endpointType = endpoint.getEndpointType();
                    if (endpointType instanceof SerialType)
                    {
                        SerialType serialType = (SerialType) endpointType;
                        config("checking %s", endpoint);
                        if (future.isDone())
                        {
                            iterator.remove();
                        }
                        else
                        {
                            Set<String> fingerPrint = endpoint.getFingerPrint();
                            fingerPrints.put(endpoint, fingerPrint);
                            cancelMap.put(serialType, future);
                        }
                    }
                }
                portMatcher.match(fingerPrints, this::matched);
                Set<SerialType> unresolved = portMatcher.getUnresolved();
                if (unresolved.isEmpty())
                {
                    config("all serial ports running");
                    portScanner = null;
                }
                else
                {
                    for (SerialType serialType : unresolved)
                    {
                        config("set %s for scanning", serialType);
                        Future future = cancelMap.get(serialType);
                        future.cancel(true);
                        futureMap.remove(future);
                        portScanner.addPort(serialType.getDevice());
                    }
                    ensureScanning();
                }
            }
        }
        catch (Throwable ex)
        {
            log(SEVERE, ex, "at quickStartHandler %s", ex.getMessage());
        }
    }
    private void matched(SerialType serialType, Endpoint endpoint)
    {
        config("%s matches %s", serialType, endpoint);
        scanChoises.removeItem(PortType.getPortType(serialType), serialType);
    }

    private void restarter()
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
