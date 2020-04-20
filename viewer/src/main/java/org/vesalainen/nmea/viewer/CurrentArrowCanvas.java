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
import javafx.scene.paint.Paint;
import org.vesalainen.fx.InterpolatingColor;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CurrentArrowCanvas extends RotatingValueCanvas implements PropertyBindable
{
    private InterpolatingColor currentColor = new InterpolatingColor(0, 180, 2, 50, 4, -40, 8, -80);

    public CurrentArrowCanvas()
    {
        super(100);
    }

    @Override
    protected void onDraw()
    {
        GraphicsContext gc = getGraphicsContext2D();
        Paint paint = adjustColor(currentColor.color(getValue()));
        gc.setStroke(paint);
        gc.beginPath();
        gc.moveTo(0, 0);
        for (int ii=0;ii<100;ii+=20)
        {
            double sign = (ii%40==0) ? -1 : 1;
            gc.quadraticCurveTo(sign*4, ii+10, 0, ii+20);
        }
        gc.stroke();
    }

    @Override
    public void bind(ViewerPreferences preferences, PropertyStore propertyStore, BooleanProperty active)
    {
        super.bind(preferences, propertyStore, active);
        angleProperty().bind(propertyStore.getBinding("currentAngleOverGround"));
        valueProperty().bind(propertyStore.getBinding("currentSpeedOverGround"));
        disableProperty().bind(propertyStore.getDisableBind("currentAngleOverGround", "currentSpeedOverGround"));
    }
    
}
