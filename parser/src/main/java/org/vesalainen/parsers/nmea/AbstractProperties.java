/*
 * Copyright (C) 2016 tkv
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
import java.util.stream.Stream;
import org.vesalainen.bean.BeanHelper;
import org.vesalainen.math.Unit;
import org.vesalainen.math.UnitType;

/**
 *
 * @author tkv
 */
public abstract class AbstractProperties
{
    protected Map<String,Prop> map;

    protected AbstractProperties(Map<String, Prop> map)
    {
        this.map = map;
    }
    
    protected static Map<String,Prop> create(Class<?> cls)
    {
        Map<String,Prop> map = new HashMap<>();
        for (Method method : cls.getMethods())
        {
            String property = BeanHelper.getProperty(method);
            Unit unit = method.getAnnotation(Unit.class);
            UnitType unitType = null;
            NMEACat nmeaCat = method.getAnnotation(NMEACat.class);
            if (unit != null)
            {
                unitType = unit.value();
            }
            NMEACategory nmeaCategory = null;
            if (nmeaCat != null)
            {
                nmeaCategory = nmeaCat.value();
            }
            map.put(property, new Prop(property, unitType, nmeaCategory));
        }
        return map;
    }
    public Stream<String> stream()
    {
        return map.keySet().stream();
    }
    public boolean isProperty(String property)
    {
        return map.containsKey(property);
    }
    
    public UnitType getType(String property)
    {
        Prop prop = map.get(property);
        if (prop != null)
        {
            return prop.unit;
        }
        return null;
    }
    public NMEACategory getCategory(String property)
    {
        Prop prop = map.get(property);
        if (prop != null)
        {
            return prop.nmeaCategory;
        }
        return NMEACategory.Miscelleneous;
    }
    protected static class Prop
    {
        private final String property;
        private final UnitType unit;
        private final NMEACategory nmeaCategory;

        public Prop(String property)
        {
            this(property, null, null);
        }

        public Prop(String property, UnitType unit)
        {
            this(property, unit, null);
        }

        public Prop(String property, UnitType unit, NMEACategory nmeaCategory)
        {
            this.property = property;
            this.unit = unit;
            this.nmeaCategory = nmeaCategory;
        }
        
    }
}
