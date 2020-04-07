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
package org.vesalainen.nmea.viewer;

import static java.util.Locale.US;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import org.vesalainen.math.UnitType;
import static org.vesalainen.math.UnitType.*;
import org.vesalainen.text.CamelCase;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class GaugeCanvas extends ResizableCanvas implements PropertyBindable
{
    private UnitType origUnit = UNITLESS;
    private final StringProperty title = new SimpleStringProperty();

    private String getTitle()
    {
        return title.get();
    }

    private void setTitle(String value)
    {
        title.set(value);
    }

    private StringProperty titleProperty()
    {
        return title;
    }

    private final StringProperty property = new SimpleStringProperty();

    public String getProperty()
    {
        return property.get();
    }

    public void setProperty(String value)
    {
        property.set(value);
    }

    public StringProperty propertyProperty()
    {
        return property;
    }
    private final DoubleProperty value = new SimpleDoubleProperty();

    public double getValue()
    {
        return value.get();
    }

    public void setValue(double v)
    {
        value.set(v);
    }

    public DoubleProperty valueProperty()
    {
        return value;
    }
    private final ObjectProperty<UnitType> unit = new SimpleObjectProperty<>(UNITLESS);

    UnitType getUnit()
    {
        return unit.get();
    }

    void setUnit(UnitType value)
    {
        unit.set(value);
    }

    ObjectProperty unitProperty()
    {
        return unit;
    }
    private final StringProperty unitTitle = new SimpleStringProperty();

    String getUnitTitle()
    {
        return unitTitle.get();
    }

    void setUnitTitle(String value)
    {
        unitTitle.set(value);
    }

    StringProperty unitTitleProperty()
    {
        return unitTitle;
    }
    
    private final StringProperty format = new SimpleStringProperty("% 5.1f");

    public String getFormat()
    {
        return format.get();
    }

    public void setFormat(String value)
    {
        format.set(value);
    }

    public StringProperty formatProperty()
    {
        return format;
    }
    public GaugeCanvas()
    {
        super(false);
        getStyleClass().add("gauge-canvas");
    }

    @Override
    public String[] bind(ViewerPreferences preferences, PropertyStore propertyStore)
    {
        getStyleClass().add(CamelCase.delimitedLower(property.getValue(), "-"));
        I18n.bind(titleProperty(), resources, property);
        I18n.bind(unitTitleProperty(), resources, Bindings.createStringBinding(()->CamelCase.property(unit.getValue().name())+"Unit", unit));
        
        String prop = property.getValue();
        origUnit = propertyStore.getOriginalUnit(prop);
        unitProperty().bind(preferences.getCategoryBinding(prop));
        valueProperty().bind(propertyStore.getBinding(prop));
        
        valueProperty().addListener(evt->reDraw());
        unitProperty().addListener(evt->reDraw());
        disabledProperty().addListener(evt->reDraw());
        
        return new String[]{property.getValue()};
    }
    @Override
    protected void onDraw()
    {
        double width = getWidth();
        double height = getHeight();
        if (width > 0 && height > 0)
        {
            GraphicsContext gc = getGraphicsContext2D();
            gc.clearRect(0, 0, width, height);
            gc.setFill(adjustColor(getTextFill()));
            String fontFamily = getFont().getFamily();
            // value
            gc.setFont(Font.font(fontFamily, height));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.fillText(String.format(US, getFormat(), origUnit.convertTo(getValue(), unit.getValue()), getValue()), width/2, height/2, width);
            // title
            gc.setFont(Font.font(fontFamily, height/10));
            gc.setTextAlign(TextAlignment.LEFT);
            gc.setTextBaseline(VPos.TOP);
            gc.fillText(title.getValue(), 0, 0, 0.8*width);
            // unit
            gc.setFont(Font.font(fontFamily, height/10));
            gc.setTextAlign(TextAlignment.RIGHT);
            gc.setTextBaseline(VPos.TOP);
            gc.fillText(unitTitle.getValue(), width, 0, 0.2*width);
        }
    }
    
}
