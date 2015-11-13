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
package org.vesalainen.nmea.processor;

import d3.env.TSAGeoMag;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.GatheringByteChannel;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.prefs.Preferences;
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
public class VariationSource extends TimerTask implements PropertySetter, Transactional
{
    private static final String[] Prefixes = new String[]{
        "latitude",
        "longitude",
        "clock"
            };
    private final GatheringByteChannel channel;
    private float longitude;
    private float latitude;
    private long lastUpdate;
    private boolean positionUpdated;
    private final ByteBuffer bb = ByteBuffer.allocateDirect(100);
    private final ByteBufferOutputStream out = new ByteBufferOutputStream(bb);
    private final CheckedOutputStream cout = new CheckedOutputStream(out, new NMEAChecksum());
    private GregorianCalendar calendar;
    private long period = 1000;
    private final Preferences prefs;
    private TSAGeoMag geoMag = new TSAGeoMag();
    private double declination;
    private Timer timer;
    private JavaLogging log = new JavaLogging();

    public VariationSource(GatheringByteChannel channel, VariationSourceType variationSourceType)
    {
        log.setLogger(this.getClass());
        this.channel = channel;
        Long per = variationSourceType.getPeriod();
        if (per != null)
        {
            period = per;
        }
        this.prefs = Preferences.userNodeForPackage(this.getClass());
        this.declination = prefs.getDouble("declination", Double.NaN);
        if (!Double.isNaN(declination))
        {
            log.config("timer started by preference period=%d declination=%f", period, declination);
            timer = new Timer();
            timer.scheduleAtFixedRate(this, 0, period);
        }
    }
    
    @Override
    public String[] getPrefixes()
    {
        return Prefixes;
    }

    @Override
    public void rollback(String reason)
    {
        log.warning("rollback(%s)", reason);
    }

    @Override
    public void commit(String reason)
    {
        long now = System.currentTimeMillis();
        if (positionUpdated && now-lastUpdate > 10000)
        {
            log.fine("location %f %f", latitude, longitude);
            declination = geoMag.getDeclination(latitude, longitude, geoMag.decimalYear(calendar), 0);
            if (timer  == null)
            {
                log.config("timer started by first update period=%d declination=%f", period, declination);
                timer = new Timer();
                timer.scheduleAtFixedRate(this, 0, period);
            }
            lastUpdate = now;
        }
    }
    
    @Override
    public void run()
    {
        bb.clear();
        try
        {
            NMEAGen.rmc(cout, declination);
            bb.flip();
            channel.write(bb);
            log.finest("send RMC declination=%f", declination);
        }
        catch (IOException ex)
        {
            log.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();
        prefs.putDouble("declination", declination);
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
