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

import javafx.beans.property.BooleanProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.vesalainen.fx.InterpolatingColor;
import org.vesalainen.navi.WindArrow;
import org.vesalainen.ui.path.FunctionalPathMaker;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class WindArrowCanvas extends RotatingValueCanvas implements PropertyBindable
{

    private final GraphicsContext gc;
    private final PathMaker pathMaker;
    private final WindArrow windArrow;
    private InterpolatingColor windColor = new InterpolatingColor(0, 180, 20, 50, 40, -40, 80, -80);

    public WindArrowCanvas()
    {
        super(50);
        getStyleClass().add("wind-arror-canvas");
        this.gc = getGraphicsContext2D();
        this.pathMaker = new PathMaker(gc, this);
        this.windArrow = new WindArrow(pathMaker);
    }

    @Override
    protected void onDraw()
    {
        GraphicsContext gc = getGraphicsContext2D();
        gc.scale(1, -1);
        windArrow.draw(getValue());
        gc.scale(1, -1);
    }

    @Override
    public void bind(ViewerPreferences preferences, PropertyStore propertyStore, BooleanProperty active)
    {
        super.bind(preferences, propertyStore, active);
        angleProperty().bind(propertyStore.getBinding("windAngleOverGround"));
        valueProperty().bind(propertyStore.getBinding("windSpeedOverGround"));
        disableProperty().bind(propertyStore.getDisableBind("windAngleOverGround", "windSpeedOverGround"));
    }
    
    private Color color(String s)
    {
        return windColor.color(getValue());
    }
    
    private class PathMaker extends FunctionalPathMaker<Paint>
    {

        private PathMaker(GraphicsContext gc, WindArrowCanvas wac)
        {
            super(
                    gc::beginPath, 
                    gc::moveTo, 
                    gc::lineTo, 
                    gc::quadraticCurveTo, 
                    gc::bezierCurveTo, 
                    ()->
                    {
                        gc.closePath();
                        gc.fill();
                    }, 
                    gc::setFill, 
                    (s)->wac.adjustColor(color(s))
            );
        }
    }
}
