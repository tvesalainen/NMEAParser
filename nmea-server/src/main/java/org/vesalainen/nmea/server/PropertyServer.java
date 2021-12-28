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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.vesalainen.code.AbstractPropertySetter;
import org.vesalainen.nmea.server.jaxb.PropertyType;
import org.vesalainen.parsers.nmea.NMEAProperties;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PropertyServer extends AbstractPropertySetter
{
    private final Clock clock;
    private final Map<String,Property> map = new ConcurrentHashMap<>();
    private final Config config;
    
    public PropertyServer(Clock clock, Config config)
    {
        super(allNMEAProperties());
        this.clock = clock;
        this.config = config;
    }

    public void attach(Observer observer)
    {
        Property p = getProperty(observer.getName());
        p.attach(observer);
    }
    @Override
    public <T> void set(String property, T arg)
    {
        Property p = getProperty(property);
        p.set(clock.millis(), arg);
    }

    @Override
    public void set(String property, double arg)
    {
        Property p = getProperty(property);
        p.set(clock.millis(), arg);
    }

    @Override
    public void set(String property, float arg)
    {
        Property p = getProperty(property);
        p.set(clock.millis(), arg);
    }

    @Override
    public void set(String property, int arg)
    {
        Property p = getProperty(property);
        p.set(clock.millis(), arg);
    }
    
    @Override
    public void set(String property, long arg)
    {
        Property p = getProperty(property);
        p.set(clock.millis(), arg);
    }
    
    @Override
    public void set(String property, char arg)
    {
        Property p = getProperty(property);
        p.set(clock.millis(), arg+"");
    }
    
    
    private static String[] allNMEAProperties()
    {
        NMEAProperties nmeaProperties = NMEAProperties.getInstance();
        Set<String> allProperties = nmeaProperties.getAllProperties();
        return allProperties.toArray(new String[allProperties.size()]);
    }
    
    private Property getProperty(String property)
    {
        Property prop = map.get(property);
        if (prop == null)
        {
            PropertyType pt = config.getProperty(property);
            prop = Property.getInstance(pt);
            map.put(property, prop);
        }
        return prop;
    }
}
