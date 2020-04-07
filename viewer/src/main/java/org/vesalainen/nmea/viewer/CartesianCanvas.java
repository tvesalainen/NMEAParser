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

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Transform;
import org.vesalainen.ui.Transforms;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CartesianCanvas extends ResizableCanvas
{

    protected final double max;
    protected final Affine transform = new Affine();
    
    public CartesianCanvas()
    {
        super(true);
        this.max = 100;
    }

    protected CartesianCanvas(double maxValue)
    {
        super(true);
        this.max = maxValue;
    }

    @Override
    protected void onSize()
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
            gc.setTransform(transform);
            onDraw();
        }
    }

    @Override
    protected final void onDraw()
    {
        double width = getWidth();
        double height = getHeight();
        if (width > 0 && height > 0)
        {
            GraphicsContext gc = getGraphicsContext2D();
            gc.clearRect(-max, -max, 2*max, 2*max);
            onDraw(gc);
        }
    }
    /**
     * On call the gc is set to cartesian coordinates x (-max to max) y (-max to max)
     * @param gc 
     */
    protected void onDraw(GraphicsContext gc)
    {
        double width = getWidth();
        double height = getHeight();
        gc.setFont(Font.font(height/10));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText("0", 0, max);
        gc.setStroke(Color.RED);
        gc.strokeLine(-max, 0, max, 0);
        gc.strokeLine(0, -max, 0, max);
    }
}
