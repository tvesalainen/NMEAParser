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
package org.vesalainen.nmea.processor;

import org.vesalainen.nmea.util.AbstractSampleConsumer;
import d3.env.TSAGeoMag;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.GatheringByteChannel;
import java.time.ZoneOffset;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Stream;
import java.util.zip.CheckedOutputStream;
import org.vesalainen.nio.channels.ByteBufferOutputStream;
import org.vesalainen.nmea.jaxb.router.VariationSourceType;
import org.vesalainen.nmea.util.NMEAFilters;
import org.vesalainen.nmea.util.NMEASample;
import org.vesalainen.parsers.nmea.NMEAChecksum;
import org.vesalainen.parsers.nmea.NMEAGen;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class VariationSource extends AbstractSampleConsumer
{
    private static final String[] Prefixes = new String[]{
        "latitude",
        "longitude",
        "clock"
            };
    private final GatheringByteChannel channel;
    private final ByteBuffer bb = ByteBuffer.allocateDirect(100);
    private final ByteBufferOutputStream out = new ByteBufferOutputStream(bb);
    private final CheckedOutputStream cout = new CheckedOutputStream(out, new NMEAChecksum());
    private long period = 1000;
    private TSAGeoMag geoMag = new TSAGeoMag();
    private GregorianCalendar calendar;
    private long nextCalendarUpdate;

    public VariationSource(GatheringByteChannel channel, VariationSourceType variationSourceType)
    {
        super(VariationSource.class);
        this.channel = channel;
        Long per = variationSourceType.getPeriod();
        if (per != null)
        {
            period = per;
        }
    }
    
    @Override
    public String[] getProperties()
    {
        return Prefixes;
    }

    @Override
    public void init(Stream<NMEASample> stream)
    {
        this.stream = stream.filter(NMEAFilters.periodicFilter(period, TimeUnit.MILLISECONDS));
    }

    private static final long DayInMillis = 24*60*60000;
    @Override
    protected void process(NMEASample sample)
    {
        bb.clear();
        try
        {
            if (calendar == null || nextCalendarUpdate < sample.getTime())
            {
                long time = sample.getTime();
                calendar = new GregorianCalendar(TimeZone.getTimeZone(ZoneOffset.UTC));
                calendar.setTimeInMillis(time);
                nextCalendarUpdate = time+DayInMillis;
            }
            fine("location %s", sample);
            double declination = geoMag.getDeclination(sample.getLatitude(), sample.getLongitude(), geoMag.decimalYear(calendar), 0);
            NMEAGen.rmc(cout, declination);
            bb.flip();
            channel.write(bb);
            finest("send RMC declination=%f", declination);
        }
        catch (IOException ex)
        {
            log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

}
