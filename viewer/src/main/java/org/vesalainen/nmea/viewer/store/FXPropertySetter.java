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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableNumberValue;
import org.vesalainen.code.PropertyGetter;
import org.vesalainen.code.PropertySetter;
import org.vesalainen.code.getter.DoubleGetter;
import org.vesalainen.code.getter.FloatGetter;
import org.vesalainen.code.getter.IntGetter;
import org.vesalainen.code.getter.LongGetter;
import org.vesalainen.code.setter.DoubleSetter;
import org.vesalainen.code.setter.FloatSetter;
import org.vesalainen.code.setter.IntSetter;
import org.vesalainen.code.setter.LongSetter;
import org.vesalainen.code.setter.Setter;
import org.vesalainen.fx.FunctionalDoubleBinding;
import org.vesalainen.parsers.nmea.NMEAProperties;
import org.vesalainen.util.CollectionHelp;
import org.vesalainen.util.Transactional;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FXPropertySetter implements PropertySetter, PropertyGetter, Transactional
{
    private final Map<String,ObservableNumberValue> valueMap = new HashMap<>();
    private final Map<String,ObservableBooleanValue> disabledMap = new HashMap<>();
    private final Map<String,Setter> setterMap = new HashMap<>();
    private final List<String> inProperties = new ArrayList<>();
    private final List<String> outProperties = new ArrayList<>();

    protected FXPropertySetter()
    {
    }
    
    protected void addNmea(boolean in, boolean out, String... properties)
    {
        NMEAProperties nmea = NMEAProperties.getInstance();
        for (String property : properties)
        {
            add(in, out, property, nmea.getType(property));
        }
    }
    protected void add(boolean in, boolean out, String property, Class<?> type)
    {
        PropertyValue pv;
        switch (type.getSimpleName())
        {
            case "int":
                pv = new IntegerPropertyValue(this, property, 0);
                break;
            case "long":
                pv = new LongPropertyValue(this, property, 0);
                break;
            case "float":
                pv = new FloatPropertyValue(this, property, 0);
                break;
            case "double":
                pv = new DoublePropertyValue(this, property, 0);
                break;
            default:
                throw new UnsupportedOperationException(type+" not supported");
        }
        valueMap.put(property, pv);
        disabledMap.put(property, pv.getDisabled());
        addProperty(in, out, property);
    }
    protected void addExt(boolean out, String property, ObservableNumberValue onv)
    {
        valueMap.put(property, onv);
        disabledMap.put(property, new ReadOnlyBooleanWrapper(this, property, false));
        addProperty(false, out, property);
    }
    protected void bind(boolean out, String property, F1 f, String f1)
    {
        ObservableNumberValue o1 = valueMap.get(f1);
        FunctionalDoubleBinding fdb = new FunctionalDoubleBinding(property, ()->f.f(o1.doubleValue()), o1);
        valueMap.put(property, fdb);
        disabledMap.put(property, getDisableBind(f1));
        addProperty(false, out, property);
    }
    protected void bind(boolean out, String property, F2 f, String f1, String f2)
    {
        ObservableNumberValue o1 = valueMap.get(f1);
        ObservableNumberValue o2 = valueMap.get(f2);
        FunctionalDoubleBinding fdb = new FunctionalDoubleBinding(property, ()->f.f(o1.doubleValue(), o2.doubleValue()), o1, o2);
        valueMap.put(property, fdb);
        disabledMap.put(property, getDisableBind(f1, f2));
        addProperty(false, out, property);
    }
    protected void bind(boolean out, String property, F3 f, String f1, String f2, String f3)
    {
        ObservableNumberValue o1 = valueMap.get(f1);
        ObservableNumberValue o2 = valueMap.get(f2);
        ObservableNumberValue o3 = valueMap.get(f3);
        FunctionalDoubleBinding fdb = new FunctionalDoubleBinding(property, ()->f.f(o1.doubleValue(), o2.doubleValue(), o3.doubleValue()), o1, o2, o3);
        valueMap.put(property, fdb);
        disabledMap.put(property, getDisableBind(f1, f2, f3));
        addProperty(false, out, property);
    }
    protected void bind(boolean out, String property, F4 f, String f1, String f2, String f3, String f4)
    {
        ObservableNumberValue o1 = valueMap.get(f1);
        ObservableNumberValue o2 = valueMap.get(f2);
        ObservableNumberValue o3 = valueMap.get(f3);
        ObservableNumberValue o4 = valueMap.get(f4);
        FunctionalDoubleBinding fdb = new FunctionalDoubleBinding(property, ()->f.f(o1.doubleValue(), o2.doubleValue(), o3.doubleValue(), o4.doubleValue()), o1, o2, o3, o4);
        valueMap.put(property, fdb);
        disabledMap.put(property, getDisableBind(f1, f2, f3, f4));
        addProperty(false, out, property);
    }
    protected void addFloatSetter(String property, FloatSetter setter)
    {
        setterMap.put(property, setter);
        addProperty(true, false, property);
    }
    protected ObservableNumberValue getProperty(String property)
    {
        return valueMap.get(property);
    }
    public ObservableBooleanValue getDisableBind(String... properties)
    {
        ObservableBooleanValue sbp = disabledMap.get(properties[0]);
        for (int ii=1;ii<properties.length;ii++)
        {
            ObservableBooleanValue  sbpx = disabledMap.get(properties[ii]);
            sbp = Bindings.or(sbp, sbpx);
        }
        return sbp;
    }
    public boolean hasProperty(String prop)
    {
        return valueMap.containsKey(prop);
    }
    private void addProperty(boolean in, boolean out, String property)
    {
        if (in)
        {
            inProperties.add(property);
        }
        if (out)
        {
            outProperties.add(property);
        }
    }
    public ObservableNumberValue getBinding(String property)
    {
        return valueMap.get(property);
    }
    /**
     * this is run in platform thread
     * @param property 
     */
    protected void setDisable(String property, boolean disabled)
    {
        SimpleBooleanProperty sbp = (SimpleBooleanProperty) disabledMap.get(property);
        sbp.set(disabled);
    }

    @Override
    public String[] getProperties()
    {
        return CollectionHelp.toArray(inProperties, String.class);
    }

    @Override
    public DoubleSetter getDoubleSetter(String property)
    {
        DoublePropertyValue pv = (DoublePropertyValue) getPropertyValue(property);
        return (v)->pv.set(v);
    }

    @Override
    public FloatSetter getFloatSetter(String property)
    {
        FloatPropertyValue pv = (FloatPropertyValue) getPropertyValue(property);
        return (v)->pv.set(v);
    }

    @Override
    public LongSetter getLongSetter(String property)
    {
        LongPropertyValue pv = (LongPropertyValue) getPropertyValue(property);
        return (v)->pv.set(v);
    }

    @Override
    public IntSetter getIntSetter(String property)
    {
        IntegerPropertyValue pv = (IntegerPropertyValue) getPropertyValue(property);
        return (v)->pv.set(v);
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
        ObservableNumberValue onv = valueMap.get(property);
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

    @Override
    public Setter getSetter(String property, Class<?> type)
    {
        Setter setter = setterMap.get(property);
        if (setter != null)
        {
            return setter;
        }
        else
        {
            return PropertySetter.super.getSetter(property, type);
        }
    }

    @Override
    public IntGetter getIntGetter(String property)
    {
        IntegerPropertyValue pv = (IntegerPropertyValue) getPropertyValue(property);
        return ()->pv.get();
    }

    @Override
    public LongGetter getLongGetter(String property)
    {
        LongPropertyValue pv = (LongPropertyValue) getPropertyValue(property);
        return ()->pv.get();
    }

    @Override
    public FloatGetter getFloatGetter(String property)
    {
        FloatPropertyValue pv = (FloatPropertyValue) getPropertyValue(property);
        return ()->pv.get();
    }

    @Override
    public DoubleGetter getDoubleGetter(String property)
    {
        DoublePropertyValue pv = (DoublePropertyValue) getPropertyValue(property);
        return ()->pv.get();
    }

    
    @Override
    public boolean getBoolean(String property)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte getByte(String property)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public char getChar(String property)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public short getShort(String property)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getInt(String property)
    {
        return getPropertyValue(property).intValue();
    }

    @Override
    public long getLong(String property)
    {
        return getPropertyValue(property).longValue();
    }

    @Override
    public float getFloat(String property)
    {
        return getPropertyValue(property).floatValue();
    }

    @Override
    public double getDouble(String property)
    {
        return getPropertyValue(property).doubleValue();
    }

    @Override
    public <T> T getObject(String property)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
