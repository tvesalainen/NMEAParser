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
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ConfigCreator extends JavaLogging
{
    private RouterConfig config;
    private Set<String> names = new HashSet<>();
    private boolean hasAis;
    private String needsSpeed;
    private RouteType providesSpeed;

    public ConfigCreator()
    {
        super(ConfigCreator.class);
    }
    
    public RouterConfig createConfig(File file) throws IOException
    {
        this.config = new RouterConfig(file);
        config.getNmeaType().getAllDevices().addAll(SerialChannel.getAllPorts());
        
        PortScanner portScanner = new PortScanner()
                .setChannelSuppliers(EnumSet.of(NMEA, NMEA_HS, SEA_TALK))
                .setPorts(SerialChannel.getFreePorts());
        portScanner.scan(this::addDevice);
        
        MulticastNMEAType net = config.createMulticastNMEAType();
        net.setName("Net");
        net.setAddress("224.0.0.3");
        net.setEnable(true);
        RouteType route = config.createRouteTypeFor("Net");
        route.setPrefix("$");
        route.getTarget().add("Listener");
        route.setComment("Send all NMEA sentences to TCP Listener");

        TcpEndpointType listener = config.createTcpEndpointType();
        listener.setName("Listener");
        listener.setPort(10110);
        listener.setEnable(true);
        RouteType listenerRoute = config.createRouteTypeFor(listener);
        listenerRoute.setPrefix("$");
        listenerRoute.getTarget().add("Net");
        listenerRoute.setComment("E.g. Autopilot sentences from Open CPN");
        
        portScanner.waitScanner();
        
        if (needsSpeed != null && providesSpeed != null)
        {
            providesSpeed.getTarget().add(needsSpeed);
        }
        if (hasAis)
        {
            DatagramType marineTraffic = config.createDatagramType();
            marineTraffic.setName("MarineTraffic");
            marineTraffic.setAddress("5.9.207.224");
            marineTraffic.setPort(5321);
        
            RouteType aisRoute = config.createRouteTypeFor(marineTraffic);
            aisRoute.setPrefix("AI");
            aisRoute.setComment("Send all AIS sentences to TCP Listener");
        }
        return config;
    }
    private void addDevice(ScanResult scanResult)
    {
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
                route.getTarget().add("Net");
                if (entry.getKey().equals(VHW))
                {
                    providesSpeed = route;
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
            return createUniqueName("AIS");
        }
        else
        {
            if (any(talkerIds, GP) || any(messageTypes, RMC))
            {
                return createUniqueName("GPS");
            }
            else
            {
                if (any(messageTypes, DBK, DBS, DBT))
                {
                    return createUniqueName("Sounder");
                }
                else
                {
                    if (any(messageTypes, VHW))
                    {
                        return createUniqueName("Log");
                    }
                    else
                    {
                        if (any(messageTypes, VWR, MWD, MWV))
                        {
                            return createUniqueName("Wind");
                        }
                        else
                        {
                            if (any(messageTypes, THS, HDG, HDM, HDT))
                            {
                                return createUniqueName("Compass");
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
