/*
 * Copyright (C) 2015 tkv
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.vesalainen.io.CompressedOutput;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author tkv
 */
public class TrackOutput extends TrackFilter implements AutoCloseable
{
    private final TrackPoint trackPoint = new TrackPoint();
    private final File directory;
    private String format;
    private CompressedOutput<TrackPoint> compressor;
    private final JavaLogging log = new JavaLogging();
    /**
     * Creates a TrackOutput for writing compressed track file. Filename is 
     * comprised of track starting date
     * @param directory 
     */
    public TrackOutput(File directory)
    {
        this(directory, "yyyyMMddHHmmss'.trc'");
    }
    /**
     * Creates a TrackOutput for writing compressed track file. 
     * @param directory 
     * @param format Track file name format as in SimpleDateFormat
     * @see java.text.SimpleDateFormat
     */
    public TrackOutput(File directory, String format)
    {
        this.directory = directory;
        this.format = format;
        log.setLogger(this.getClass());
    }

    @Override
    protected void output(long time, float latitude, float longitude) throws IOException
    {
        trackPoint.time = time;
        trackPoint.latitude = latitude;
        trackPoint.longitude = longitude;
        compressor.write();
        log.finer("input %d %f %f", time, latitude, longitude);
    }

    @Override
    protected void open(long time) throws IOException
    {
        super.open(time);
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String dstr = sdf.format(new Date(time));
        File file = new File(directory, dstr);
        log.fine("open %s", file);
        FileOutputStream out = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(out);
        compressor = new CompressedOutput<>(bos, trackPoint);
    }

    @Override
    public void close() throws IOException
    {
        super.close();
        if (compressor != null)
        {
            compressor.close();
            compressor = null;
        }
        log.fine("close tracker file");
    }

    @Override
    public TrackOutput setMaxPassive(long maxPassive)
    {
        return (TrackOutput) super.setMaxPassive(maxPassive);
    }

    @Override
    public TrackOutput setMaxSpeed(double maxSpeed)
    {
        return (TrackOutput) super.setMaxSpeed(maxSpeed);
    }

    @Override
    public TrackOutput setMinDistance(double minDistance)
    {
        return (TrackOutput) super.setMinDistance(minDistance);
    }

    @Override
    public TrackOutput setBearingTolerance(double bearingTolerance)
    {
        return (TrackOutput) super.setBearingTolerance(bearingTolerance);
    }

}
