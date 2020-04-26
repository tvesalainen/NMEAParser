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

import javafx.beans.value.WritableIntegerValue;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class IntegerPropertyValue extends PropertyValue implements WritableIntegerValue
{
    private int value;
    
    public IntegerPropertyValue(Object bean, String name, int initialValue)
    {
        super(bean, name);
        this.value = initialValue;
    }

    @Override
    public int get()
    {
        valid = true;
        return value;
    }

    @Override
    public void set(int value)
    {
        if (this.value != value)
        {
            this.value = value;
            invalidate();
        }
        disabled.set(false);
    }

    @Override
    public void setValue(Number value)
    {
        set(value.intValue());
    }

    @Override
    public Number getValue()
    {
        return get();
    }
    
    @Override
    public int intValue()
    {
        return get();
    }

    @Override
    public long longValue()
    {
        return get();
    }

    @Override
    public float floatValue()
    {
        return get();
    }

    @Override
    public double doubleValue()
    {
        return get();
    }

    @Override
    public void setInt(int value)
    {
        set(value);
    }

    @Override
    public void setLong(long value)
    {
        set((int) value);
    }

    @Override
    public void setFloat(float value)
    {
        set((int) value);
    }

    @Override
    public void setDouble(double value)
    {
        set((int) value);
    }
    
}
