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
import javafx.scene.paint.Paint;
import org.vesalainen.ui.SVGHelp;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CompassRingCanvas extends RotatingCanvas
{
    private static final double TEXT_LIMIT = 300;
    private static final double LIMIT10 = 100;
    private static final double LIMIT5 = 300;
    private static final double LIMIT1 = 500;
    public CompassRingCanvas()
    {
        super(100);
        getStyleClass().add("compass-ring-canvas");
    }

    @Override
    protected void onDraw()
    {
        GraphicsContext gc = getGraphicsContext2D();
        double width = getWidth();
        double height = getHeight();
        boolean haveText = height >= TEXT_LIMIT;
        boolean have10 = height >= LIMIT10;
        boolean have5 = height >= LIMIT5;
        boolean have1 = height >= LIMIT1;
        Paint textFill = adjustColor(getTextFill());
        gc.setFill(textFill);
        gc.setStroke(textFill);
        String fontFamily = getFont().getFamily();
        for (int ii = 0;ii <360;ii++)
        {
            switch (ii)
            {
                case 30:
                case 60:
                case 120:
                case 150:
                case 210:
                case 240:
                case 300:
                case 330:
                    if (haveText)
                    {
                        String path = SVGHelp.toPath(fontFamily, 10, 0, 0, max, String.valueOf(ii), org.vesalainen.ui.TextAlignment.MIDDLE_X);
                        gc.beginPath();
                        gc.appendSVGPath(path);
                        gc.closePath();
                        gc.fill();
                    }
                    break;
            }
            if (have10 && (ii % 10) == 0)
            {
                gc.strokeLine(0, 90, 0, max);
            }
            else
            {
                if (have5 && (ii % 5) == 0)
                {
                    gc.strokeLine(0, 95, 0, max);
                }
                else
                {
                    if (have1)
                    {
                        gc.strokeLine(0, 97, 0, max);
                    }
                }
            }
            gc.rotate(-1);
        }
    }
    
}
