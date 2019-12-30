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
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import static org.vesalainen.nmea.viewer.PropertyTitleConverter.PROPERTY_TITLE_CONVERTER;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Gauge extends GridPane
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
    @FXML private Label title;
    @FXML private Label unit;
    @FXML private Label value;

    public Gauge()
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/gauge.fxml"));
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
            title.textProperty().bindBidirectional(propertyProperty, PROPERTY_TITLE_CONVERTER);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

}
