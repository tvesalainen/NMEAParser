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

import javafx.beans.value.WritableLongValue;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class LongPropertyValue extends PropertyValue implements WritableLongValue
{
    private long value;
    
    public LongPropertyValue(Object bean, String name, long initialValue)
    {
        super(bean, name);
        this.value = initialValue;
    }

    @Override
    public long get()
    {
        return value;
    }

    @Override
    public void set(long value)
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
        set(value.longValue());
    }

    @Override
    public Number getValue()
    {
        return get();
    }
    
    @Override
    public int intValue()
    {
        return (int) value;
    }

    @Override
    public long longValue()
    {
        return value;
    }

    @Override
    public float floatValue()
    {
        return value;
    }

    @Override
    public double doubleValue()
    {
        return value;
    }

    @Override
    public void setInt(int value)
    {
        set(value);
    }

    @Override
    public void setLong(long value)
    {
        set(value);
    }

    @Override
    public void setFloat(float value)
    {
        set((long) value);
    }

    @Override
    public void setDouble(double value)
    {
        set((long) value);
    }
    
}
