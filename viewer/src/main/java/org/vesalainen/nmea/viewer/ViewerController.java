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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.vesalainen.fx.EnumTitleConverter;
import org.vesalainen.math.UnitType;
import static org.vesalainen.math.UnitType.*;
import org.vesalainen.navi.SolarWatch.DayPhase;
import static org.vesalainen.navi.SolarWatch.DayPhase.*;
import static org.vesalainen.nmea.viewer.ViewerPreferences.DEFAULT_STRING_CONVERTER;
import org.vesalainen.util.CollectionHelp;

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
    @FXML TextField transducerOffset;
    @FXML TextField keelOffset;
    @FXML TextField waterLineLength;
    @FXML TextField timeToLive;
    @FXML TextField trendTimeout;
    @FXML TextField solarDepressionAngle;
    @FXML TextField solarUpdateSeconds;
    @FXML ColorPicker dayBackgroundColor;
    @FXML ColorPicker nightBackgroundColor;
    @FXML ColorPicker twilightBackgroundColor;
    @FXML ComboBox<String> fontFamily;
    @FXML ComboBox<DayPhase> dayPhase;
    @FXML CheckBox solarAutomation;
    @FXML CheckBox simulate;
    @FXML TextField simBoatSpeed;
    @FXML TextField simBoatDirection;
    @FXML TextField simWindSpeed;
    @FXML TextField simWindDirection;
    @FXML TextField simCurrentSpeed;
    @FXML TextField simCurrentDirection;
    
    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        
    }
    public void bindPreferences(ViewerPreferences preferences)
    {
        preferences.bindString("host", "", host);
        preferences.bindInteger("port", 0, 0, 65535, port);
        EnumTitleConverter converter = new EnumTitleConverter<>(UnitType.class);
        preferences.bindCombo("depthUnit", METER, depthUnit, converter, METER, FOOT, FATHOM);
        preferences.bindCombo("speedUnit", KNOT, speedUnit, converter, KNOT, KILO_METERS_PER_HOUR, MILES_PER_HOUR);
        preferences.bindCombo("temperatureUnit", CELSIUS, temperatureUnit, converter, CELSIUS, FAHRENHEIT);
        preferences.bindFloat("transducerOffset", 0, 0, 5, transducerOffset);
        preferences.bindFloat("keelOffset", 0, 0, 20, keelOffset);
        preferences.bindFloat("waterLineLength", 10, 0, 1000, waterLineLength);
        preferences.bindLong("timeToLive", 5, 1, 60, timeToLive);
        preferences.bindLong("trendTimeout", 30, 1, 60, trendTimeout);
        preferences.bindDouble("solarDepressionAngle", 6, 0, 90, solarDepressionAngle);
        preferences.bindLong("solarUpdateSeconds", 60, 1, Long.MAX_VALUE, solarUpdateSeconds);
        preferences.bindColor("dayBackgroundColor", Color.web("#ececec"), dayBackgroundColor);
        preferences.bindColor("nightBackgroundColor", Color.BLACK, nightBackgroundColor);
        preferences.bindColor("twilightBackgroundColor", Color.LIGHTGRAY, twilightBackgroundColor);
        preferences.bindCombo("fontFamily", "Calibri", fontFamily, DEFAULT_STRING_CONVERTER, CollectionHelp.toArray(Font.getFamilies(), String.class));
        preferences.bindBoolean("solarAutomation", true, solarAutomation.selectedProperty());
        EnumTitleConverter dayPhaseConverter = new EnumTitleConverter<>(DayPhase.class);
        preferences.bindCombo("dayPhase", DAY, dayPhase, dayPhaseConverter, DAY, TWILIGHT, NIGHT);
        dayPhase.disableProperty().bind(solarAutomation.selectedProperty());
        // simulation
        preferences.bindBoolean("simulate", false, simulate.selectedProperty());
        preferences.bindFloat("simBoatSpeed", 5, 0, 100, simBoatSpeed);
        preferences.bindFloat("simBoatDirection", 0, 0, 359, simBoatDirection);
        preferences.bindFloat("simWindSpeed", 10, 0, 300, simWindSpeed);
        preferences.bindFloat("simWindDirection", 90, 0, 359, simWindDirection);
        preferences.bindFloat("simCurrentSpeed", 1, 0, 10, simCurrentSpeed);
        preferences.bindFloat("simCurrentDirection", 270, 0, 359, simCurrentDirection);
    }
}
