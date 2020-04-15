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

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CourseSpeedCanvas extends RotatingValueCanvas implements PropertyBindable
{
    private static final int GAP = 4;
    public CourseSpeedCanvas()
    {
        super(100);
    }

    @Override
    protected void onDraw()
    {
        GraphicsContext gc = getGraphicsContext2D();
        double value = getValue();
        if (value > 0)
        {
            gc.setStroke(adjustColor(Color.ORANGE));
            gc.setLineCap(StrokeLineCap.ROUND);
            gc.setLineWidth(4);
            gc.setLineDashes(dashes(value));
            gc.strokeLine(0, 0, 0, max-GAP);
        }
    }

    private double[] dashes(double value)
    {
        int ceil = (int) Math.ceil(value);
        int ceil1 = ceil-1;
        double[] arr = new double[2*ceil];
        double d = (max-ceil1-GAP)/value;
        for (int ii=0;ii<ceil1;ii++)
        {
            arr[2*ii] = d;
            arr[2*ii+1] = GAP;
        }
        arr[2*ceil1] = d*(value-ceil1);
        arr[2*ceil1+1] = 0;
        return arr;
    }

    @Override
    public void bind(ViewerPreferences preferences, PropertyStore propertyStore)
    {
        angleProperty().bind(propertyStore.getBinding("trackMadeGood"));
        valueProperty().bind(propertyStore.getBinding("speedOverGround"));
        disableProperty().bind(propertyStore.getDisableBind("trackMadeGood", "speedOverGround"));
    }
    
    
}
