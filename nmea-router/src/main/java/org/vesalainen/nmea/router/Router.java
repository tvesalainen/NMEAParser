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
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import static java.util.logging.Level.*;
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
import org.vesalainen.util.LongMap;
import org.vesalainen.util.MapSet;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Router extends JavaLogging implements RouterEngine
{
    private static final long MAX_RESTART_DELAY = 100000;
    private final RouterConfig config;
    private final Map<String,Endpoint> sources = new HashMap<>();
    private final Map<Future,Endpoint> futureMap = new ConcurrentHashMap<>();
    private final Set<SerialType> serialSet = new HashSet<>();
    private final SymmetricDifferenceMatcher<String,SerialType> portMatcher = new SymmetricDifferenceMatcher<>();
    private final MapSet<PortType,SerialType> scanChoises = new EnumMapSet<>(PortType.class);
    private PortScanner portScanner;
    private final long monitorDelay;
    private final long closeDelay;
    private final ExecutorCompletionService starter = new ExecutorCompletionService(POOL);
    private final LongMap<EndpointType> startTimeMap = new LongMap<>();
    private final Map<EndpointType,Endpoint> endpointMap = new HashMap<>();

    public Router(RouterConfig config)
    {
        super(Router.class);
        this.config = config;
        monitorDelay = config.getMonitorDelay();
        closeDelay = config.getCloseDelay();
    }

    public void start() throws IOException
    {
        config("starting %s", Version.getVersion());
        portScanner = new PortScanner(POOL);
        populateSerialSet();
        populatePortMatcher();
        startNonSerial();
        portScanner.setChannelSuppliers(scanChoises.keySet());
        portScanner.setCheckDelay(monitorDelay);
        portScanner.setCloseDelay(closeDelay);
        portScanner.setFingerPrintDelay(Long.MAX_VALUE);
        List<String> allDevices = config.getAllDevices();
        config("last ports %s", allDevices);
        List<String> allPorts = SerialChannel.getAllPorts();
        config("ports now  %s", allPorts);
        if (allPorts.equals(allDevices))
        {
            config("ports seems to be the same as last run - try the same config");
            startAllSerial();
            POOL.schedule(this::quickStartHandler, monitorDelay, TimeUnit.MILLISECONDS);
        }
        else
        {
            config("port configuration has changed - start scanner");
            config.updateAllDevices(allPorts);
            ensureScanning();
        }
        while (true)
        {
            try
            {
                Future future = starter.take();
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
            }
            catch (InterruptedException ex)
            {
                return;
            }
        }
    }
    private void ensureScanning() throws IOException
    {
        if (!portScanner.isScanning())
        {
            portScanner.scan(this::resolvedPort, portMatcher);
            config("started scanner");
        }
    }
    private void resolvedPort(ScanResult scanResult)
    {
        config("resolved port %s", scanResult);
        try
        {
            config.changeDevice(scanResult.getSerialType(), scanResult.getPort());
            SerialType serialType = scanResult.getSerialType();
            POOL.schedule(()->startEndpoint(serialType), closeDelay, TimeUnit.MILLISECONDS);
            config("starting resolved port %s after %d millis", scanResult.getPort(), closeDelay);
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
        endpointMap.put(endpointType, endpoint);
        Future<?> future = starter.submit(endpoint, null);
        startTimeMap.put(endpointType, System.currentTimeMillis());
        futureMap.put(future, endpoint);
        config("started %s", endpoint);
        if (portMatcher.getUnresolved().isEmpty())
        {
            portScanner.stop();
            portScanner = null;
            config("all ports resolved port scanner stopped");
        }
    }
    public void addSource(String src, Endpoint endpoint)
    {
        sources.put(src, endpoint);
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
        Endpoint endpoint = Endpoint.get(target);
        if (endpoint != null)
        {
            EndpointType endpointType = endpoint.getEndpointType();
            if (endpointType instanceof SerialType)
            {
                SerialType serialType = (SerialType) endpointType;
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
                    config("killed %s", target);
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
            warning("%s to be killed not found", target);
        }
        return false;
    }

    private void quickStartHandler()
    {
        config("started quickStartHandler");
        Map<Endpoint,Set<String>> fingerPrints = new HashMap<>();
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
                        String name = serialType.getName();
                        config("set %s for scanning", name);
                        kill(name);
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

    public RouterConfig getConfig()
    {
        return config;
    }

}
