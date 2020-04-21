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

import java.util.Locale;
import static java.util.Locale.US;
import java.util.function.DoubleFunction;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
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
import org.vesalainen.navi.CoordinateFormat;
import org.vesalainen.navi.Navis;
import org.vesalainen.text.CamelCase;
import org.vesalainen.time.YearHelp;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class GaugeCanvas extends ResizableCanvas implements PropertyBindable
{
    private final StringProperty title = new SimpleStringProperty(this, "title");
    private DoubleFunction<String> formatter;

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

    private final StringProperty property = new SimpleStringProperty(this, "property");

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
    private final DoubleProperty value = new SimpleDoubleProperty(this, "value");

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
    private final ObjectProperty<UnitType> unit = new SimpleObjectProperty<>(this, "unit", UNITLESS);

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
    private final StringProperty unitTitle = new SimpleStringProperty(this, "unitTitle");

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
    
    public GaugeCanvas()
    {
        super(false);
        getStyleClass().add("gauge-canvas");
    }

    @Override
    public void bind(ViewerPreferences preferences, PropertyStore propertyStore, BooleanProperty active)
    {
        super.bind(preferences, propertyStore, active);
        String prop = getProperty();
        getStyleClass().add(CamelCase.delimitedLower(prop, "-"));
        I18n.bind(titleProperty(), propertyProperty());
        I18n.bind(unitTitleProperty(), Bindings.createStringBinding(()->CamelCase.property(getUnit().name())+"Unit", unitProperty()));
        
        formatter = createFormatter(propertyStore, prop);
        unitProperty().bind(propertyStore.getCategoryBinding(prop));
        valueProperty().bind(propertyStore.getBinding(prop));

        onReDrawListener.bind(valueProperty(), unitProperty(), disabledProperty());
        
        disableProperty().bind(propertyStore.getDisableBind(prop));
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
            
            double val = getValue();
            // value
            gc.setFont(Font.font(fontFamily, height));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.fillText(formatter.apply(val), width/2, height/2, width);
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

    private DoubleFunction<String> createFormatter(PropertyStore propertyStore, String prop)
    {
        switch (prop)
        {
            case "latitude":
                    return (val)->CoordinateFormat.formatLatitude(US, val, unit.getValue());
            case "longitude":
                    return (val)->CoordinateFormat.formatLongitude(US, val, unit.getValue());
            case "utcDate":
                    return (val)->String.format(US, "%04d.%02d.%02d", YearHelp.year4((int) (val/10000)), ((int)val%10000)/100, ((int)val%100));
            case "utcTime":
                    return (val)->String.format(US, "%02d:%02d:%02d", (int)(val/10000), ((int)val%10000)/100, ((int)val%100));
            default:
                UnitType origUnit = propertyStore.getOriginalUnit(prop);
                switch (origUnit.getCategory())
                {
                    case PLANE_ANGLE:
                        return (val)->
                        {
                            switch (unit.getValue())
                            {
                                case DEGREE:
                                    return String.format(US, "% 5.1f", Navis.normalizeAngle(val));
                                case DEGREE_NEG:
                                    return String.format(US, "% 5.1f", Navis.normalizeToHalfAngle(val));
                                default:
                                    throw new UnsupportedOperationException(unit.getValue()+" not supported");
                            }
                        };
                    default:
                        return (val)->String.format(US, "% 5.1f", origUnit.convertTo(val, unit.getValue()));
                }
        }
    }

}
