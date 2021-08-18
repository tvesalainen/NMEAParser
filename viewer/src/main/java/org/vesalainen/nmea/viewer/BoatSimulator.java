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
package org.vesalainen.nmea.viewer;

import org.vesalainen.parsers.nmea.NMEASender0;
import static java.lang.Math.*;
import java.nio.channels.WritableByteChannel;
import java.time.Clock;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.DoubleSupplier;
import static org.vesalainen.math.UnitType.*;
import org.vesalainen.navi.Navis;
import org.vesalainen.nmea.util.Stoppable;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BoatSimulator implements Runnable, Stoppable
{
    private static final double HOUR_IN_MILLIS = TimeUnit.HOURS.toMillis(1);
    private WritableByteChannel channel;
    private final CachedScheduledThreadPool executor;
    private ScheduledFuture<?> future;
    private NMEASender0 sender;
    private Clock clock;
    private long lasttime;
    private double latitude;
    private double longitude;
    private DoubleSupplier boatSpeed;
    private DoubleSupplier boatDirection;
    private DoubleSupplier windSpeed;
    private DoubleSupplier windDirection;
    private DoubleSupplier currentSpeed;
    private DoubleSupplier currentDirection;

    public BoatSimulator(
            WritableByteChannel channel, 
            CachedScheduledThreadPool executor, 
            Clock clock, 
            double latitude, 
            double longitude, 
            DoubleSupplier boatSpeed, 
            DoubleSupplier boatDirection, 
            DoubleSupplier windSpeed, 
            DoubleSupplier windDirection, 
            DoubleSupplier currentSpeed, 
            DoubleSupplier currentDirection
    )
    {
        this.channel = channel;
        this.executor = executor;
        this.sender = new NMEASender0(channel, executor);
        this.clock = clock;
        this.lasttime = clock.millis();
        this.latitude = latitude;
        this.longitude = longitude;
        this.boatSpeed = boatSpeed;
        this.boatDirection = boatDirection;
        this.windSpeed = windSpeed;
        this.windDirection = windDirection;
        this.currentSpeed = currentSpeed;
        this.currentDirection = currentDirection;
        
        sender.begin("clock");
        sender.set("clock", clock);
        sender.commit("clock");
    }
    
    public void simulate()
    {
        sender.begin("simulate");
        double waterSpeed = boatSpeed.getAsDouble();
        double trueHeading = boatDirection.getAsDouble();
        double curSpeed = currentSpeed.getAsDouble();
        double curDir = currentDirection.getAsDouble();
        sender.set("waterSpeed", (float)waterSpeed);
        sender.set("trueHeading", (float)trueHeading);
        long millis = clock.millis();
        long duration = millis - lasttime;
        double durationHours = duration/HOUR_IN_MILLIS;
        double boatDistance = waterSpeed*durationHours;
        double currentDistance = curSpeed*durationHours;
        double dLat1 = Navis.deltaLatitude(boatDistance, trueHeading);
        double dLon1 = Navis.deltaLongitude(latitude, boatDistance, trueHeading);
        double dLat2 = Navis.deltaLatitude(currentDistance, curDir);
        double dLon2 = Navis.deltaLongitude(latitude, currentDistance, curDir);
        double lat = latitude + dLat1 + dLat2;
        double lon = longitude + dLon1 + dLon2;
        double trackMadeGood = Navis.bearing(latitude, longitude, lat, lon);
        sender.set("trackMadeGood", (float)trackMadeGood);
        double distance = Navis.distance(latitude, longitude, lat, lon);
        double speedOverGround = distance/durationHours;
        sender.set("speedOverGround", (float)speedOverGround);
        latitude = lat;
        longitude = lon;
        sender.set("latitude", latitude);
        sender.set("longitude", longitude);
        // wind
        double wSpeed = windSpeed.getAsDouble();
        double wDir = windDirection.getAsDouble();
        double driftAngle = Navis.angleDiff(trueHeading, trackMadeGood);
        double radWDir = toRadians(wDir);
        double radTrackMadeGood = toRadians(trackMadeGood);
        double y = wSpeed*sin(radWDir) + speedOverGround*sin(radTrackMadeGood);
        double x = wSpeed*cos(radWDir) + speedOverGround*cos(radTrackMadeGood);
        double relativeWindSpeed = Math.hypot(x, y);
        sender.set("relativeWindSpeed", (float)relativeWindSpeed);
        double windAngleOverGround = Math.toDegrees(Math.atan2(y, x));
        double relativeWindAngle = Navis.normalizeAngle(windAngleOverGround - trueHeading);
        sender.set("relativeWindAngle", (float)relativeWindAngle);
        
        sender.commit("simulation");
        lasttime = millis;
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
    public void run()
    {
        try
        {
            simulate();
            sender.run();
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

}
