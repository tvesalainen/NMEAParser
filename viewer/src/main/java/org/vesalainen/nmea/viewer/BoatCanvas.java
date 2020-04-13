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
import javafx.scene.paint.Paint;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BoatCanvas extends RotatingCanvas implements PropertyBindable
{
    
    public BoatCanvas()
    {
        super(50);
    }

    @Override
    protected void onDraw()
    {
        GraphicsContext gc = getGraphicsContext2D();
        Paint color = adjustColor(Color.NAVY);
        gc.setStroke(color);
        
        gc.beginPath();
        gc.moveTo(-20, -40);
        gc.bezierCurveTo(-20, 10, -20, 10, 0, 50);
        gc.bezierCurveTo(20, 10, 20, 10, 20, -40);
        gc.bezierCurveTo(0, -45, 0, -45, -20, -40);
        gc.closePath();
        gc.stroke();
    }

    @Override
    public String[] bind(ViewerPreferences preferences, PropertyStore propertyStore)
    {
        angleProperty().bind(propertyStore.getBinding("trueHeading"));
        return new String[]{"trueHeading"};
    }
    
}
