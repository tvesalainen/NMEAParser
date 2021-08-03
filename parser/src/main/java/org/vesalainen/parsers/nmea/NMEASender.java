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
package org.vesalainen.parsers.nmea;

import d3.env.TSAGeoMag;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.channels.WritableByteChannel;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import static java.util.logging.Level.SEVERE;
import org.vesalainen.code.AnnotatedPropertyStore;
import org.vesalainen.code.Property;
import org.vesalainen.math.UnitType;
import org.vesalainen.nmea.util.Stoppable;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class NMEASender extends AnnotatedPropertyStore implements Stoppable
{
    private @Property Clock clock;
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
    private Set<String> updated = new HashSet<>();
    private final TSAGeoMag geoMag = new TSAGeoMag();
    
    private final Map<String,Long> scheduleMap = new ConcurrentHashMap<>();
    private final Map<String,ScheduledFuture> futureMap = new ConcurrentHashMap<>();
    private boolean started;
    
    private final NMEASentence rmc;
    private final NMEASentence dbt;
    private final NMEASentence hdt;
    private final NMEASentence mtw;
    private final NMEASentence mwv;
    private final NMEASentence vhw;
    
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
        
        this.rmc = NMEASentence.rmc(
                ()->clock, 
                ()->latitude, 
                ()->longitude, 
                ()->speedOverGround, 
                ()->trackMadeGood, 
                this::magneticVariation)
                ;
        this.dbt = NMEASentence.dbt(()->depthOfWater+transducerOffset, UnitType.METER);
        this.hdt = NMEASentence.hdt(()->trueHeading);
        this.mtw = NMEASentence.mtw(()->waterTemperature, UnitType.CELSIUS);
        this.mwv = NMEASentence.mwv(()->relativeWindAngle, ()->relativeWindSpeed, UnitType.METERS_PER_SECOND, false);
        this.vhw = NMEASentence.vhw(()->waterSpeed, UnitType.KNOT);
    }

    private void run(NMEASentence sentence)
    {
        try
        {
            sentence.writeTo(channel);
        }
        catch (IOException ex)
        {
            log(SEVERE, ex, "NMEASender %s", ex.getMessage());
        }
    }

    public void schedule(String prefix, long period, TimeUnit unit)
    {
        switch (prefix)
        {
            case "RMC":
            case "DBT":
            case "HDT":
            case "MTW":
            case "MWV":
            case "VHW":
                scheduleMap.put(prefix, TimeUnit.MILLISECONDS.convert(period, unit));
                break;
            default:
                throw new UnsupportedOperationException(prefix+" not supported");

        }
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
        scheduleMap.forEach((prefix, period)->
        {
            ScheduledFuture<?> future;
            ScheduledFuture<?> old = null;
            switch (prefix)
            {
                case "RMC":
                    future = executor.scheduleAtFixedRate(()->run(rmc), 0, period, TimeUnit.MILLISECONDS);
                    old = futureMap.put(prefix, future);
                    break;
                case "DBT":
                    future = executor.scheduleAtFixedRate(()->run(dbt), 0, period, TimeUnit.MILLISECONDS);
                    old = futureMap.put(prefix, future);
                    break;
                case "HDT":
                    future = executor.scheduleAtFixedRate(()->run(hdt), 0, period, TimeUnit.MILLISECONDS);
                    old = futureMap.put(prefix, future);
                    break;
                case "MTW":
                    future = executor.scheduleAtFixedRate(()->run(mtw), 0, period, TimeUnit.MILLISECONDS);
                    old = futureMap.put(prefix, future);
                    break;
                case "MWV":
                    future = executor.scheduleAtFixedRate(()->run(mwv), 0, period, TimeUnit.MILLISECONDS);
                    old = futureMap.put(prefix, future);
                    break;
                case "VHW":
                    future = executor.scheduleAtFixedRate(()->run(vhw), 0, period, TimeUnit.MILLISECONDS);
                    old = futureMap.put(prefix, future);
                    break;
                default:
                    throw new UnsupportedOperationException(prefix+" not supported");

            }
            if (old != null)
            {
                old.cancel(true);
            }
        });
    }
    @Override
    public void stop()
    {
        if (!started)
        {
            throw new IllegalStateException();
        }
        futureMap.values().forEach((f)->f.cancel(true));
    }

    @Override
    public void commit(String reason, Collection<String> updatedProperties)
    {
        updated.addAll(updatedProperties);
    }
    
    
}
