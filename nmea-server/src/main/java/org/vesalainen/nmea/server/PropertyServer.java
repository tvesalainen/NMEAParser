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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.vesalainen.code.AbstractPropertySetter;
import org.vesalainen.math.UnitType;
import org.vesalainen.nmea.server.SseServlet.SseHandler;
import org.vesalainen.nmea.server.jaxb.PropertyType;
import org.vesalainen.parsers.nmea.NMEAProperties;
import org.vesalainen.util.ConcurrentHashMapList;
import org.vesalainen.util.MapList;
import web.I18n;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PropertyServer extends AbstractPropertySetter
{
    private final Clock clock;
    private final MapList<String,Property> mapList = new ConcurrentHashMapList<>();
    private final Map<String,Property> map = new ConcurrentHashMap<>();
    private final Config config;
    
    public PropertyServer(Clock clock, Config config)
    {
        super(allNMEAProperties());
        this.clock = clock;
        this.config = config;
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
            Property p = getProperty(name);
            Observer observer = Observer.getInstance(event, p, null, null, sseHandler);
            sseHandler.addReference(observer);
            populate(event, sseHandler, p);
            p.attach(observer);
        }
    }
    private void populate(String event, SseHandler sseHandler, Property p)
    {
        String name = p.getName();
        String description = I18n.get().getString(name);
        UnitType unit = p.getUnit();
        long history = p.getHistoryMillis();
        double min = p.getMin();
        double max = p.getMax();
        sseHandler.fireEvent(event, "{"
                + "\"name\": \""+name+"\", "
                + "\"title\": \""+description+"\", "
                + "\"unit\": \""+unit.getUnit()+ "\", "
                + "\"history\": \""+history+ "\", "
                + "\"min\": \""+min+ "\", "
                + "\"max\": \""+max+ "\" "
                + "}");
    }
    @Override
    public <T> void set(String property, T arg)
    {
        List<Property> p = getProperty(property);
        long millis = clock.millis();
        p.forEach((pr)->pr.set(millis, arg));
    }

    @Override
    public void set(String property, double arg)
    {
        List<Property> p = getProperty(property);
        long millis = clock.millis();
        p.forEach((pr)->pr.set(millis, arg));
    }

    @Override
    public void set(String property, float arg)
    {
        List<Property> p = getProperty(property);
        long millis = clock.millis();
        p.forEach((pr)->pr.set(millis, arg));
    }

    @Override
    public void set(String property, int arg)
    {
        List<Property> p = getProperty(property);
        long millis = clock.millis();
        p.forEach((pr)->pr.set(millis, arg));
    }
    
    @Override
    public void set(String property, long arg)
    {
        List<Property> p = getProperty(property);
        long millis = clock.millis();
        p.forEach((pr)->pr.set(millis, arg));
    }
    
    @Override
    public void set(String property, char arg)
    {
        List<Property> p = getProperty(property);
        long millis = clock.millis();
        p.forEach((pr)->pr.set(millis, arg+""));
    }
    
    
    private static String[] allNMEAProperties()
    {
        NMEAProperties nmeaProperties = NMEAProperties.getInstance();
        Set<String> allProperties = nmeaProperties.getAllProperties();
        return allProperties.toArray(new String[allProperties.size()]);
    }
    
    private List<Property> getProperty(String property)
    {
        List<Property> prop = mapList.get(property);
        if (prop == null)
        {
            PropertyType pt = config.getProperty(property);
            Property p = Property.getInstance(pt);
            mapList.add(property, p);
            map.put(property, p);
        }
        return mapList.get(property);
    }

}
