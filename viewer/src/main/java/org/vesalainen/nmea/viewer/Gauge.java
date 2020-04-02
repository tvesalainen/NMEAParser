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

    private final StringProperty name = new SimpleStringProperty();

    public String getName()
    {
        return name.get();
    }

    public void setName(String value)
    {
        name.set(value);
    }

    public StringProperty nameProperty()
    {
        return name;
    }
    private StringProperty value;

    public String getValue()
    {
        return value.get();
    }

    public void setValue(String v)
    {
        value.set(v);
    }

    public StringProperty valueProperty()
    {
        return value;
    }
    private StringProperty unit;

    public String getUnit()
    {
        return unit.get();
    }

    public void setUnit(String value)
    {
        unit.set(value);
    }

    public StringProperty unitProperty()
    {
        return unit;
    }

    
    @FXML private Label titleLabel;
    @FXML private Label unitLabel;
    @FXML private Label valueLabel;

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
        I18n.bind(titleLabel.textProperty(), resources, name);
        unit = unitLabel.textProperty();
        value = valueLabel.textProperty();
    }

}
