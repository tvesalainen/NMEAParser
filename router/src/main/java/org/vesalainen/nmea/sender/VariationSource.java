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
package org.vesalainen.nmea.sender;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CheckedOutputStream;
import org.vesalainen.code.PropertySetter;
import org.vesalainen.nio.channels.ByteBufferOutputStream;
import org.vesalainen.nio.channels.UnconnectedDatagramChannel;
import org.vesalainen.nmea.jaxb.router.VariationSourceType;
import org.vesalainen.parsers.nmea.Clock;
import org.vesalainen.parsers.nmea.NMEAChecksum;
import org.vesalainen.parsers.nmea.NMEAGen;
import org.vesalainen.util.Transactional;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author tkv
 */
public class VariationSource extends JavaLogging implements PropertySetter, Transactional
{
    private static final String[] Prefixes = new String[]{
        "latitude",
        "longitude",
        "clock"
            };
    private UnconnectedDatagramChannel channel;
    private float trackMadeGood;
    private float speedOverGround;
    private float longitude;
    private float latitude;
    private boolean positionUpdated;
    private final ByteBuffer bb = ByteBuffer.allocateDirect(100);
    private final ByteBufferOutputStream out = new ByteBufferOutputStream(bb);
    private final CheckedOutputStream cout = new CheckedOutputStream(out, new NMEAChecksum());
    private GregorianCalendar calendar;
    private final VariationSourceType variationSourceType;

    public VariationSource(UnconnectedDatagramChannel channel, VariationSourceType variationSourceType)
    {
        setLogger(this.getClass());
        this.channel = channel;
        this.variationSourceType = variationSourceType;
    }
    
    @Override
    public String[] getPrefixes()
    {
        return Prefixes;
    }

    @Override
    public void rollback(String reason)
    {
        warning("rollback(%s)", reason);
    }

    @Override
    public void commit(String reason)
    {
        if (positionUpdated)
        {
            bb.clear();
            try
            {
                fine("location %f %f", latitude, longitude);
                NMEAGen.rmc(cout, latitude, longitude, calendar);
                bb.flip();
                channel.write(bb);
            }
            catch (IOException ex)
            {
                Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
            }
            positionUpdated = false;
        }
    }
    
    @Override
    public void set(String property, boolean arg)
    {
        switch (property)
        {
            
        }
    }

    @Override
    public void set(String property, byte arg)
    {
        switch (property)
        {
            
        }
    }

    @Override
    public void set(String property, char arg)
    {
        switch (property)
        {
            
        }
    }

    @Override
    public void set(String property, short arg)
    {
        switch (property)
        {
            
        }
    }

    @Override
    public void set(String property, int arg)
    {
        switch (property)
        {
            
        }
    }

    @Override
    public void set(String property, long arg)
    {
        switch (property)
        {
            
        }
    }

    @Override
    public void set(String property, float arg)
    {
        switch (property)
        {
            case "latitude":
                latitude = arg;
                positionUpdated = true;
                break;
            case "longitude":
                longitude = arg;
                break;
        }
    }

    @Override
    public void set(String property, double arg)
    {
        switch (property)
        {
            
        }
    }

    @Override
    public void set(String property, Object arg)
    {
        switch (property)
        {
            case "clock":
                Clock clock = (Clock) arg;
                calendar = clock.getCalendar();
                break;
        }
    }

}
