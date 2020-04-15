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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import javafx.util.converter.BooleanStringConverter;
import javafx.util.converter.DefaultStringConverter;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.FloatStringConverter;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.LongStringConverter;
import org.vesalainen.fx.ColorStringConverter;
import org.vesalainen.fx.PreferencesBindings;
import org.vesalainen.fx.ValidatingDoubleStringConverter;
import org.vesalainen.fx.ValidatingFloatStringConverter;
import org.vesalainen.fx.ValidatingIntegerStringConverter;
import org.vesalainen.fx.ValidatingLongStringConverter;
import org.vesalainen.math.UnitType;
import org.vesalainen.parsers.nmea.NMEACategory;
import org.vesalainen.parsers.nmea.NMEAProperties;

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
    public static final ColorStringConverter COLOR_STRING_CONVERTER = new ColorStringConverter();
    
    private PreferencesBindings bindingPreferences = PreferencesBindings.userNodeForPackage(ViewerPreferences.class);
    private Map<String,Binding<?>> bindings = new HashMap<>();
    
    public void bindString(String key, String def, TextField textField)
    {
        bindString(key, def, textField, DEFAULT_STRING_CONVERTER);
    }
    public void bindString(String key, String def, TextField textField, StringConverter<String> converter)
    {
        TextFormatter<String> formatter = setFormatter(textField, converter);
        bindingPreferences.bindStringBiDirectional(key, def, formatter.valueProperty());
        bindings.put(key, bindingPreferences.createStringBinding(key, def));
    }
    public void bindBoolean(String key, boolean def, TextField textField)
    {
        bindBoolean(key, def, textField, BOOLEAN_STRING_CONVERTER);
    }
    public void bindBoolean(String key, boolean def, TextField textField, StringConverter<Boolean> converter)
    {
        TextFormatter<Boolean> formatter = setFormatter(textField, converter);
        bindingPreferences.bindBooleanBiDirectional(key, def, formatter.valueProperty());
        bindings.put(key, bindingPreferences.createBooleanBinding(key, def));
    }
    public void bindDouble(String key, double def, TextField textField)
    {
        bindDouble(key, def, textField, DOUBLE_STRING_CONVERTER);
    }
    public void bindDouble(String key, double def, double min, double max, TextField textField)
    {
        bindDouble(key, def, textField, new ValidatingDoubleStringConverter(min, max));
    }
    public void bindDouble(String key, double def, TextField textField, StringConverter<Double> converter)
    {
        TextFormatter<Double> formatter = setFormatter(textField, converter);
        bindingPreferences.bindDoubleBiDirectional(key, def, formatter.valueProperty());
        bindings.put(key, bindingPreferences.createDoubleBinding(key, def));
    }
    public void bindFloat(String key, float def, TextField textField)
    {
        bindFloat(key, def, textField, FLOAT_STRING_CONVERTER);
    }
    public void bindFloat(String key, float def, float min, float max, TextField textField)
    {
        bindFloat(key, def, textField, new ValidatingFloatStringConverter(min, max));
    }
    public void bindFloat(String key, float def, TextField textField, StringConverter<Float> converter)
    {
        TextFormatter<Float> formatter = setFormatter(textField, converter);
        bindingPreferences.bindFloatBiDirectional(key, def, formatter.valueProperty());
        bindings.put(key, bindingPreferences.createFloatBinding(key, def));
    }
    public void bindInteger(String key, int def, TextField textField)
    {
        bindInteger(key, def, textField, INTEGER_STRING_CONVERTER);
    }
    public void bindInteger(String key, int def, int min, int max, TextField textField)
    {
        bindInteger(key, def, textField, new ValidatingIntegerStringConverter(min, max));
    }
    public void bindInteger(String key, int def, TextField textField, StringConverter<Integer> converter)
    {
        TextFormatter<Integer> formatter = setFormatter(textField, converter);
        bindingPreferences.bindIntegerBiDirectional(key, def, formatter.valueProperty());
        bindings.put(key, bindingPreferences.createIntegerBinding(key, def));
    }
    public void bindLong(String key, long def, TextField textField)
    {
        bindLong(key, def, textField, LONG_STRING_CONVERTER);
    }
    public void bindLong(String key, long def, long min, long max, TextField textField)
    {
        bindLong(key, def, textField, new ValidatingLongStringConverter(min, max));
    }
    public void bindLong(String key, long def, TextField textField, StringConverter<Long> converter)
    {
        TextFormatter<Long> formatter = setFormatter(textField, converter);
        bindingPreferences.bindLongBiDirectional(key, def, formatter.valueProperty());
        bindings.put(key, bindingPreferences.createLongBinding(key, def));
    }
    public <T> void bindCombo(String key, T def, ComboBox<T> combo, StringConverter<T> converter, T... values)
    {
        combo.setItems(FXCollections.observableArrayList(values));
        combo.setConverter(converter);
        bindingPreferences.bindBiDirectional(key, def, combo.valueProperty(), converter);
        bindings.put(key, bindingPreferences.createObjectBinding(key, def, converter));
    }
    public void bindColor(String key, Color def, ColorPicker colorPicker)
    {
        bindingPreferences.bindBiDirectional(key, def, colorPicker.valueProperty(), COLOR_STRING_CONVERTER);
        bindings.put(key, bindingPreferences.createObjectBinding(key, def, COLOR_STRING_CONVERTER));
    }
    public void bindBoolean(String key, boolean def, BooleanProperty b)
    {
        bindingPreferences.bindBooleanBiDirectional(key, def, b);
        bindings.put(key, bindingPreferences.createBooleanBinding(key, def));
    }
    public String getString(String key)
    {
        return (String) getBinding(key).getValue();
    }
    public boolean getBoolean(String key)
    {
        return (Boolean) getBinding(key).getValue();
    }
    public double getDouble(String key)
    {
        return (Double) getBinding(key).getValue();
    }
    public float getFloat(String key)
    {
        return (Float) getBinding(key).getValue();
    }
    public int getInteger(String key)
    {
        return (Integer) getBinding(key).getValue();
    }
    public long getLong(String key)
    {
        return (Long) getBinding(key).getValue();
    }

    public <T> Property<T> getProperty(String key)
    {
        return bindingPreferences.getProperty(key);
    }
    
    public Binding<Number> getNumberBinding(String key)
    {
        Binding<Number> binding = (Binding<Number>) bindings.get(key);
        if (binding != null)
        {
            return binding;
        }
        else
        {
            throw new UnsupportedOperationException(key+" not supported");
        }
    }
    public <T> Binding<T> getBinding(String key)
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

    public Binding<UnitType> getCategoryBinding(String property)
    {
        NMEACategory cat = NMEAProperties.getInstance().getCategory(property);
        if (cat != null)
        {
            switch (cat)
            {
                case DEPTH:
                    return getBinding("depthUnit");
                case SPEED:
                    return getBinding("speedUnit");
                case TEMPERATURE:
                    return getBinding("temperatureUnit");
                default:
                    throw new UnsupportedOperationException(cat+" not supported");
            }
        }
        else
        {
            switch (property)
            {
                default:
                    throw new UnsupportedOperationException(property+" has no category");
            }
        }
    }

}
