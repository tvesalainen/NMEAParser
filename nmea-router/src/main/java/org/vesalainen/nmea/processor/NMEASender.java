/*
 * Copyright (C) 2020 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.lang.invoke.MethodHandles;
import java.nio.channels.WritableByteChannel;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import static java.util.logging.Level.SEVERE;
import org.vesalainen.code.AnnotatedPropertyStore;
import org.vesalainen.code.Property;
import org.vesalainen.math.UnitType;
import org.vesalainen.nmea.util.Stoppable;
import org.vesalainen.parsers.nmea.NMEASentence;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class NMEASender extends AnnotatedPropertyStore implements Stoppable
{
    private @Property Clock clock;
    private @Property long millis;
    private @Property int pgn;
    private @Property double latitude;
    private @Property double longitude;
    private @Property float depthOfWater;
    private @Property float transducerOffset;
    private @Property float waterTemperature;
    private @Property float waterSpeed;
    private @Property float trueHeading;
    private @Property float relativeWindAngle;
    private @Property float relativeWindSpeed;
    private @Property float pitch;
    private @Property float roll;
    private @Property float speedOverGround;
    private @Property float trackMadeGood;
    
    private final WritableByteChannel channel;
    private final CachedScheduledThreadPool executor;
    private final TSAGeoMag geoMag = new TSAGeoMag();
    
    private final Map<String,NMEASentence> prefixMap = new HashMap<>();
    private final Map<Integer,NMEASentence> pgnMap = new HashMap<>();
    
    private boolean started;
    
    public NMEASender(WritableByteChannel channel)
    {
        this(Clock.systemUTC(), channel, new CachedScheduledThreadPool());
    }
    public NMEASender(WritableByteChannel channel, CachedScheduledThreadPool executor)
    {
        this(Clock.systemUTC(), channel, executor);
    }
    public NMEASender(Clock clock, WritableByteChannel channel, CachedScheduledThreadPool executor)
    {
        super(MethodHandles.lookup());
        this.clock = clock;
        this.channel = channel;
        this.executor = executor;
        
        prefixMap.put("RMC", NMEASentence.rmc(
                ()->clock, 
                ()->latitude, 
                ()->longitude, 
                ()->speedOverGround, 
                ()->trackMadeGood, 
                this::magneticVariation));
        prefixMap.put("DBT", NMEASentence.dbt(()->depthOfWater+transducerOffset, UnitType.METER));
        prefixMap.put("HDT", NMEASentence.hdt(()->trueHeading));
        prefixMap.put("MTW", NMEASentence.mtw(()->waterTemperature, UnitType.CELSIUS));
        prefixMap.put("MWV", NMEASentence.mwv(()->relativeWindAngle, ()->relativeWindSpeed, UnitType.METERS_PER_SECOND, false));
        prefixMap.put("VHW", NMEASentence.vhw(()->waterSpeed, UnitType.METERS_PER_SECOND));
    }

    public void add(String prefix, int pgn)
    {
        NMEASentence sentence = prefixMap.get(prefix);
        if (sentence == null)
        {
            throw new UnsupportedOperationException(prefix+" not supported");
        }
        pgnMap.put(pgn, sentence);
    }
    public double magneticVariation()
    {
        ZonedDateTime now = ZonedDateTime.now(clock);
        return geoMag.getDeclination(latitude, longitude, (double)(now.getYear()+now.getDayOfYear()/365.0), 0);
    }
    public void start()
    {
        if (started)
        {
            throw new IllegalStateException();
        }
    }
    @Override
    public void stop()
    {
        if (!started)
        {
            throw new IllegalStateException();
        }
    }

    @Override
    public void commit(String reason)
    {
        NMEASentence sentence = pgnMap.get(pgn);
        if (sentence != null)
        {
            try
            {
                sentence.writeTo(channel);
            }
            catch (IOException ex)
            {
                log(SEVERE, ex, "commit");
            }
        }
    }
    
}
