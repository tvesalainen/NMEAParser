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
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.vesalainen.io.CompressedOutput;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author tkv
 */
public class TrackOutput extends JavaLogging implements AutoCloseable
{
    private final TrackPoint trackPoint = new TrackPoint();
    private final File directory;
    private boolean buffered;

    private String format;
    private CompressedOutput<TrackPoint> compressor;
    private File file;
    private boolean open;
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
        super(TrackOutput.class);
        this.directory = directory;
        this.format = format;
    }

    public void output(long time, float latitude, float longitude) throws IOException
    {
        if (!open)
        {
            open(time);
        }
        trackPoint.time = time;
        trackPoint.latitude = latitude;
        trackPoint.longitude = longitude;
        compressor.write();
        finer("input %d %f %f", time, latitude, longitude);
    }

    protected void open(long time) throws IOException
    {
        open = true;
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String dstr = sdf.format(new Date(time));
        file = new File(directory, dstr);
        fine("open %s", file);
        OutputStream out = new FileOutputStream(file);
        if (buffered)
        {
            out = new BufferedOutputStream(out);
        }
        compressor = new CompressedOutput<>(out, trackPoint);
    }

    @Override
    public void close() throws IOException
    {
        open = false;
        if (compressor != null)
        {
            compressor.close();
            if (!compressor.hasData())
            {
                file.delete();
            }
            compressor = null;
        }
        fine("close tracker file");
    }

    public TrackOutput setBuffered(boolean buffered)
    {
        this.buffered = buffered;
        return this;
    }
    
}
