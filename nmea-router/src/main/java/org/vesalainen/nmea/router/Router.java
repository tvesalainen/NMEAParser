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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import static java.util.logging.Level.*;
import org.vesalainen.comm.channel.SerialChannel;
import org.vesalainen.math.SymmetricDifferenceMatcher;
import org.vesalainen.nmea.jaxb.router.EndpointType;
import org.vesalainen.nmea.jaxb.router.RouteType;
import org.vesalainen.nmea.jaxb.router.SerialType;
import static org.vesalainen.nmea.router.RouterManager.POOL;
import org.vesalainen.nmea.router.endpoint.EndpointFactory;
import org.vesalainen.nmea.router.scanner.PortScanner;
import org.vesalainen.nmea.router.scanner.PortScanner.ScanResult;
import org.vesalainen.nmea.script.RouterEngine;
import org.vesalainen.util.LongMap;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Router extends JavaLogging implements RouterEngine
{
    private static final long MAX_RESTART_DELAY = 100000;
    private final RouterConfig config;
    private final Map<String,EndpointType> endpointTypeMap = new HashMap<>();
    private final Map<Future,Endpoint> futureMap = new ConcurrentHashMap<>();
    private final Set<SerialType> serialSet = new HashSet<>();
    private final Map<PortType,SymmetricDifferenceMatcher<String,SerialType>> portMatcher = new EnumMap<>(PortType.class);
    private final Map<String,PortType> lastPortType = new HashMap<>();
    private PortScanner portScanner;
    private final long monitorDelay;
    private final long closeDelay;
    private final ExecutorCompletionService starter = new ExecutorCompletionService(POOL);
    private final LongMap<EndpointType> startTimeMap = new LongMap<>();
    private final Map<EndpointType,Endpoint> endpointMap = new HashMap<>();
    private List<String> portsNow;

    public Router(RouterConfig config)
    {
        super(Router.class);
        this.config = config;
        monitorDelay = config.getMonitorDelay();
        closeDelay = config.getCloseDelay();
        SerialChannel.debug(config.isNativeDebug());
    }
    /**
     * Starts router returns true if port configuration has changed during the
     * run
     * @return
     * @throws IOException 
     */
    public boolean start() throws IOException
    {
        config("starting %s", Version.getVersion());
        portsNow = SerialChannel.getAllPorts();
        populateEndpoints();
        populatePortMatcher();
        portScanner = new PortScanner(POOL, config.getDontScan(), lastPortType);
        portScanner.setCheckDelay(monitorDelay);
        portScanner.setCloseDelay(closeDelay);
        portScanner.setFingerPrintDelay(Long.MAX_VALUE);
        List<String> configDevices = config.getAllDevices();
        config("last ports %s", configDevices);
        config("ports now  %s", portsNow);
        portScanner.scan(this::resolvedPort, portMatcher);
        config("started scanner");
        startNonSerial();
        while (true)
        {
            try
            {
                Future future = starter.take();
                if (portsNow.equals(SerialChannel.getAllPorts()))
                {
                    Endpoint endpoint = futureMap.get(future);
                    if (endpoint != null)
                    {
                        EndpointType endpointType = endpoint.getEndpointType();
                        endpointMap.remove(endpointType);
                        long elapsed = System.currentTimeMillis() - startTimeMap.getLong(endpointType);
                        long delay = elapsed > 0 ? MAX_RESTART_DELAY / elapsed : MAX_RESTART_DELAY;
                        POOL.schedule(()->startEndpoint(endpointType), delay, TimeUnit.MILLISECONDS);
                        config("restart %s after %d millis", endpoint, delay);
                    }
                    else
                    {
                        config("cancelled task - not restarted");
                    }
                }
                else
                {
                    severe("%s -> %s during run HW problem!", configDevices, portsNow);
                    return true;    // port configuration has changed during the run
                                    // hw problem
                }
            }
            catch (InterruptedException ex)
            {
                return false;   
            }
        }
    }
    private void resolvedPort(ScanResult scanResult)
    {
        config("resolved port %s", scanResult);
        try
        {
            config.changeDevice(scanResult.getSerialType(), scanResult.getPort());
            SerialType serialType = scanResult.getSerialType();
            startEndpoint(serialType);
            config("started resolved port %s", scanResult.getPort());
        }
        catch (IOException ex)
        {
            log(SEVERE, ex, "%s %s", scanResult, ex.getMessage());
        }
    }
    private void populateEndpoints()
    {
        config.getRouterEndpoints().forEach((endpointType)->
        {
            if (endpointType instanceof SerialType)
            {
                SerialType serialType = (SerialType) endpointType;
                serialSet.add(serialType);
            }
            endpointTypeMap.put(endpointType.getName(), endpointType);
        });
    }
    private void populatePortMatcher()
    {
        for (SerialType serialType : serialSet)
        {
            for (PortType portType : PortType.getPortType(serialType))
            {
                SymmetricDifferenceMatcher<String, SerialType> sdm = portMatcher.get(portType);
                if (sdm == null)
                {
                    sdm = new SymmetricDifferenceMatcher<>();
                    portMatcher.put(portType, sdm);
                }
                for (RouteType route : serialType.getRoute())
                {
                    String prefix = route.getPrefix();
                    sdm.map(prefix, serialType);
                    config("add finger print %s -> %s", portType, prefix);
                }
                lastPortType.put(serialType.getDevice(), portType);
            }
        }
    }

    private void startNonSerial()
    {
        config.getRouterEndpoints().forEach((endpointType)->
        {
            if (!(endpointType instanceof SerialType))
            {
                startEndpoint(endpointType);
            }
        });
    }

    private void startEndpoint(EndpointType endpointType)
    {
        Endpoint endpoint = EndpointFactory.getInstance(endpointType, this);
        endpointMap.put(endpointType, endpoint);
        Future<?> future = starter.submit(endpoint, null);
        startTimeMap.put(endpointType, System.currentTimeMillis());
        futureMap.put(future, endpoint);
        config("started %s", endpoint);
        checkScannerState();
    }
    private synchronized void checkScannerState()
    {
        boolean allResolved = portMatcher.values().stream().allMatch((sdm)->sdm.getUnresolved().isEmpty());
        if (allResolved && portScanner != null)
        {
            try
            {
                config.updateAllDevices(portsNow);
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
            config("updated config ports %s", portsNow);
            portScanner.stop();
            portScanner = null;
            config("all ports resolved port scanner stopped");
        }
    }
    private Set<SerialType> getUnresolved()
    {
        Set<SerialType> set = new HashSet<>();
        portMatcher.values().stream().map((sdm)->sdm.getUnresolved()).forEach((s)->set.addAll(s));
        return set;
    }
    @Override
    public int send(String target, ByteBuffer bb) throws IOException
    {
        Endpoint endpoint = Endpoint.get(target);
        if (endpoint != null)
        {
            return endpoint.write(bb);
        }
        return 0;
    }

    @Override
    public boolean kill(String target)
    {
        config("killing %s", target);
        EndpointType endpointType = endpointTypeMap.get(target);
        if (endpointType != null)
        {
            if (endpointType instanceof SerialType)
            {
                SerialType serialType = (SerialType) endpointType;
                for (PortType portType : PortType.getPortType(serialType))
                {
                    SymmetricDifferenceMatcher<String, SerialType> sdm = portMatcher.get(portType);
                    sdm.unmap(serialType);
                    config("removed %s from portMatcher", target);
                }
                checkScannerState();
            }
        }
        return cancelEndpoint(target);
    }
    public boolean cancelEndpoint(String target)
    {
        config("cancelling %s", target);
        Endpoint endpoint = Endpoint.get(target);
        if (endpoint != null)
        {
            EndpointType endpointType = endpoint.getEndpointType();
            if (endpointType instanceof SerialType)
            {
                Future future = null;
                for (Entry<Future,Endpoint> e : futureMap.entrySet())
                {
                    if (endpoint.equals(e.getValue()))
                    {
                        future = e.getKey();
                        break;
                    }
                }
                if (future != null)
                {
                    future.cancel(true);
                    futureMap.remove(future);
                    config("cancelled %s", target);
                    return true;
                }
                else
                {
                    warning("%s was not running", target);
                }
            }
            else
            {
                warning("%s not serial", target);
            }
        }
        else
        {
            fine("%s not started", target);
        }
        return false;
    }

    public RouterConfig getConfig()
    {
        return config;
    }

}
