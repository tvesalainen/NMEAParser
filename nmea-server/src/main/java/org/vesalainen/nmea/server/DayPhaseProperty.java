/*
 * Copyright (C) 2022 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nmea.server;

import java.io.IOException;
import java.time.Clock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vesalainen.navi.SolarWatch;
import org.vesalainen.navi.SolarWatch.DayPhase;
import org.vesalainen.nmea.server.jaxb.PropertyType;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;
import org.vesalainen.util.logging.AttachedLogger;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DayPhaseProperty extends ObjectProperty implements AttachedLogger
{
    private Clock clock;
    private SolarWatch solarWatch;
    private double longitude = Double.NaN;
    private double latitude = Double.NaN;
    
    public DayPhaseProperty(CachedScheduledThreadPool executor, PropertyType property)
    {
        super(executor, property, "clock", "latitude", "longitude");
    }

    private void setDayPhase(DayPhase phase)
    {
        super.set("dayPhase", clock.millis(), phase);
        info("new day phase %s", phase);
    }
    
    @Override
    protected void advertise(Observer observer)
    {
        if (solarWatch != null)
        {
            try
            {
                observer.accept(clock.millis(), solarWatch.getPhase());
            }
            catch (IOException ex)
            {
                Logger.getLogger(Property.class.getName()).log(Level.WARNING, "advertise %s", ex.getMessage());
            }
        }
    }
    
    @Override
    public <T> void set(String property, long time, T arg)
    {
        this.clock = (Clock) arg;
    }

    @Override
    public <T> void set(String property, long time, double value)
    {
        switch (property)
        {
            case "latitude":
                this.latitude = value;
                break;
            case "longitude":
                this.longitude = value;
                break;
            default:
                super.set(property, time, value);
                break;
        }
        /*
        if (solarWatch == null && !Double.isNaN(latitude) && !Double.isNaN(longitude))
        {
            solarWatch = new SolarWatch(clock, executor, ()->latitude, ()->longitude, 12);
            solarWatch.addObserver(this::setDayPhase);
            solarWatch.start();
        }
        */
    }
    
}
