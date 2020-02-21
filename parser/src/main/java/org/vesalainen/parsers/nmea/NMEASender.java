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

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.channels.WritableByteChannel;
import java.time.Clock;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import static java.util.logging.Level.SEVERE;
import org.vesalainen.code.AnnotatedPropertyStore;
import org.vesalainen.code.Property;
import org.vesalainen.math.UnitType;
import org.vesalainen.navi.Navis;
import org.vesalainen.nmea.util.Stoppable;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class NMEASender extends AnnotatedPropertyStore implements Runnable, Stoppable
{
    private @Property Clock clock;
    private @Property float magneticVariation;
    private @Property float depthOfWater;
    private @Property float waterTemperature;
    private @Property float waterSpeed;
    private @Property float trueHeading;
    private @Property float windDirection;
    private @Property float windSpeed;
    private @Property float pitch;
    private @Property float roll;
    
    private final WritableByteChannel channel;
    private final CachedScheduledThreadPool executor;
    private ScheduledFuture<?> future;
    private Set<String> updated = new HashSet<>();
    
    private double latitude;
    private double longitude;
    private double lastLatitude;
    private double lastLongitude;
    private long coordinateUpdate;
    private long lastCoordinateUpdate;
    
    private final NMEASentence rmc;
    private final NMEASentence dbt;
    private final NMEASentence hdt;
    private final NMEASentence mtw;
    
    public NMEASender(WritableByteChannel channel)
    {
        this(channel, new CachedScheduledThreadPool());
    }
    public NMEASender(WritableByteChannel channel, CachedScheduledThreadPool executor)
    {
        super(MethodHandles.lookup());
        this.channel = channel;
        this.executor = executor;
        
        this.rmc = NMEASentence.rmc(
                ()->clock, 
                ()->latitude, 
                ()->longitude, 
                this::speedOverGround, 
                this::trackMadeGood, 
                ()->magneticVariation);
        this.dbt = NMEASentence.dbt(()->depthOfWater, UnitType.METER);
        this.hdt = NMEASentence.hdt(()->trueHeading);
        this.mtw = NMEASentence.mtw(()->waterTemperature, UnitType.CELSIUS);
    }

    @Property public void setLatitude(double latitude)
    {
        lastLatitude = this.latitude;
        this.latitude = latitude;
        lastCoordinateUpdate = coordinateUpdate;
        coordinateUpdate = clock.millis();
    }

    @Property public void setLongitude(double longitude)
    {
        lastLongitude = this.longitude;
        this.longitude = longitude;
    }

    private double speedOverGround()
    {
        if (coordinateUpdate > lastCoordinateUpdate)
        {
            return Navis.speed(lastCoordinateUpdate, lastLatitude, lastLongitude, coordinateUpdate, latitude, longitude);
        }
        else
        {
            return 0;
        }
    }
    private double trackMadeGood()
    {
        return Navis.bearing(lastLatitude, lastLongitude, latitude, longitude);
    }
    public void start()
    {
        if (future != null)
        {
            throw new IllegalStateException();
        }
        future = executor.scheduleAtFixedRate(this, 0, 1, TimeUnit.SECONDS);
    }
    @Override
    public void stop()
    {
        if (future == null)
        {
            throw new IllegalStateException();
        }
        future.cancel(true);
        future = null;
    }

    @Override
    public void commit(String reason, Collection<String> updatedProperties)
    {
        updated.addAll(updatedProperties);
    }
    
    @Override
    public void run()
    {
        try
        {
            rmc.writeTo(channel);
            dbt.writeTo(channel);
            hdt.writeTo(channel);
            mtw.writeTo(channel);
            updated.clear();
        }
        catch (IOException ex)
        {
            log(SEVERE, ex, "NMEASender %s", ex.getMessage());
        }
    }

    
}
