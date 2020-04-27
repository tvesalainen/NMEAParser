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
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.RotateEvent;
import static javafx.scene.input.RotateEvent.ROTATE;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RotatingCanvas extends CartesianCanvas
{

    private final BooleanProperty rotating = new SimpleBooleanProperty(false);

    public boolean isRotating()
    {
        return rotating.get();
    }

    public void setRotating(boolean value)
    {
        rotating.set(value);
    }

    public BooleanProperty rotatingProperty()
    {
        return rotating;
    }
    private final DoubleProperty angle = new SimpleDoubleProperty(this, "angle", 0);

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

    protected Point2D mousePressed;
    
    protected RotatingCanvas(double maxValue)
    {
        super(maxValue);
        onReDrawListener.bind(angleProperty());
        addEventFilter(ROTATE, e->rotate(e));
    }

    protected void transform()
    {
        super.transform();
        GraphicsContext gc = getGraphicsContext2D();
        gc.setTransform(transform);
        gc.rotate(-getAngle());
    }
    private void rotate(RotateEvent re)
    {
        if (isRotating())
        {
            setAngle(getAngle()+re.getAngle());
        }
    }
}
