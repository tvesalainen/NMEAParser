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

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.GraphicsContext;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RotatingCanvas extends CartesianCanvas
{
    private final DoubleProperty angle = new SimpleDoubleProperty(0);

    public double getAngle()
    {
        return angle.get();
    }

    public void setAngle(double value)
    {
        angle.set(value);
    }

    public DoubleProperty angleProperty()
    {
        return angle;
    }

    protected RotatingCanvas(double maxValue)
    {
        super(maxValue);
    }
    
    /**
     * When overriding first call super.onDraw to set angle
     * @param gc 
     */
    @Override
    protected void onDraw(GraphicsContext gc)
    {
        gc.rotate(360-angle.doubleValue());
    }
    
}
