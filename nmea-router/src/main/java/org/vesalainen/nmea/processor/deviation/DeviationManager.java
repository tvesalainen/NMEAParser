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

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.vesalainen.lang.Primitives;
import org.vesalainen.math.PointList;
import org.vesalainen.math.PolarCubicSpline;
import org.vesalainen.navi.Navis;
import org.vesalainen.parsers.nmea.NMEASentence;
import static org.vesalainen.ui.Direction.LEFT;
import static org.vesalainen.ui.Direction.TOP;
import org.vesalainen.ui.PolarPlotter;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DeviationManager extends JavaLogging implements DeviationMXBean
{
    
    protected Path path;
    protected final Accumulator[] accumulators = new Accumulator[36];
    protected final PointList points = new PointList();
    protected ByteBuffer[] trueHeading = new ByteBuffer[3600];
    protected final ObjectName objectName;
    protected PolarCubicSpline spline;
    protected double variation;
    protected double magneticHeading;
    protected long headingTimestamp;

    public DeviationManager(Path path, double variation) throws IOException
    {
        super(DeviationManager.class);
        try
        {
            this.path = path;
            this.variation = variation;
            this.objectName = new ObjectName("org.vesalainen.navi.deviation:type=manager");
            ManagementFactory.getPlatformMBeanServer().registerMBean(this, objectName);
            load();
        }
        catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public String getDeviation(int deg)
    {
        return String.format(Locale.US, "%.1f", getDeviation((double)deg));
    }

    public void correct(double magneticHeading, double radarHeading, double aisHeading, double weight)
    {
        fine("mh %.1f: radar %.1f  ais %.1f", magneticHeading, radarHeading, aisHeading);
        double deviation = Navis.angleDiff(radarHeading, aisHeading);
        setDeviation(magneticHeading, deviation, weight);
    }

    public void setDeviation(double magneticHeading, double deviation, double weight)
    {
        double cumulatedDeviation = getDeviation(magneticHeading) + deviation;
        fine("mh %.1f: %.1f + %.1f = %.1f", magneticHeading, getDeviation(magneticHeading), deviation, cumulatedDeviation);
        info("add correction magneticBearing %.1f deviation %.1f", magneticHeading, cumulatedDeviation);
        int index = (int) (magneticHeading/10);
        Accumulator acc = accumulators[index];
        if (acc == null)
        {
            acc = new Accumulator();
            accumulators[index] = acc;
        }
        acc.add(magneticHeading, cumulatedDeviation, weight);
        update();
        try
        {
            store();
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    @Override
    public void setDeviation(double deviation)
    {
        if (System.currentTimeMillis() - headingTimestamp < 1800)
        {
            double cumulatedDeviation = getDeviation(magneticHeading) + deviation;
            int index = (int) (magneticHeading/10);
            Accumulator acc = new Accumulator();
            acc.add(magneticHeading, cumulatedDeviation);
            accumulators[index] = acc;
            info("force correction magneticBearing %.1f deviation %.1f", magneticHeading, cumulatedDeviation);
            update();
        }
        else
        {
            info("outdated magneticHeading");
        }
    }

    @Override
    public void plot(String path) throws IOException
    {
        PolarPlotter plotter = new PolarPlotter(1000, 1000, Color.WHITE);
        plotter.setColor(Color.GREEN);
        plotter.draw(spline);
        plotter.setColor(Color.LIGHT_GRAY);
        plotter.drawCoordinates();
        plotter.plot(path);
    }

    @Override
    public void store() throws IOException
    {
        try (final BufferedWriter bw = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING))
        {
            for (Accumulator acc :  accumulators)
            {
                if (acc != null)
                {
                    bw.write(String.format("%s\n", acc));
                }
            }
        }
    }

    @Override
    public final void load() throws IOException
    {
        if (Files.exists(path))
        {
            for (String line : Files.readAllLines(path))
            {
                Accumulator acc = new Accumulator(line);
                int index = (int) (acc.getX()/10);
                accumulators[index] = acc;
            }
        }
        update();
}

    @Override
    public void reset()
    {
        points.clear();
        spline = null;
    }
    public double getDeviation(double deg)
    {
        if (spline != null && spline.isInjection())
        {
            return spline.eval(deg, 0.0001);
        }
        else
        {
            return 0;
        }
    }
    protected void updateTrueHeading()
    {
        for (int ii=0;ii<3600;ii++)
        {
            double magneticHeading = (double)ii/10F;
            NMEASentence hdt = NMEASentence.hdt(getTrueHeading(magneticHeading));
            trueHeading[ii] = hdt.getByteBuffer();
        }
    }

    public ByteBuffer getHDT(float deg)
    {
        return trueHeading[(int) (deg * 10F)];
    }
    public double getTrueHeading(double magneticHeading)
    {
        return Navis.normalizeAngle(magneticHeading + getDeviation(magneticHeading) + variation);
    }
    public void updateMagneticHeading(double magneticHeading)
    {
        this.magneticHeading = magneticHeading;
        this.headingTimestamp = System.currentTimeMillis();
    }

    public void updateVariation(double variation)
    {
        this.variation = variation;
        updateTrueHeading();
    }

    private void update()
    {
        points.clear();
        for (Accumulator a :  accumulators)
        {
            if (a != null)
            {
                points.add(a.getX(), a.getY());
            }
        }
        if (!points.isEmpty())
        {
            spline = new PolarCubicSpline(points.array());
            if (!spline.isInjection())
            {
                spline.forceInjection();
            }
            info("created new deviation table");
        }
        updateTrueHeading();
    }
    @Override
    public String getPath()
    {
        return path.toString();
    }

    @Override
    public double getVariation()
    {
        return variation;
    }

    public PolarCubicSpline getSpline()
    {
        return spline;
    }
    
}
