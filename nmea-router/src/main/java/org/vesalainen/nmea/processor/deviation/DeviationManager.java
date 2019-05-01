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

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Locale;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.vesalainen.lang.Primitives;
import org.vesalainen.math.PolarCubicSpline;
import org.vesalainen.navi.Navis;
import org.vesalainen.parsers.nmea.NMEASentence;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class DeviationManager extends JavaLogging
{
    
    protected Path path;
    protected double[] points;
    protected ByteBuffer[] trueHeading = new ByteBuffer[3600];
    protected final ObjectName objectName;
    protected PolarCubicSpline spline;
    protected double variation;

    protected DeviationManager(Path path, double variation)
    {
        super(DeviationManager.class);
        try
        {
            this.path = path;
            this.variation = variation;
            this.objectName = new ObjectName("org.vesalainen.navi.deviation:type=manager");
            ManagementFactory.getPlatformMBeanServer().registerMBean(this, objectName);
        }
        catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public static DeviationManager getInstance(Path path, double variation) throws IOException
    {
        if (Files.exists(path))
        {
            return new EditableDeviationManager(path, variation);
        }
        else
        {
            return new DeviationBuilder(path, variation);
        }
    }
    public void store() throws IOException
    {
        try (final BufferedWriter bw = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING))
        {
            int len = points.length / 2;
            for (int ii = 0; ii < len; ii++)
            {
                bw.write(String.format(Locale.US, "%.0f %.1f\n", points[2 * ii], points[2 * ii + 1]));
            }
        }
    }

    public void load() throws IOException
    {
        List<String> lines = Files.readAllLines(path);
        points = new double[lines.size() * 2];
        int idx = 0;
        for (String line : lines)
        {
            String[] split = line.split(" ");
            if (split.length != 2)
            {
                throw new IllegalArgumentException(line);
            }
            points[idx++] = Primitives.parseDouble(split[0]);
            points[idx++] = Primitives.parseDouble(split[1]);
        }
    }

    public void reset()
    {
        points = new double[2 * 36];
        int len = 36;
        for (int ii = 0; ii < len; ii++)
        {
            points[2 * ii] = ii * 10;
        }
    }
    public double getDeviation(double deg)
    {
        if (spline != null && spline.isInjection())
        {
            return spline.applyAsDouble(deg);
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
            double a = (double)ii/10F;
            NMEASentence hdt = NMEASentence.hdt(Navis.normalizeAngle(getDeviation(a) + a + variation));
            trueHeading[ii] = hdt.getByteBuffer();
        }
    }

    public ByteBuffer getHDT(float deg)
    {
        return trueHeading[(int) (deg * 10F)];
    }

    public void updateVariation(double variation)
    {
        this.variation = variation;
    }

    public String getPath()
    {
        return path.toString();
    }

    public double getVariation()
    {
        return variation;
    }
    
}
