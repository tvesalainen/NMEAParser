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

import java.util.Locale;
import java.util.function.DoubleFunction;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.vesalainen.code.AbstractPropertySetter;
import org.vesalainen.code.PropertySetter;
import org.vesalainen.management.AbstractDynamicMBean;
import org.vesalainen.nmea.server.jaxb.PropertyType;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Property extends AbstractDynamicMBean implements PropertySetter
{
    protected final PropertyType property;
    protected final DoubleFunction<String> format;

    private Property(PropertyType property)
    {
        super(property.getDescription(), property);
        this.property = property;
        this.format = createFormat();
        register();
    }

    public static Property getInstance(PropertyType property)
    {
        switch (property.getType())
        {
            case "float32":
                return new FloatProperty(property);
            case "float64":
                return new DoubleProperty(property);
            case "int32":
                return new IntProperty(property);
            case "string":
                return new StringProperty(property);
            default:
                throw new UnsupportedOperationException(property.getType()+" not supported");
        }
    }

    @Override
    public void set(String property, double arg)
    {
        PropertySetter.super.set(property, arg); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> void set(String property, T arg)
    {
        PropertySetter.super.set(property, arg); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    protected ObjectName createObjectName() throws MalformedObjectNameException
    {
        return ObjectName.getInstance(Config.class.getName(), "Property", property.getName());
    }

    protected DoubleFunction<String> createFormat()
    {
        String f = property.getFormat();
        if (f != null)
        {
            return (x)->String.format(Locale.US, f, x);
        }
        else
        {
            Integer decimals = property.getDecimals();
            if (decimals != null)
            {
                String fmt = String.format("%%.%df", decimals);
                return (x)->String.format(Locale.US, fmt, x);
            }
        }
    }
    private static class FloatProperty extends DoubleProperty
    {
        public FloatProperty(PropertyType property)
        {
            super(property);
        }
        @Override
        public void set(String property, float arg)
        {
            super.set(property, (double)arg);
        }

    }
    private static class DoubleProperty extends Property
    {
        public DoubleProperty(PropertyType property)
        {
            super(property);
        }
        @Override
        public void set(String property, double arg)
        {
            super.set(property, arg);
        }
        @Override
        protected DoubleFunction<String> createFormat()
        {
            String f = property.getFormat();
            if (f != null)
            {
                return (x)->String.format(Locale.US, f, x);
            }
            else
            {
                Integer decimals = property.getDecimals();
                if (decimals != null)
                {
                    String fmt = String.format("%%.%df", decimals);
                    return (x)->String.format(Locale.US, fmt, x);
                }
                else
                {
                    return (x)->String.format(Locale.US, "%.1f", x);
                }
            }
        }
        
    }
    private static class IntProperty extends DoubleProperty
    {
        public IntProperty(PropertyType property)
        {
            super(property);
        }
        @Override
        public void set(String property, int arg)
        {
            super.set(property, (double)arg);
        }
    }
    private static class StringProperty extends Property
    {
        public StringProperty(PropertyType property)
        {
            super(property);
        }
        @Override
        public <T> void set(String property, T arg)
        {
            super.set(property, arg);
        }
    }
}
