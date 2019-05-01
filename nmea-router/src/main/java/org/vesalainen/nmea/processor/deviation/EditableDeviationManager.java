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
import static java.nio.file.StandardOpenOption.*;
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
import org.vesalainen.util.ArrayHelp;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class EditableDeviationManager extends DeviationManager implements DeviationReadMXBean, DeviationWriteMXBean
{

    protected EditableDeviationManager(Path path, double variation) throws IOException
    {
        super(path, variation);
        load();
    }

    @Override
    public void rotate(double diff)
    {
        for (int ii = 0; ii < 36; ii ++)
        {
            points[2 * ii] = Navis.normalizeAngle(points[2 * ii]+diff);
        }
        ArrayHelp.sort(points, 2);
        update();
        for (int ii = 0; ii < 36; ii ++)
        {
            points[2 * ii] = ii*10;
            points[2 * ii + 1] = spline.applyAsDouble(ii*10);
        }
    }

    @Override
    public final void load() throws IOException
    {
        super.load();
        update();
    }
    @Override
    public void updateVariation(double variation)
    {
        super.updateVariation(variation);
        updateTrueHeading();
    }
    private void update()
    {
        spline = new PolarCubicSpline(points);
        updateTrueHeading();
    }
    @Override
    public String getDeviation(int deg)
    {
        return String.format(Locale.US, "%.1f", points[2*deg/10+1]);
    }

    @Override
    public void setDeviation(int deg, String deviation)
    {
        points[2*deg/10+1] = Primitives.parseDouble(deviation);
        update();
    }

}
