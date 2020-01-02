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

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Gauge extends GridPane implements Initializable
{

    private final StringProperty propertyProperty = new SimpleStringProperty();

    public String getPropertyProperty()
    {
        return propertyProperty.get();
    }

    public void setPropertyProperty(String value)
    {
        propertyProperty.set(value);
    }

    public StringProperty propertyPropertyProperty()
    {
        return propertyProperty;
    }
    private StringProperty propertyUnit;

    public String getPropertyUnit()
    {
        return propertyUnit.get();
    }

    public void setPropertyUnit(String value)
    {
        propertyUnit.set(value);
    }

    public StringProperty propertyUnitProperty()
    {
        return propertyUnit;
    }
    private StringProperty propertyValue;

    public String getPropertyValue()
    {
        return propertyValue.get();
    }

    public void setPropertyValue(String value)
    {
        propertyValue.set(value);
    }

    public StringProperty propertyValueProperty()
    {
        return propertyValue;
    }
    
    @FXML private Label title;
    @FXML private Label unit;
    @FXML private Label value;

    public Gauge()
    {
        try
        {
            ResourceBundle bundle = ResourceBundle.getBundle(I18n.class.getName(), Locale.getDefault());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/gauge.fxml"), bundle);
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        I18n.bind(title.textProperty(), resources, propertyProperty);
        propertyUnit = unit.textProperty();
        propertyValue = value.textProperty();
    }

}
