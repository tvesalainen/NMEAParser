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
package org.vesalainen.nmea.experimental;

import java.awt.Color;
import static java.awt.Font.BOLD;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.logging.Level;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.lang.Primitives;
import org.vesalainen.math.AngleAverage;
import org.vesalainen.math.AngleAverageSeeker;
import org.vesalainen.math.PolarCubicSpline;
import org.vesalainen.math.SimpleAverage;
import org.vesalainen.navi.Navis;
import org.vesalainen.parser.util.Input;
import org.vesalainen.parsers.nmea.NMEAChecksum;
import org.vesalainen.parsers.nmea.NMEAParser;
import static org.vesalainen.ui.Direction.*;
import org.vesalainen.ui.PolarPlotter;
import org.vesalainen.util.logging.JavaLogging;
import org.xml.sax.InputSource;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class GPSCompassT
{
    
    public GPSCompassT()
    {
        JavaLogging.setConsoleHandler("org.vesalainen", Level.CONFIG);
    }

    //@Test
    public void testMain()
    {
        GPSCompass.main();
    }
    @Test
    public void test1() throws IOException, URISyntaxException
    {
        try (
                InputStream is = GPSCompassT.class.getResourceAsStream("/deviation.txt");
                InputStreamReader isr = new InputStreamReader(is, US_ASCII);
                BufferedReader br = new BufferedReader(isr)
                )
        {
            double[] points = new double[72];
            int idx = 0;
            String line = br.readLine();
            while (line != null)
            {
                String[] split = line.trim().split(" ");
                double deg = Primitives.parseDouble(split[0])*10;
                double hea = Primitives.parseDouble(split[2]);
                System.err.printf("%.0f %.1f\n", 
                        deg,
                        Navis.angleDiff(deg, hea-70)
                        );
                points[idx++] = deg;
                points[idx++] = Navis.angleDiff(deg, hea-70)+100;
                line = br.readLine();
            }
            PolarCubicSpline spline = new PolarCubicSpline(points);
            PolarPlotter plotter = new PolarPlotter(1000, 1000, Color.WHITE);
            plotter.setColor(Color.RED);
            plotter.setFont("Arial", BOLD, 20);
            //plotter.drawLine(0, 1, 360, 1);
            plotter.draw(spline);
            plotter.setColor(Color.GRAY);
            plotter.drawCoordinates(LEFT, TOP);
            plotter.plot("deviation.png");
        }
    }
    //@Test
    public void test_2() throws IOException, URISyntaxException
    {
        Path dir = Paths.get("C:\\Users\\tkv\\share");
        Path path = dir.resolve("ais.nmea");
        Input.getInstance(new InputSource(), 8192);
        NMEAParser parser = NMEAParser.newInstance();
        parser.parse(Files.newByteChannel(path), null, null);
    }
    //@Test
    public void test_1() throws IOException
    {
        String msg = "$GPGLL,0854.99526,S,14006.04630,W,234347.00,A,D*6C";
        NMEAChecksum s = new NMEAChecksum();
        s.update(msg);
        assertEquals(0x6c, s.getValue());
    }
    //@Test
    public void test0()
    {
        double delta = 1.0;
        Random rand = new Random(1234567L);
        SimpleAverage ave = new SimpleAverage();
        AngleAverage aave = new AngleAverage();
        AngleAverageSeeker seeker = new AngleAverageSeeker(256);
        int count = 0;
        while (!seeker.isWithin(delta))
        {
            double lon1 = random(rand);
            double lat1 = random(rand);
            double lon2 = 3+random(rand);
            double lat2 = random(rand);
            double distance = Math.hypot(lat1-lat2,lon1-lon2);
            double bearing = Navis.bearing(lat1, lon1, lat2, lon2);
            ave.add(distance);
            aave.addDeg(bearing);
            seeker.add(bearing);
            //System.err.printf("%s %s\n", ave, aave);
            count++;
        }
        assertEquals(90.0, aave.averageDeg(), delta);
        assertEquals(3.0, ave.fast(), delta);
    }
    private double random(Random rand)
    {
        return rand.nextDouble()*2.0-1.0;
    }
}
