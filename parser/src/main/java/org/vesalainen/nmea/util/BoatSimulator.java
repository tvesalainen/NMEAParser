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
package org.vesalainen.nmea.util;

import static java.lang.Math.*;
import java.time.Clock;
import static java.util.concurrent.TimeUnit.*;
import java.util.function.DoubleSupplier;
import org.vesalainen.code.AnnotatedPropertyStore;
import org.vesalainen.code.PropertySetter;
import static org.vesalainen.math.UnitType.*;
import org.vesalainen.navi.Navis;
import static org.vesalainen.parsers.nmea.NMEAProperty.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BoatSimulator
{
    private AnnotatedPropertyStore report;
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
    
    public void simulate()
    {
        report.begin("simulate");
        double waterSpeed = boatSpeed.getAsDouble();
        double trueHeading = boatDirection.getAsDouble();
        double curSpeed = currentSpeed.getAsDouble();
        double curDir = currentDirection.getAsDouble();
        report.set("waterSpeed", (float)waterSpeed);
        report.set("trueHeading", (float)trueHeading);
        long millis = clock.millis();
        long duration = millis - lasttime;
        long durationHours = MILLISECONDS.toHours(duration);
        double boatDistance = waterSpeed*durationHours;
        double currentDistance = curSpeed*durationHours;
        double dLat1 = Navis.deltaLatitude(boatDistance, trueHeading);
        double dLon1 = Navis.deltaLongitude(latitude, boatDistance, trueHeading);
        double dLat2 = Navis.deltaLatitude(currentDistance, curDir);
        double dLon2 = Navis.deltaLongitude(latitude, currentDistance, curDir);
        double lat = latitude + dLat1 + dLat2;
        double lon = longitude + dLon1 + dLon2;
        double trackMadeGood = Navis.bearing(latitude, longitude, lat, lon);
        report.set("trackMadeGood", (float)trackMadeGood);
        double distance = Navis.distance(latitude, longitude, lat, lon);
        double speedOverGround = distance/durationHours;
        report.set("speedOverGround", (float)speedOverGround);
        latitude = lat;
        longitude = lon;
        report.set("latitude", latitude);
        report.set("longitude", longitude);
        // wind
        double wSpeed = windSpeed.getAsDouble();
        double wDir = windDirection.getAsDouble();
        double driftAngle = Navis.angleDiff(trueHeading, trackMadeGood);
        double radWDir = toRadians(wDir);
        double radTrackMadeGood = toRadians(trackMadeGood);
        double x = wSpeed*cos(radWDir) - speedOverGround*cos(radTrackMadeGood);
        double y = wSpeed*sin(radWDir) - speedOverGround*sin(radTrackMadeGood);
        double relativeWindSpeed = Math.hypot(x, y);
        report.set("relativeWindSpeed", (float)KNOT.convertTo(relativeWindSpeed, METER));
        double windAngleOverGround = Math.toDegrees(Math.atan2(y, x));
        double relativeWindAngle = Navis.normalizeAngle(windAngleOverGround - driftAngle);
        report.set("relativeWindAngle", (float)relativeWindAngle);
        
        report.commit("simulation");
        lasttime = millis;
    }
}
