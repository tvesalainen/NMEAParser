/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import static java.time.ZoneOffset.UTC;
import java.time.ZonedDateTime;
import static java.time.temporal.ChronoField.YEAR;
import static java.time.temporal.ChronoUnit.MILLIS;
import java.util.function.DoubleConsumer;
import org.vesalainen.util.EnumMapList;
import org.vesalainen.util.MapList;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class GeoMagManager
{

    public enum Type
    {
        DECLINATION,
        DIP_ANGLE,
        INTENSITY,
        EAST_INTENSITY,
        NORTH_INTENSITY,
        HORIZONTAL_INTENSITY,
        VERTICAL_INTENSITY
    }
    private TSAGeoMag geoMag = new TSAGeoMag();
    private Clock clock;
    private Year year;
    private double dYear;
    private long nextYear;
    private double lastLat;
    private double lastLon;
    private double lastAlt;
    private MapList<Type,Observer> map = new EnumMapList<>(Type.class);

    public GeoMagManager()
    {
        this(Clock.systemUTC());
    }
    public GeoMagManager(Clock clock)
    {
        this.clock = clock;
        this.year = Year.now(clock);
    }
    /**
     * 
     * @param dlat
     * @param dlong
     * @param altitude In Km
     */
    public void update(double dlat, double dlong, double altitude)
    {
        if (
                updateYear() ||
                Math.abs(dlat-lastLat) > 1 ||
                Math.abs(dlong-lastLon) > 1 ||
                Math.abs(altitude-lastAlt) > 1
                )
        {
            lastLat = dlat;
            lastLon = dlong;
            lastAlt = altitude;
            map.forEach((type,list)->
            {
                double value;
                switch (type)
                {
                    case DECLINATION:
                        value = geoMag.getDeclination(dlat, dlong, dYear, altitude);
                        break;
                    case DIP_ANGLE:
                        value = geoMag.getDipAngle(dlat, dlong, dYear, altitude);
                        break;
                    case INTENSITY:
                        value = geoMag.getIntensity(dlat, dlong, dYear, altitude);
                        break;
                    case EAST_INTENSITY:
                        value = geoMag.getEastIntensity(dlat, dlong, dYear, altitude);
                        break;
                    case NORTH_INTENSITY:
                        value = geoMag.getNorthIntensity(dlat, dlong, dYear, altitude);
                        break;
                    case HORIZONTAL_INTENSITY:
                        value = geoMag.getHorizontalIntensity(dlat, dlong, dYear, altitude);
                        break;
                    case VERTICAL_INTENSITY:
                        value = geoMag.getVerticalIntensity(dlat, dlong, dYear, altitude);
                        break;
                    default:
                        throw new UnsupportedOperationException(type+" not supported");
                }
                for(Observer o : list)
                {
                    o.update(value);
                }
            });
        }
    }
    private boolean updateYear()
    {
        long now = System.currentTimeMillis();
        if (now > nextYear)
        {
            year = Year.now(clock);
            dYear = year.get(YEAR);
            Year ny = year.plusYears(1);
            LocalDate date = ny.atDay(1);
            LocalDateTime time = date.atTime(0, 0);
            ZonedDateTime zoned = time.atZone(UTC);
            long until = clock.instant().until(zoned, MILLIS);
            nextYear = now + until;
            return true;
        }
        else
        {
            return false;
        }
    }
    public Observer addObserver(Type type, double precision, DoubleConsumer observer)
    {
        Observer obs = new Observer(type, precision, observer);
        map.add(type, obs);
        return obs;
    }
    public void removeObservers(Observer... observers)
    {
        for (Observer o : observers)
        {
            removeObserver(o);
        }
    }
    public void removeObserver(Observer observer)
    {
        map.removeItem(observer.type, observer);
    }
    public class Observer
    {
        private Type type;
        private double precision;
        private DoubleConsumer observer;
        private double last = Double.MAX_VALUE;

        private Observer(Type type, double precision, DoubleConsumer observer)
        {
            this.type = type;
            this.precision = precision;
            this.observer = observer;
        }

        
        private void update(double value)
        {
            if (Math.abs(last-value) > precision)
            {
                observer.accept(value);
                last = value;
            }
        }
    }
}
