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
import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import org.vesalainen.nmea.viewer.I18n.I18nString;
import org.vesalainen.util.ArrayHelp;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class GaugePane extends StackPane implements PropertyBindable
{
    private final StringProperty property = new SimpleStringProperty(this, "property", "");
    private ViewerPreferences preferences;
    private PropertyStore propertyStore;
    private BooleanProperty active;

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

    public GaugePane()
    {
        setMinHeight(0);
        setMinWidth(0);
        setMaxHeight(Double.MAX_VALUE);
        setMaxWidth(Double.MAX_VALUE);
    }

    @Override
    public void bind(ViewerPreferences preferences, PropertyStore propertyStore, BooleanProperty active)
    {
        this.preferences = preferences;
        this.propertyStore = propertyStore;
        this.active = active;
        String prop = getProperty();
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
            preferences.bindString(id, prop, property);
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
        setProperty(prop);
        ObservableList<Node> children = getChildren();
        children.clear();
        
        GaugeCanvas gauge = new GaugeCanvas();
        gauge.setProperty(prop);
        gauge.bind(preferences, propertyStore, active);
        switch (propertyStore.getOriginalUnit(prop).getCategory())
        {
            case COORDINATE:
            case PLANE_ANGLE:
            case TIME:
            case UNKNOWN:
                break;
            default:
                TrendCanvas trend = new TrendCanvas(gauge.valueProperty());
                trend.bind(preferences, propertyStore, active);
                children.add(trend);
                break;
        }
        children.add(gauge);
    }

    private void onMousePressed(MouseEvent e)
    {
        List<I18nString> list = new ArrayList<>();
        for (String p : propertyStore.getDisplayProperties())
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
