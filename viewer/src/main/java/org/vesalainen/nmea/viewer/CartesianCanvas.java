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
import javafx.scene.transform.Affine;
import org.vesalainen.ui.Transforms;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CartesianCanvas extends ResizableCanvas
{
    protected final double max;
    protected final Affine transform = new Affine();
    private final DoubleProperty scale = new SimpleDoubleProperty(1);

    double getScale()
    {
        return scale.get();
    }

    void setScale(double value)
    {
        scale.set(value);
    }

    DoubleProperty scaleProperty()
    {
        return scale;
    }
    
    public CartesianCanvas()
    {
        this(100);
    }

    protected CartesianCanvas(double maxValue)
    {
        super(true);
        this.max = maxValue;
        onReDrawListener.bind(scale);
    }

    @Override
    protected void transform()
    {
        double width = getWidth();
        double height = getHeight();
        if (width > 0 && height > 0)
        {
            GraphicsContext gc = getGraphicsContext2D();
            Transforms.createScreenTransform(
                    width, 
                    height, 
                    -max, 
                    -max, 
                    max, 
                    max, 
                    true, 
                    (double mxx, double mxy, double myx, double myy, double tx, double ty)->
                    {
                        transform.setToTransform(mxx, mxy, tx, myx, myy, ty);
                    });
            double sca = getScale();
            transform.appendScale(sca, sca);
            gc.setTransform(transform);
            double m = max/sca;
            gc.clearRect(-m, -m, 2*m, 2*m);
        }
    }


}
