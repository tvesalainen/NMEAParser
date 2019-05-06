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
package org.vesalainen.nmea.processor;

import java.awt.Color;
import static java.awt.Font.BOLD;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.math.PolarCubicSpline;
import org.vesalainen.nmea.processor.deviation.DeviationManager;
import static org.vesalainen.ui.Direction.LEFT;
import static org.vesalainen.ui.Direction.TOP;
import org.vesalainen.ui.PolarPlotter;
import org.vesalainen.util.CharSequences;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DeviationManagerTest
{
    
    public DeviationManagerTest()
    {
    }

    @Test
    public void testCorrection() throws IOException
    {
        double variation = 10;
        Point2D.Double p1 = new Point2D.Double(121, -10.2);
        Point2D.Double p2 = new Point2D.Double(133, 12.3);
        Point2D.Double p3 = new Point2D.Double(156, 25.5);
        Point2D.Double p4 = new Point2D.Double(172, -19.2);
        Point2D.Double p5 = new Point2D.Double(179, -9.7);
        Point2D.Double p6 = new Point2D.Double(180, -15.6);
        Point2D.Double p7 = new Point2D.Double(182, -14.2);
        Point2D.Double p8 = new Point2D.Double(190, 2.1);
        Point2D.Double p9 = new Point2D.Double(200, -1.9);
        Point2D.Double p10 = new Point2D.Double(206, 1.4);
        PolarCubicSpline realDeviation = new PolarCubicSpline(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10);
        if (!realDeviation.isInjection())
        {
            realDeviation.forceInjection();
        }
        DeviationManager dm = new DeviationManager(Paths.get("foo"), 10);
        Random random = new Random(12345678L);
        for (int ii=0;ii<39;ii++)
        {
            double magneticHeading = 360*random.nextDouble();
            double aisHeading = magneticHeading + realDeviation.applyAsDouble(magneticHeading) + variation;
            double radarHeading = dm.getTrueHeading(magneticHeading);
            try
            {
                if (ii == 39)
                {
                    System.err.println();
                }
                dm.correct(aisHeading, radarHeading, aisHeading);
            }
            catch (Exception ex)
            {
                System.err.println();
            }
        }
        PolarPlotter plotter = new PolarPlotter(1000, 1000, Color.WHITE);
        plotter.setColor(Color.BLUE);
        plotter.setFont("Arial", BOLD, 20);
        plotter.draw(realDeviation);
        plotter.setColor(Color.RED);
        plotter.draw(dm.getSpline());
        plotter.setColor(Color.LIGHT_GRAY);
        plotter.drawCoordinates(LEFT, TOP);
        plotter.plot("dev3.png");
    }
    //@Test
    public void test1() throws IOException
    {
        DeviationManager dm = new DeviationManager(Paths.get("foo"), 10);
        assertEquals("$HCHDT,280,T*3D\r\n", get(dm.getHDT(270)));
        dm.updateMagneticHeading(270);
        dm.setPlus5();
        assertEquals("$HCHDT,285,T*38\r\n", get(dm.getHDT(270)));
        dm.updateMagneticHeading(270);
        dm.setPlus1();
        assertEquals("$HCHDT,286,T*3B\r\n", get(dm.getHDT(270)));
        assertEquals("$HCHDT,316,T*33\r\n", get(dm.getHDT(300)));
        dm.updateMagneticHeading(300);
        dm.setPlus1();
    }
    private String get(ByteBuffer bb)
    {
        return CharSequences.getAsciiCharSequence(bb).toString();
    }
    //@Test
    public void generate()
    {
        for (int ii=0;ii<36;ii++)
        {
            System.err.println("public String getDeviation"+ii+"0() {return getDeviation("+ii+"0);}");
            System.err.println("public void setDeviation"+ii+"0(String deviation) {setDeviation("+ii+"0, deviation);}");
        }
    }
    
}
