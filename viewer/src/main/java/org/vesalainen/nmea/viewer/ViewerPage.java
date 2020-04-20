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

import java.io.IOException;
import java.util.ResourceBundle;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ViewerPage
{

    private final BooleanProperty active;

    public boolean isActive()
    {
        return active.get();
    }

    public void setActive(boolean value)
    {
        active.set(value);
    }

    public BooleanProperty activeProperty()
    {
        return active;
    }
    
    private Parent parent;

    public ViewerPage(BooleanProperty active, Parent parent)
    {
        this.active = active;
        this.parent = parent;
    }

    public static ViewerPage loadPreferencePage(ViewerPreferences preferences, String fxml, ResourceBundle bundle) throws IOException
    {
        FXMLLoader preferencesLoader = new FXMLLoader(ViewerPage.class.getResource("/fxml/preferences.fxml"), bundle);
        Parent preferencesParent = preferencesLoader.load();
        PreferenceController preferencesController = preferencesLoader.getController();
        preferencesController.bindPreferences(preferences);
        return new ViewerPage(new SimpleBooleanProperty(), preferencesParent);
    }

    public static ViewerPage loadPage(ViewerService service, String fxml, ResourceBundle bundle, StringExpression styleExpression) throws IOException
    {
        BooleanProperty active = new SimpleBooleanProperty();
        FXMLLoader load = new FXMLLoader(ViewerPage.class.getResource(fxml), bundle);
        Parent parent = load.load();
        parent.styleProperty().bind(styleExpression);
        service.register(active, parent.lookupAll("*"));
        return new ViewerPage(active, parent);
    }

    public Parent getParent()
    {
        return parent;
    }
    
}
