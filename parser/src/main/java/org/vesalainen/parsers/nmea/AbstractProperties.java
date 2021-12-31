/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.parsers.nmea;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.vesalainen.bean.BeanHelper;
import org.vesalainen.math.Unit;
import org.vesalainen.math.UnitType;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AbstractProperties
{
    protected Map<String,Prop> map;

    protected AbstractProperties(Map<String, Prop> map)
    {
        this.map = map;
    }
    
    protected static Map<String,Prop> createNMEACAt(Class<?>... interfaces)
    {
        Map<String,Prop> map = new HashMap<>();
        for (Class<?> cls : interfaces)
        {
            for (Method method : cls.getMethods())
            {
                if (BeanHelper.isSetter(method))
                {
                    NMEACat nmeaCat = method.getAnnotation(NMEACat.class);
                    if (nmeaCat == null)
                    {
                        continue;
                    }
                    NMEACategory nmeaCategory = nmeaCat.value();
                    String property = BeanHelper.getProperty(method);
                    Unit unit = method.getAnnotation(Unit.class);
                    Class<?> type = method.getParameterTypes()[0];
                    map.put(property, new Prop(property, unit, nmeaCategory, type));
                }
            }
        }
        return map;
    }
    protected static Map<String,Prop> createAll(Class<?> cls)
    {
        Map<String,Prop> map = new HashMap<>();
        for (Method method : cls.getMethods())
        {
            if (BeanHelper.isSetter(method))
            {
                NMEACat nmeaCat = method.getAnnotation(NMEACat.class);
                NMEACategory nmeaCategory = null;
                if (nmeaCat != null)
                {
                    nmeaCategory = nmeaCat.value();
                }
                String property = BeanHelper.getProperty(method);
                Unit unit = method.getAnnotation(Unit.class);
                Class<?> type = method.getParameterTypes()[0];
                map.put(property, new Prop(property, unit, nmeaCategory, type));
            }
        }
        return map;
    }
    /**
     * Stream of all properties
     * @return 
     */
    public Stream<String> stream()
    {
        return map.keySet().stream();
    }
    public Set<String> getAllProperties()
    {
        return map.keySet();
    }
    public Stream<String> stream(Class<?> type)
    {
        return map.values().stream().filter((p)->{return type == p.type;}).map((p)->{return p.property;});
    }
    /**
     * Returns true if given property is one of NMEA properties
     * @param property
     * @return 
     */
    public boolean isProperty(String property)
    {
        return map.containsKey(property);
    }
    /**
     * Returns NMEA property unit
     * @param property
     * @return 
     */
    public UnitType getUnit(String property)
    {
        Prop prop = map.get(property);
        if (prop != null)
        {
            return prop.unit;
        }
        return null;
    }
    /**
     * Returns NMEA property type
     * @param property
     * @return 
     */
    public Class<?> getType(String property)
    {
        Prop prop = map.get(property);
        if (prop != null)
        {
            return prop.type;
        }
        return null;
    }
    /**
     * Returns NMEA property minimum value or -Double.MAX_VALUE.
     * @param property
     * @return 
     */
    public double getMin(String property)
    {
        Prop prop = map.get(property);
        if (prop != null)
        {
            return prop.min;
        }
        return -Double.MAX_VALUE;
    }
    /**
     * Returns NMEA property maximum value or Double.MAX_VALUE.
     * @param property
     * @return 
     */
    public double getMax(String property)
    {
        Prop prop = map.get(property);
        if (prop != null)
        {
            return prop.max;
        }
        return Double.MAX_VALUE;
    }
    /**
     * Returns NMEA property category or MISCELLENEOUS if property not found.
     * @param property
     * @return 
     */
    public NMEACategory getCategory(String property)
    {
        Prop prop = map.get(property);
        if (prop != null)
        {
            return prop.nmeaCategory;
        }
        return NMEACategory.MISCELLENEOUS;
    }
    protected static class Prop
    {
        private final String property;
        private UnitType unit = UnitType.UNITLESS;
        private NMEACategory nmeaCategory = NMEACategory.MISCELLENEOUS;
        private Class<?> type;
        private double min = -Double.MAX_VALUE;
        private double max = Double.MAX_VALUE;

        public Prop(String property)
        {
            this(property, null, null, null);
        }

        public Prop(String property, Unit unit)
        {
            this(property, unit, null, null);
        }

        public Prop(String property, Unit unit, NMEACategory nmeaCategory, Class<?> type)
        {
            this.property = property;
            if (unit != null)
            {
                this.unit = unit.value();
                this.min = unit.min();
                this.max = unit.max();
            }
            this.nmeaCategory = nmeaCategory;
            this.type = type;
        }
        
    }
}
