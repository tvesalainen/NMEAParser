/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nmea.viewer;

import java.util.HashMap;
import java.util.Map;
import javafx.beans.binding.Binding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;
import javafx.util.converter.BooleanStringConverter;
import javafx.util.converter.DefaultStringConverter;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.FloatStringConverter;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.LongStringConverter;
import org.vesalainen.fx.EnumStringConverter;
import org.vesalainen.fx.PreferencesBindings;
import org.vesalainen.math.UnitType;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ViewerPreferences
{
    public static final DefaultStringConverter DEFAULT_STRING_CONVERTER = new DefaultStringConverter();
    public static final DoubleStringConverter DOUBLE_STRING_CONVERTER = new DoubleStringConverter();
    public static final BooleanStringConverter BOOLEAN_STRING_CONVERTER = new BooleanStringConverter();
    public static final FloatStringConverter FLOAT_STRING_CONVERTER = new FloatStringConverter();
    public static final IntegerStringConverter INTEGER_STRING_CONVERTER = new IntegerStringConverter();
    public static final LongStringConverter LONG_STRING_CONVERTER = new LongStringConverter();
    
    private PreferencesBindings bindingPreferences = PreferencesBindings.userNodeForPackage(ViewerPreferences.class);
    private Map<String,Binding<?>> bindings = new HashMap<>();
    
    public void bindString(String key, String def, TextField textField)
    {
        TextFormatter<String> formatter = setFormatter(textField, DEFAULT_STRING_CONVERTER);
        bindingPreferences.bindBiDirectional(key, def, formatter.valueProperty());
        bindings.put(key, bindingPreferences.createStringBinding(key, def));
    }
    public void bindBoolean(String key, boolean def, TextField textField)
    {
        TextFormatter<Boolean> formatter = setFormatter(textField, BOOLEAN_STRING_CONVERTER);
        bindingPreferences.bindBooleanBiDirectional(key, def, formatter.valueProperty());
        bindings.put(key, bindingPreferences.createBooleanBinding(key, def));
    }
    public void bindDouble(String key, double def, TextField textField)
    {
        TextFormatter<Double> formatter = setFormatter(textField, DOUBLE_STRING_CONVERTER);
        bindingPreferences.bindDoubleBiDirectional(key, def, formatter.valueProperty());
        bindings.put(key, bindingPreferences.createDoubleBinding(key, def));
    }
    public void bindFloat(String key, float def, TextField textField)
    {
        TextFormatter<Float> formatter = setFormatter(textField, FLOAT_STRING_CONVERTER);
        bindingPreferences.bindFloatBiDirectional(key, def, formatter.valueProperty());
        bindings.put(key, bindingPreferences.createFloatBinding(key, def));
    }
    public void bindInteger(String key, int def, TextField textField)
    {
        TextFormatter<Integer> formatter = setFormatter(textField, INTEGER_STRING_CONVERTER);
        bindingPreferences.bindIntegerBiDirectional(key, def, formatter.valueProperty());
        bindings.put(key, bindingPreferences.createIntegerBinding(key, def));
    }
    public void bindLong(String key, long def, TextField textField)
    {
        TextFormatter<Long> formatter = setFormatter(textField, LONG_STRING_CONVERTER);
        bindingPreferences.bindLongBiDirectional(key, def, formatter.valueProperty());
        bindings.put(key, bindingPreferences.createLongBinding(key, def));
    }
    public <E extends Enum<E>> void bindCombo(String key, ComboBox<E> combo, StringConverter<E> converter, E... values)
    {
        combo.setItems(FXCollections.observableArrayList(values));
        combo.setConverter(converter);
        bindingPreferences.bindEnumBiDirectional(key, values[0], combo.valueProperty());
        bindings.put(key, bindingPreferences.createEnumBinding(key, values[0]));
    }
    public String getString(String key)
    {
        return (String) get(key).getValue();
    }
    public boolean getBoolean(String key)
    {
        return (Boolean) get(key).getValue();
    }
    public double getDouble(String key)
    {
        return (Double) get(key).getValue();
    }
    public float getFloat(String key)
    {
        return (Float) get(key).getValue();
    }
    public int getInteger(String key)
    {
        return (Integer) get(key).getValue();
    }
    public long getLong(String key)
    {
        return (Long) get(key).getValue();
    }
    public <T> Binding<T> get(String key)
    {
        Binding<T> binding = (Binding<T>) bindings.get(key);
        if (binding != null)
        {
            return binding;
        }
        else
        {
            throw new UnsupportedOperationException(key+" not supported");
        }
    }
    private <T> TextFormatter<T> setFormatter(TextField textField, StringConverter<T> converter)
    {
        textField.textFormatterProperty().set(new TextFormatter<>(converter));
        return (TextFormatter<T>) textField.textFormatterProperty().get();
    }

}
