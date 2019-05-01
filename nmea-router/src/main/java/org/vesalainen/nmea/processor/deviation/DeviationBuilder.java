/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nmea.processor.deviation;

import java.awt.BasicStroke;
import java.awt.Color;
import static java.awt.Font.BOLD;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vesalainen.math.PointList;
import org.vesalainen.math.PolarCubicSpline;
import org.vesalainen.navi.Navis;
import static org.vesalainen.ui.Direction.*;
import org.vesalainen.ui.PolarPlotter;
import org.vesalainen.util.ArrayHelp;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DeviationBuilder extends DeviationManager implements DeviationReadMXBean
{
    private PointList pointList = new PointList();
    
    public DeviationBuilder(Path path, double variation)
    {
        super(path, variation);
        updateTrueHeading();
    }

    @Override
    public String getDeviation(int deg)
    {
        return String.format(Locale.US, "%.1f", getDeviation(deg));
    }

    public void correct(double trueBearing, double radarBearing, double aisBearing)
    {
        double magneticBearing = Navis.normalizeAngle(trueBearing - variation);
        double deviation = Navis.angleDiff(radarBearing, aisBearing);
        if (pointList.indexOf(0, magneticBearing, Double.NaN, 1, 0) == -1)
        {
            pointList.add(magneticBearing, deviation);
            info("add correction magneticBearing %f deviation %f", magneticBearing, deviation);
            if (pointList.size() >= 4)
            {
                double[] array = pointList.array();
                ArrayHelp.sort(array, 2);
                spline = new PolarCubicSpline(array);
                if (spline.isInjection())
                {
                    info("created new deviation table");
                    updateTrueHeading();
                }
            }
        }
    }

    @Override
    public void store() throws IOException
    {
        if (spline != null && spline.isInjection())
        {
            points = new double[72];
            for (int ii = 0; ii < 36; ii ++)
            {
                points[2 * ii] = ii*10;
                points[2 * ii + 1] = spline.applyAsDouble(ii*10);
            }
            super.store();
        }
    }

    private void plot()
    {
        PolarPlotter plotter = new PolarPlotter(1000, 1000, Color.WHITE);
        plotter.setColor(Color.RED);
        plotter.setFont("Arial", BOLD, 10);
        plotter.draw(spline);
        plotter.setColor(Color.LIGHT_GRAY);
        plotter.drawCoordinates(LEFT, TOP);
        try
        {
            plotter.plot("deviation"+pointList.size()+".png");
        }
        catch (IOException ex)
        {
            Logger.getLogger(DeviationBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
