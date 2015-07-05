/*
 * Copyright (C) 2015 tkv
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.vesalainen.comm.channel.SerialChannel;
import org.vesalainen.comm.channel.SerialChannel.Speed;
import org.vesalainen.nmea.jaxb.router.EndpointType;
import org.vesalainen.nmea.jaxb.router.RouteType;
import org.vesalainen.nmea.router.Router.Endpoint;
import org.vesalainen.nmea.router.Router.SerialEndpoint;
import org.vesalainen.util.Bijection;
import org.vesalainen.util.HashMapList;
import org.vesalainen.util.HashMapSet;
import org.vesalainen.util.MapList;
import org.vesalainen.util.MapSet;

/**
 *
 * @author tkv
 */
public final class NMEAMatcherManager
{
    private final MapSet<SerialChannel.Speed,String> ambiguousPrefixes = new HashMapSet<>();
    private final MapList<Speed, EndpointType> speedMap;
    private final MapList<String,String> prefixes = new HashMapList<>();
    private final Bijection<Endpoint, EndpointType> endpointBijection;

    public NMEAMatcherManager(Bijection<Endpoint,EndpointType> endpointMap, MapList<Speed, EndpointType> speedMap)
    {
        this.endpointBijection = endpointMap;
        this.speedMap = speedMap;
        
        for (SerialChannel.Speed speed : speedMap.keySet())
        {
            setMatchers(speed);
        }
    }

    public void match(SerialEndpoint serialEndpoint)
    {
        Speed speed = serialEndpoint.getSpeed();
        EndpointType endpointType = endpointBijection.getSecond(serialEndpoint);
        speedMap.get(speed).remove(endpointType);
        NMEAMatcher wm = null;
        for (RouteType rt : endpointType.getRoute())
        {
            List<String> targetList = rt.getTarget();
            if (wm == null)
            {
                wm = new NMEAMatcher();
            }
            String prefix = rt.getPrefix();
            wm.addExpression(prefix, new Route(rt));
        }
        if (wm != null)
        {
            wm.compile();
        }
        serialEndpoint.setMatcher(wm);
        setMatchers(speed);
    }
    public void kill(SerialEndpoint se)
    {
        EndpointType et = endpointBijection.getSecond(se);
        endpointBijection.removeFirst(et);
        speedMap.get(se.getSpeed()).remove(et);
    }

    private void setMatchers(Speed speed)
    {
        update(speed);
        for (EndpointType endpointType : speedMap.get(speed))
        {
            NMEAMatcher wm = null;
            List<RouteType> route = endpointType.getRoute();
            if (!endpointType.getRoute().isEmpty())
            {
                for (RouteType rt : route)
                {
                    if (!isAmbiguousPrefix(speed, rt.getPrefix()))
                    {
                        if (wm == null)
                        {
                            wm = new NMEAMatcher();
                        }
                        String prefix = rt.getPrefix();
                        wm.addExpression(prefix, new Route(rt));
                    }
                }
            }
            if (wm != null)
            {
                wm.compile();
                SerialEndpoint serialEndpoint = (SerialEndpoint) endpointBijection.getFirst(endpointType);
                serialEndpoint.setMatcher(wm);
            }
        }
    }
    /**
     * Return true is prefix is ambiguous. Ambiguous prefix cannot be used in 
     * determining which port is which, 
     * because messages prefixed with ambiguous prefixes are received from several ports.
     * @param prefix
     * @return 
     */
    public boolean isAmbiguousPrefix(SerialChannel.Speed speed, String prefix)
    {
        Set<String> set = ambiguousPrefixes.get(speed);
        if (set != null)
        {
            return set.contains(prefix);
        }
        return false;
    }
    private void update(Speed speed)
    {
        ambiguousPrefixes.remove(speed);
        prefixes.clear();
        for (EndpointType et : speedMap.get(speed))
        {
            String name = et.getName();
            for (RouteType rt : et.getRoute())
            {
                String prefix = rt.getPrefix();
                for (Map.Entry<String,List<String>> e : prefixes.entrySet())
                {
                    String key = e.getKey();
                    if (!name.equals(key))
                    {
                        for (String pre : e.getValue())
                        {
                            if (NMEAPrefix.matchesSame(prefix, pre))
                            {
                                ambiguousPrefixes.add(speed, pre);
                            }
                        }
                    }
                }
                prefixes.add(name, prefix);
            }
        }
    }

    public void allMatched()
    {
        MapList<NMEAPrefix,Route> mapList = new HashMapList<>();
        List<Route> backups = new ArrayList<>();
        for (Entry<Endpoint, EndpointType> entry : endpointBijection.entrySet())
        {
            Endpoint ep = entry.getKey();
            NMEAMatcher matcher = ep.matcher;
            if (matcher != null)
            {
                for (Route route : matcher.getRoutes())
                {
                    if (route.isBackup())
                    {
                        backups.add(route);
                    }
                    else
                    {
                        mapList.add(route.getPrefix(), route);
                    }
                }
            }
        }
        for (Route route : backups)
        {
            NMEAPrefix prefix = route.getPrefix();
            route.setBackupSources(mapList.get(prefix));
        }
    }

}
