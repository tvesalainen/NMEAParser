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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.vesalainen.code.AbstractPropertySetter;
import org.vesalainen.nmea.server.SseServlet.SseHandler;
import org.vesalainen.nmea.server.jaxb.PropertyType;
import org.vesalainen.parsers.nmea.NMEAProperties;
import org.vesalainen.util.ConcurrentHashMapSet;
import org.vesalainen.util.MapSet;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PropertyServer extends AbstractPropertySetter
{
    private final Clock clock;
    private final MapSet<String,Property> dispatchMap = new ConcurrentHashMapSet<>();
    private final Map<String,Property> propertyMap = new ConcurrentHashMap<>();
    private final Config config;
    
    public PropertyServer(Clock clock, Config config)
    {
        super(allNMEAProperties());
        this.clock = clock;
        this.config = config;
        List<String> sources = new ArrayList<>();
        config.getProperties().forEach((p)->
        {
            Property property = Property.getInstance(p);
            String name = p.getName();
            propertyMap.put(name, property);
            String source = p.getSource();
            if (source != null)
            {
                dispatchMap.add(source, property);
                sources.add(source);
            }
            else
            {
                dispatchMap.add(name, property);
            }
        });
        sources.forEach((source)->
        {
            if (!propertyMap.containsKey(source))
            {
                Property sourceProperty = Property.getInstance(config.getProperty(source));
                dispatchMap.add(source, sourceProperty);
                propertyMap.put(source, sourceProperty);
            }
        });
    }

    public void addSse(Map<String,String[]> map, SseHandler sseHandler)
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
            Observer observer = Observer.getInstance(event, p, null, null, sseHandler);
            sseHandler.addReference(observer);
            p.attach(observer);
        }
    }
    @Override
    public <T> void set(String property, T arg)
    {
        Set<Property> p = getProperty(property);
        long millis = clock.millis();
        p.forEach((pr)->pr.set(millis, arg));
    }

    @Override
    public void set(String property, double arg)
    {
        Set<Property> p = getProperty(property);
        long millis = clock.millis();
        p.forEach((pr)->pr.set(millis, arg));
    }

    @Override
    public void set(String property, float arg)
    {
        Set<Property> p = getProperty(property);
        long millis = clock.millis();
        p.forEach((pr)->pr.set(millis, arg));
    }

    @Override
    public void set(String property, int arg)
    {
        Set<Property> p = getProperty(property);
        long millis = clock.millis();
        p.forEach((pr)->pr.set(millis, arg));
    }
    
    @Override
    public void set(String property, long arg)
    {
        Set<Property> p = getProperty(property);
        long millis = clock.millis();
        p.forEach((pr)->pr.set(millis, arg));
    }
    
    @Override
    public void set(String property, char arg)
    {
        Set<Property> p = getProperty(property);
        long millis = clock.millis();
        p.forEach((pr)->pr.set(millis, arg+""));
    }
    
    
    private static String[] allNMEAProperties()
    {
        NMEAProperties nmeaProperties = NMEAProperties.getInstance();
        Set<String> allProperties = nmeaProperties.getAllProperties();
        return allProperties.toArray(new String[allProperties.size()]);
    }
    
    private Set<Property> getProperty(String property)
    {
        Set<Property> prop = dispatchMap.get(property);
        if (prop == null || prop.isEmpty())
        {
            PropertyType pt = config.getProperty(property);
            Property p = Property.getInstance(pt);
            dispatchMap.add(property, p);
            propertyMap.put(property, p);
        }
        return dispatchMap.get(property);
    }

}
