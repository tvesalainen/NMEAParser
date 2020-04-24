/*
 * Copyright (C) 2020 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nmea.viewer.store;

import java.util.HashMap;
import java.util.Map;
import javafx.beans.value.ObservableNumberValue;
import org.vesalainen.code.PropertySetter;
import org.vesalainen.fx.FunctionalDoubleBinding;
import org.vesalainen.fx.FunctionalFloatBinding;
import org.vesalainen.fx.FunctionalIntegerBinding;
import org.vesalainen.fx.FunctionalLongBinding;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FXPropertySetter implements PropertySetter
{
    private Map<String,ObservableNumberValue> map = new HashMap<>();
    
    protected void add(String property, Class<?> type)
    {
        switch (type.getSimpleName())
        {
            case "int":
                map.put(property, new IntegerPropertyValue(this, property, 0));
            case "long":
                map.put(property, new LongPropertyValue(this, property, 0));
            case "float":
                map.put(property, new FloatPropertyValue(this, property, 0));
            case "double":
                map.put(property, new DoublePropertyValue(this, property, 0));
            default:
                throw new UnsupportedOperationException(type+" not supported");
        }
    }

    @Override
    public void set(String property, double arg)
    {
        getPropertyValue(property).setDouble(arg);
    }

    @Override
    public void set(String property, float arg)
    {
        getPropertyValue(property).setFloat(arg);
    }

    @Override
    public void set(String property, long arg)
    {
        getPropertyValue(property).setLong(arg);
    }

    @Override
    public void set(String property, int arg)
    {
        getPropertyValue(property).setInt(arg);
    }
    private PropertyValue getPropertyValue(String property)
    {
        ObservableNumberValue onv = map.get(property);
        if (onv == null)
        {
            throw new IllegalArgumentException(property+" not found");
        }
        if (onv instanceof PropertyValue)
        {
            return (PropertyValue) onv;
        }
        throw new IllegalArgumentException(property+" not property");
    }
}
