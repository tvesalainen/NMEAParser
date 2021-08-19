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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import static java.util.logging.Level.SEVERE;
import org.vesalainen.code.AnnotatedPropertyStore;
import org.vesalainen.code.Property;
import org.vesalainen.math.UnitType;
import org.vesalainen.nmea.util.Stoppable;
import org.vesalainen.util.TimeToLiveSet;
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
    private final Set<String> updated;
    private final TSAGeoMag geoMag = new TSAGeoMag();
    
    private final Map<String,Long> scheduleMap = new ConcurrentHashMap<>();
    private final Map<String,ScheduledFuture> futureMap = new ConcurrentHashMap<>();
    private boolean started;
    
    private final Sentence rmc;
    private final Sentence dbt;
    private final Sentence hdt;
    private final Sentence mtw;
    private final Sentence mwv;
    private final Sentence vhw;
    
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
        this.updated = new TimeToLiveSet<>(clock, 5, TimeUnit.SECONDS, this::stale);        
        
        this.rmc = new Sentence(
                NMEASentence.rmc(
                ()->clock, 
                ()->latitude, 
                ()->longitude, 
                ()->speedOverGround, 
                ()->trackMadeGood, 
                this::magneticVariation),
                ()->areFresh("latitude", "longitude", "speedOverGround", "trackMadeGood")
        );
        this.dbt = new Sentence(
                NMEASentence.dbt(()->depthOfWater+transducerOffset, UnitType.METER),
                ()->areFresh("depthOfWater", "transducerOffset")
        );
        this.hdt = new Sentence(
                NMEASentence.hdt(()->trueHeading),
                ()->areFresh("trueHeading")
        );
        this.mtw = new Sentence(
                NMEASentence.mtw(()->waterTemperature, UnitType.CELSIUS),
                ()->areFresh("waterTemperature")
        );
        this.mwv = new Sentence(
                NMEASentence.mwv(()->relativeWindAngle, ()->relativeWindSpeed, UnitType.METERS_PER_SECOND, false),
                ()->areFresh("relativeWindAngle", "relativeWindSpeed")
        );
        this.vhw = new Sentence(
                NMEASentence.vhw(()->waterSpeed, UnitType.KNOT),
                ()->areFresh("waterSpeed")
        );
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
    private boolean areFresh(String... params)
    {
        for (String p : params)
        {
            if (!isFresh(p))
            {
                return false;
            }
        }
        return true;
    }
    private boolean isFresh(String param)
    {
        return updated.contains(param);
    }
    private void stale(String param)
    {
        
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
                    future = executor.scheduleAtFixedRate(rmc, 0, period, TimeUnit.MILLISECONDS);
                    old = futureMap.put(prefix, future);
                    break;
                case "DBT":
                    future = executor.scheduleAtFixedRate(dbt, 0, period, TimeUnit.MILLISECONDS);
                    old = futureMap.put(prefix, future);
                    break;
                case "HDT":
                    future = executor.scheduleAtFixedRate(hdt, 0, period, TimeUnit.MILLISECONDS);
                    old = futureMap.put(prefix, future);
                    break;
                case "MTW":
                    future = executor.scheduleAtFixedRate(mtw, 0, period, TimeUnit.MILLISECONDS);
                    old = futureMap.put(prefix, future);
                    break;
                case "MWV":
                    future = executor.scheduleAtFixedRate(mwv, 0, period, TimeUnit.MILLISECONDS);
                    old = futureMap.put(prefix, future);
                    break;
                case "VHW":
                    future = executor.scheduleAtFixedRate(vhw, 0, period, TimeUnit.MILLISECONDS);
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
    
    private class Sentence implements Runnable
    {
        private NMEASentence sentence;
        private BooleanSupplier predicate;

        public Sentence(NMEASentence sentence, BooleanSupplier predicate)
        {
            this.sentence = sentence;
            this.predicate = predicate;
        }
        
        @Override
        public void run()
        {
            try
            {
                if (predicate.getAsBoolean())
                {
                    sentence.writeTo(channel);
                }
            }
            catch (IOException ex)
            {
                log(SEVERE, ex, "NMEASender %s", ex.getMessage());
            }
        }
        
    }
}
