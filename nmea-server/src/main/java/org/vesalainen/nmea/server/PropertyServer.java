/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nmea.server;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.vesalainen.code.AbstractPropertySetter;
import org.vesalainen.nmea.server.SseServlet.SseReference;
import org.vesalainen.nmea.server.jaxb.PropertyType;
import org.vesalainen.parsers.nmea.NMEAProperties;
import org.vesalainen.parsers.nmea.ais.AISService;
import org.vesalainen.parsers.nmea.ais.AISService.AISTargetObserver;
import org.vesalainen.parsers.nmea.ais.AISTarget;
import org.vesalainen.util.ConcurrentHashMapSet;
import org.vesalainen.util.LifeCycle;
import org.vesalainen.util.MapSet;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class PropertyServer extends AbstractPropertySetter implements AISTargetObserver
{
    private final Clock clock;
    private final MapSet<String,Property> dispatchMap = new ConcurrentHashMapSet<>();
    private final Map<String,Property> propertyMap = new ConcurrentHashMap<>();
    private final Config config;
    private final CachedScheduledThreadPool executor;
    
    public PropertyServer(Clock clock, Config config, CachedScheduledThreadPool executor)
    {
        super(allNMEAProperties());
        this.clock = clock;
        this.config = config;
        this.executor = executor;
        List<String> sources = new ArrayList<>();
        config.getProperties().forEach((p)->
        {
            Property property = Property.getInstance(executor, p, null);
            String name = p.getName();
            propertyMap.put(name, property);
            String[] srcs = property.getSources();
            if (srcs.length > 0)
            {
                for (String source : srcs)
                {
                    dispatchMap.add(source, property);
                    sources.add(source);
                }
            }
            else
            {
                dispatchMap.add(name, property);
            }
        });
    }

    @Override
    public void observe(LifeCycle status, int mmsi, AISTarget target)
    {
        switch (status)
        {
            case OPEN:
            case UPDATE:
                set("ais", target);
                break;
            case CLOSE:
                AISProperty.remove(target);
                break;
        }
    }

    public void addSse(Map<String,String[]> map, SseReference sseReference, Locale locale)
    {
        String[] arr = map.get("event");
        String event = arr[0];
        String[] properties = map.get("property");
        if (properties == null)
        {
            properties = map.get("property[]");
        }
        for (String name : properties)
        {
            Property p = propertyMap.get(name);
            if (p == null)
            {
                throw new IllegalArgumentException(name+" not a property");
            }
            Observer observer = Observer.getInstance(event, p, null, null, sseReference, locale);
            p.attach(observer);
        }
    }
    @Override
    public <T> void set(String property, T arg)
    {
        Set<Property> p = getProperty(property, arg.getClass());
        long millis = clock.millis();
        p.forEach((pr)->pr.set(property, millis, arg));
    }

    @Override
    public void set(String property, double arg)
    {
        Set<Property> p = getProperty(property, double.class);
        long millis = clock.millis();
        p.forEach((pr)->pr.set(property, millis, arg));
    }

    @Override
    public void set(String property, float arg)
    {
        Set<Property> p = getProperty(property, float.class);
        long millis = clock.millis();
        p.forEach((pr)->pr.set(property, millis, arg));
    }

    @Override
    public void set(String property, int arg)
    {
        Set<Property> p = getProperty(property, int.class);
        long millis = clock.millis();
        p.forEach((pr)->pr.set(property, millis, arg));
    }
    
    @Override
    public void set(String property, long arg)
    {
        Set<Property> p = getProperty(property, long.class);
        long millis = clock.millis();
        p.forEach((pr)->pr.set(property, millis, arg));
    }
    
    @Override
    public void set(String property, char arg)
    {
        Set<Property> p = getProperty(property, char.class);
        long millis = clock.millis();
        p.forEach((pr)->pr.set(property, millis, arg+""));
    }
    
    
    private static String[] allNMEAProperties()
    {
        NMEAProperties nmeaProperties = NMEAProperties.getInstance();
        Set<String> allProperties = nmeaProperties.getAllProperties();
        return allProperties.toArray(new String[allProperties.size()]);
    }
    
    private Set<Property> getProperty(String property, Class<?> type)
    {
        Set<Property> prop = dispatchMap.get(property);
        if (prop == null || prop.isEmpty())
        {
            PropertyType pt = config.getProperty(property);
            Property p = Property.getInstance(executor, pt, type);
            dispatchMap.add(property, p);
            propertyMap.put(property, p);
            return dispatchMap.get(property);
        }
        else
        {
            return prop;
        }
    }

}
