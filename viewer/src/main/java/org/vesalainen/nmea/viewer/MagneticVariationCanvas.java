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
public class MagneticVariationCanvas extends RotatingCanvas implements PropertyBindable
{
    
    public MagneticVariationCanvas()
    {
        super(100);
    }
    
    @Override
    protected void onDraw()
    {
        GraphicsContext gc = getGraphicsContext2D();
        Paint color = adjustColor(Color.RED);
        gc.setFill(color);
        
        gc.beginPath();
        gc.moveTo(-2, 90);
        gc.lineTo(0, 100);
        gc.lineTo(2, 90);
        gc.closePath();
        gc.fill();
    }

    @Override
    public String[] bind(ViewerPreferences preferences, PropertyStore propertyStore)
    {
        angleProperty().bind(propertyStore.getBinding("magneticVariation"));
        return new String[]{"magneticVariation"};
    }
}
