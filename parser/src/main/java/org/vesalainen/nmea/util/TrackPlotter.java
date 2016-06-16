/*
 * Copyright (C) 2016 tkv
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
package org.vesalainen.nmea.util;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.UnaryOperator;
import org.vesalainen.math.AbstractPoint;
import org.vesalainen.math.Point;
import org.vesalainen.ui.Plotter;
import org.vesalainen.ui.Plotter.Polyline;

/**
 *
 * @author tkv
 */
public class TrackPlotter
{
    private TrackInput input;
    private Plotter plotter;
    private Color color;

    public TrackPlotter(String filename, int width, int height, Color pen, Color background) throws IOException
    {
        this(new File(filename), width, height, pen, background);
    }
    
    public TrackPlotter(File file, int width, int height, Color pen, Color background) throws IOException
    {
        this(new FileInputStream(file), width, height, pen, background);
    }
    
    public TrackPlotter(InputStream is, int width, int height, Color penColor, Color background) throws IOException
    {
        input = new TrackInput(is);
        plotter = new Plotter(width, height, background);
        color = penColor;
    }
    
    public void plot(String filename, String ext) throws IOException
    {
        plot(new File(filename), ext);
    }
    
    public void plot(File file, String ext) throws IOException
    {
        Polyline polyline = plotter.polyline(color);
        polyline.lineTo(input.stream().map((t)->{return new AbstractPoint(t.longitude, t.latitude);}).map(new DepCorr()));
        plotter.drawPolyline(polyline);
        plotter.plot(file, ext);
    }
    
    private static class DepCorr implements UnaryOperator<AbstractPoint>
    {
        private double dep = Double.NaN;
        @Override
        public AbstractPoint apply(AbstractPoint t)
        {
            if (Double.isNaN(dep))
            {
                dep = Math.cos(Math.toRadians(t.getY()));
            }
            t.setX(dep*t.getX());
            return t;
        }
        
    }
}
