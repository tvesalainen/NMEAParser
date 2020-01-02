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

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.vesalainen.fx.EnumStringConverter;
import org.vesalainen.fx.EnumTitleConverter;
import org.vesalainen.math.UnitType;
import static org.vesalainen.math.UnitType.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ViewerController implements Initializable
{
    @FXML TextField host;
    @FXML TextField port;
    @FXML ComboBox<UnitType> depthUnit;
    @FXML ComboBox<UnitType> speedUnit;
    @FXML ComboBox<UnitType> temperatureUnit;
    
    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        
    }
    public void bindPreferences(ViewerPreferences preferences)
    {
        preferences.bindString("host", "", host);
        preferences.bindInteger("port", 0, port);
        EnumTitleConverter converter = new EnumTitleConverter<>(UnitType.class);
        preferences.bindCombo("depthUnit", depthUnit, converter, METER, FOOT, FATHOM);
        preferences.bindCombo("speedUnit", speedUnit, converter, KNOT, KILO_METERS_PER_HOUR, MILES_PER_HOUR);
        preferences.bindCombo("temperatureUnit", temperatureUnit, converter, CELSIUS, FAHRENHEIT);
    }
}
