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
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.beans.binding.When;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.vesalainen.fx.CanvasPlotter;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ResizableCanvas extends Canvas implements Initializable
{

    private final BooleanProperty square = new SimpleBooleanProperty(true);

    public boolean isSquare()
    {
        return square.get();
    }

    public void setSquare(boolean value)
    {
        square.set(value);
    }

    public BooleanProperty squareProperty()
    {
        return square;
    }

    public ResizableCanvas()
    {
        try
        {
            ResourceBundle bundle = ResourceBundle.getBundle(I18n.class.getName(), Locale.getDefault());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ResizableCanvas.fxml"), bundle);
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
    public double prefHeight(double width)
    {
        return getHeight();
    }

    @Override
    public double prefWidth(double height)
    {
        return getWidth();
    }

    @Override
    public boolean isResizable()
    {
        return true;
    }

    protected void onDraw()
    {
        if (getWidth() > 0 && getHeight() > 0)
        {
            CanvasPlotter plotter = new CanvasPlotter(this);
            plotter.setFont("arial", 0, 1);
            plotter.drawCircle(0, 0, 1);
            plotter.drawCoordinates();
            plotter.plot();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        parentProperty().addListener(this::setParent);
    }
    private void setParent(ObservableValue<? extends Parent> observable, Parent oldValue, Parent newParent)
    {
        if (newParent instanceof Region)
        {
            Region region = (Region) newParent;
            ReadOnlyDoubleProperty regionWidth = region.widthProperty();
            ReadOnlyDoubleProperty regionHeight = region.heightProperty();
            regionWidth.addListener(evt -> onDraw());
            regionHeight.addListener(evt -> onDraw());
            
            widthProperty()
                    .bind(
                            new When(square)
                                    .then(new When(regionWidth.lessThanOrEqualTo(regionHeight))
                                            .then(regionWidth)
                                            .otherwise(regionHeight))
                                    .otherwise(regionWidth));
            heightProperty()
                    .bind(
                            new When(square)
                                    .then(new When(regionHeight.lessThanOrEqualTo(regionWidth))
                                            .then(regionHeight)
                                            .otherwise(regionWidth))
                                    .otherwise(regionHeight));
        }
        else
        {
            System.err.println(newParent+" not suitable for ResizableCanvas");
        }
    }
}
