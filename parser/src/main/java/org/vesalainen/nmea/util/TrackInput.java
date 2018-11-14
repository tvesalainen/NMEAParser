/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.stream.Stream;
import org.vesalainen.io.ObjectCompressedInput;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TrackInput implements AutoCloseable
{
    private final TrackPoint trackPoint = new TrackPoint();
    private ObjectCompressedInput<TrackPoint> input;

    public TrackInput(String filename) throws IOException
    {
        this(new File(filename));
    }
    public TrackInput(File file) throws IOException
    {
        this(new FileInputStream(file));
    }
    public TrackInput(InputStream is) throws IOException
    {
        if (!(is instanceof BufferedInputStream))
        {
            is = new BufferedInputStream(is);
        }
        input = new ObjectCompressedInput<>(is, trackPoint);
    }
    /**
     * Returns the UUID of compressed track
     * @return 
     */
    public UUID getUuid()
    {
        return input.getUuid();
    }
    /**
     * Reads a new position
     * @return True if read succeeded. False if eof.
     * @throws IOException 
     */
    public boolean read() throws IOException
    {
        try
        {
            input.read();
            return true;
        }
        catch (EOFException ex)
        {
            return false;
        }
    }
    
    public long getTime()
    {
        return trackPoint.time;
    }
    public double getLatitude()
    {
        return trackPoint.latitude;
    }
    public double getLongitude()
    {
        return trackPoint.longitude;
    }

    @Override
    public void close() throws IOException
    {
        input.close();
    }
    
    public Stream<TrackPoint> stream()
    {
        return input.stream();
    }
}
