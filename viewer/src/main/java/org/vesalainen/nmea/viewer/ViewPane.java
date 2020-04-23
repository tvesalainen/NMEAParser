package org.vesalainen.nmea.viewer;


import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.ScrollEvent;
import static javafx.scene.input.ScrollEvent.SCROLL;
import javafx.scene.layout.StackPane;

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
/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ViewPane extends StackPane implements PropertyBindable
{
    private double exp;
    
    private final BooleanProperty zoom = new SimpleBooleanProperty(false);

    public boolean isZoom()
    {
        return zoom.get();
    }

    public void setZoom(boolean value)
    {
        zoom.set(value);
    }
    private final DoubleProperty scale = new SimpleDoubleProperty(1);

    private double getScale()
    {
        return scale.get();
    }

    private void setScale(double value)
    {
        scale.set(value);
    }

    private DoubleProperty scaleProperty()
    {
        return scale;
    }

    public BooleanProperty zoomProperty()
    {
        return zoom;
    }

    public ViewPane()
    {
        setMinHeight(0);
        setMinWidth(0);
        setMaxHeight(Double.MAX_VALUE);
        setMaxWidth(Double.MAX_VALUE);
        addEventHandler(SCROLL, e -> onScroll(e));
    }

    private void onScroll(ScrollEvent e)
    {
        if (isZoom())
        {
            double width = getWidth();
            Point2D screenToLocal = screenToLocal(e.getScreenX(), e.getScreenY());
            if (screenToLocal.getX() > 3*width/4)
            {
                exp += e.getDeltaY();
                setScale(Math.pow(0.5, exp/40));
                System.err.println(exp);
            }
        }
    }

    @Override
    public void bind(ViewerPreferences preferences, PropertyStore propertyStore, BooleanProperty active)
    {
        if (isZoom())
        {
            getChildren().forEach((Node node)->
            {
                if (node instanceof CartesianCanvas)
                {
                    CartesianCanvas cc = (CartesianCanvas) node;
                    cc.scaleProperty().bind(scale);
                }
            });
        }
    }

}
