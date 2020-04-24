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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import org.vesalainen.fx.BasicObservableNumber;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class PropertyValue extends BasicObservableNumber
{
    protected BooleanProperty disabled = new SimpleBooleanProperty();
    
    public PropertyValue(Object bean, String name)
    {
        super(bean, name);
    }

    public ObservableBooleanValue getDisabled()
    {
        return disabled;
    }
    
    public void setDisable(boolean disable)
    {
        disabled.set(disable);
    }
    
    public abstract void setInt(int value);
    public abstract void setLong(long value);
    public abstract void setFloat(float value);
    public abstract void setDouble(double value);
}
