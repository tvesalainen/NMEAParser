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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import org.vesalainen.nmea.viewer.I18n.I18nString;
import org.vesalainen.text.CamelCase;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class GaugePane extends StackPane implements PropertyBindable
{

    private final StringProperty property = new SimpleStringProperty();
    private ViewerPreferences preferences;
    private PropertyStore propertyStore;

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

    @Override
    public void bind(ViewerPreferences preferences, PropertyStore propertyStore)
    {
        this.preferences = preferences;
        this.propertyStore = propertyStore;
        String prop = getProperty();
        if (prop == null || prop.isEmpty())
        {
            Parent parent = getParent();
            if (parent instanceof GridPane)
            {
                GridPane gridPane = (GridPane) parent;
                String parentId = gridPane.getId();
                Integer columnIndex = GridPane.getColumnIndex(this);
                Integer rowIndex = GridPane.getRowIndex(this);
                int col = columnIndex != null ? columnIndex : 0;
                int row = rowIndex != null ? rowIndex : 0;
                String id = parentId+"-"+col+"-"+row;
                preferences.bindString(id, "", property);
            }
        }
        prop = getProperty();
        if (propertyStore.hasProperty(prop))
        {
            bind2(prop);
        }
        else
        {
            Label label = new Label(I18n.get().getString("noProperty"));
            getChildren().add(label);
        }
        onMousePressedProperty().setValue((e)->{onMousePressed(e);});
    }

    private void bind2(String prop)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void onMousePressed(MouseEvent e)
    {
        if (e.isSecondaryButtonDown())
        {
            List<I18nString> list = new ArrayList<>();
            for (String p : propertyStore.getProperties())
            {
                list.add(I18n.getI18nString(p));
            }
            list.sort(null);
            ChoiceDialog<I18nString> dia = new ChoiceDialog<>(list.get(0), list);
            ResourceBundle rb = I18n.get();
            dia.setTitle(rb.getString("addPropertyTitle"));
            dia.setContentText(rb.getString("addPropertyContent"));
            dia.setHeaderText(rb.getString("addPropertyHeader"));
            dia.showAndWait().ifPresent(response -> bind2(response.getKey()));        
        }
    }

}
