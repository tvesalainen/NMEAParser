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
package org.vesalainen.nmea.router.scanner;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.vesalainen.comm.channel.SerialChannel;
import org.vesalainen.nmea.jaxb.router.DatagramType;
import org.vesalainen.nmea.jaxb.router.MulticastNMEAType;
import org.vesalainen.nmea.jaxb.router.RouteType;
import org.vesalainen.nmea.jaxb.router.SerialType;
import org.vesalainen.nmea.jaxb.router.TcpEndpointType;
import static org.vesalainen.nmea.router.PortType.*;
import org.vesalainen.nmea.router.RouterConfig;
import org.vesalainen.nmea.router.scanner.PortScanner.ScanResult;
import org.vesalainen.parsers.nmea.MessageType;
import static org.vesalainen.parsers.nmea.MessageType.*;
import org.vesalainen.parsers.nmea.TalkerId;
import static org.vesalainen.parsers.nmea.TalkerId.*;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ConfigCreator extends JavaLogging
{
    private static final String MULTICAST_ADDRESS = "224.0.0.3";
    private static final String MARINE_TRAFFIC = "MarineTraffic";
    private static final String TCP_LISTENER = "Listener";
    private static final String MULTICAST = "Net";
    private static final String AIS = "AIS";
    private static final String GPS = "GPS";
    private static final String WIND = "Wind";
    private static final String COMPASS = "Compass";
    private static final String LOG = "Log";
    private static final String SOUNDER = "Sounder";
    private CachedScheduledThreadPool pool = new CachedScheduledThreadPool();
    private RouterConfig config;
    private Set<String> names = new HashSet<>();
    private boolean hasAis;
    private String needsSpeed;
    private RouteType providesSpeed;
    private Set<String> scannedPorts = new HashSet<>();

    public ConfigCreator()
    {
        super(ConfigCreator.class);
    }
    
    public RouterConfig createConfig(File file) throws IOException
    {
        this.config = new RouterConfig(file);
        config.getNmeaType().getAllDevices().addAll(SerialChannel.getAllPorts());
        
        PortScanner portScanner = new PortScanner(pool, scannedPorts, Collections.EMPTY_MAP);
        portScanner.scan(this::addDevice);
        
        Set<String> fingerPrint = new HashSet<>();
        NetScanner netScanner = new NetScanner(MULTICAST_ADDRESS, fingerPrint);
        Future<Set<String>> netFuture = pool.submit(netScanner);
        
        TcpEndpointType listener = config.createTcpEndpointType();
        listener.setName(TCP_LISTENER);
        listener.setPort(10110);
        listener.setEnable(true);
        RouteType listenerRoute = config.createRouteTypeFor(listener);
        listenerRoute.setPrefix("$");
        listenerRoute.getTarget().add(MULTICAST);
        listenerRoute.setComment("E.g. Autopilot sentences from Open CPN");
        
        portScanner.waitScanner(2, TimeUnit.MINUTES);
        pool.shutdownNow();
        
        if (needsSpeed != null && providesSpeed != null)
        {
            providesSpeed.getTarget().add(needsSpeed);
        }
        if (hasAis)
        {
            DatagramType marineTraffic = config.createDatagramType();
            marineTraffic.setName(MARINE_TRAFFIC);
            marineTraffic.setAddress("5.9.207.224");
            marineTraffic.setPort(5321);
        }
        netFuture.cancel(true);
        MulticastNMEAType net = config.createMulticastNMEAType();
        net.setName(MULTICAST);
        net.setAddress(MULTICAST_ADDRESS);
        net.setEnable(true);
        if (fingerPrint.isEmpty())
        {
            RouteType route = config.createRouteTypeFor(net);
            route.setPrefix("$");
            route.getTarget().add(TCP_LISTENER);
            route.setComment("Send all NMEA sentences to TCP Listener");
        }
        else
        {
            Map<MessageType,String> messageTypes = getMessageTypes(fingerPrint);
            for (Entry<MessageType,String> entry : messageTypes.entrySet())
            {
                RouteType route = config.createRouteTypeFor(net);
                route.setPrefix(entry.getValue());
                route.getTarget().add(TCP_LISTENER);
                route.setComment(entry.getKey().getDescription());
            }
        }
        return config;
    }
    private void addDevice(ScanResult scanResult)
    {
        scannedPorts.add(scanResult.getPort());
        if (scanResult.getFingerPrint().isEmpty())
        {
            config("empty %s", scanResult);
        }
        else
        {
            SerialType serial = null;
            switch (scanResult.getPortType())
            {
                case NMEA:
                    serial = config.createNmea0183Type();
                    break;
                case NMEA_HS:
                    serial = config.createNmea0183HsType();
                    break;
                case SEA_TALK:
                    serial = config.createSeatalkType();
                    break;
                default:
                    throw new UnsupportedOperationException(scanResult.getPortType()+" not supported");
            }
            Map<TalkerId,String> talkerIds = getTalkerIds(scanResult.getFingerPrint());
            Map<MessageType,String> messageTypes = getMessageTypes(scanResult.getFingerPrint());
            String name = createName(talkerIds.keySet(), messageTypes.keySet());
            serial.setName(name);
            serial.setDevice(scanResult.getPort());
            serial.setEnable(true);
            for (Entry<MessageType,String> entry : messageTypes.entrySet())
            {
                RouteType route = config.createRouteTypeFor(serial);
                route.setPrefix(entry.getValue());
                route.setComment(entry.getKey().getDescription());
                route.getTarget().add(MULTICAST);
                route.getTarget().add(TCP_LISTENER);
                if (entry.getKey().equals(VHW))
                {
                    providesSpeed = route;
                }
                if (entry.getKey().equals(VDM) || entry.getKey().equals(VDO))
                {
                    route.getTarget().add(MARINE_TRAFFIC);
                    hasAis = true;
                }
            }
            if (any(messageTypes.keySet(), VWR, MWD, MWV) && !messageTypes.containsKey(VHW))
            {
                needsSpeed = name;
            }
        }
    }
    private String createName(Set<TalkerId> talkerIds, Set<MessageType> messageTypes)
    {
        if (any(talkerIds, AI) || any(messageTypes, VDM, VDO))
        {
            return createUniqueName(AIS);
        }
        else
        {
            if (any(talkerIds, GP) || any(messageTypes, RMC))
            {
                return createUniqueName(GPS);
            }
            else
            {
                if (any(messageTypes, DBK, DBS, DBT))
                {
                    return createUniqueName(SOUNDER);
                }
                else
                {
                    if (any(messageTypes, VHW))
                    {
                        return createUniqueName(LOG);
                    }
                    else
                    {
                        if (any(messageTypes, VWR, MWD, MWV))
                        {
                            return createUniqueName(WIND);
                        }
                        else
                        {
                            if (any(messageTypes, THS, HDG, HDM, HDT))
                            {
                                return createUniqueName(COMPASS);
                            }
                            else
                            {
                                return createUniqueName("NMEA_1");
                            }
                        }
                    }
                }
            }
        }
    }
    private <T> boolean any(Set<T> set, T... items)
    {
        for (T i : items)
        {
            if (set.contains(i))
            {
                return true;
            }
        }
        return false;
    }
    private String createUniqueName(String name)
    {
        if (!names.contains(name))
        {
            names.add(name);
            return name;
        }
        else
        {
            int idx = name.lastIndexOf('_');
            if (idx == -1)
            {
                return createUniqueName(name+"_2");
            }
            else
            {
                int num = Integer.parseInt(name.substring(idx+1));
                return createUniqueName(String.format("%s_%d", name.substring(0, idx), num+1));
            }
        }
    }
    private Map<TalkerId,String> getTalkerIds(Set<String> fingerPrint)
    {
        EnumMap<TalkerId,String> map = new EnumMap(TalkerId.class);
        for (String prefix : fingerPrint)
        {
            if (!prefix.startsWith("$P") && prefix.length() == 6)
            {
                try
                {
                    map.put(TalkerId.valueOf(prefix.substring(1, 3)), prefix);
                }
                catch (IllegalArgumentException ex)
                {
                }
            }
        }
        return map;
    }
    private Map<MessageType,String> getMessageTypes(Set<String> fingerPrint)
    {
        EnumMap<MessageType,String> set = new EnumMap(MessageType.class);
        for (String prefix : fingerPrint)
        {
            if (!prefix.startsWith("$P") && prefix.length() == 6)
            {
                try
                {
                    set.put(MessageType.valueOf(prefix.substring(3)), prefix);
                }
                catch (IllegalArgumentException ex)
                {
                }
            }
        }
        return set;
    }
}
