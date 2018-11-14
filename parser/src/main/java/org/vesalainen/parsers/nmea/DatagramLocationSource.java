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
package org.vesalainen.parsers.nmea;

import java.time.Clock;
import java.util.GregorianCalendar;
import org.vesalainen.code.AbstractPropertySetter;
import org.vesalainen.navi.LocationSource;
import org.vesalainen.util.Transactional;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DatagramLocationSource extends LocationSource
{
    private static final String[] Prefixes = new String[]{
        "horizontalDilutionOfPrecision",
        "latitude",
        "longitude",
        "clock"
            };
    private Observer observer;

    @Override
    protected void start() throws Exception
    {
        DatagramNMEAService service = DatagramNMEAService.getInstance();
        observer = new Observer();
        service.addNMEAObserver(observer);
        if (!service.isStarted())
        {
            service.start();
        }
    }

    @Override
    protected void stop() throws Exception
    {
        DatagramNMEAService service = DatagramNMEAService.getInstance();
        service.removeNMEAObserver(observer);
        if (!service.hasObservers() && service.isStarted())
        {
            service.stop();
        }
        observer = null;
    }
    
    private class Observer extends AbstractPropertySetter implements Transactional, AutoCloseable
    {
        private double latitude;
        private double longitude;
        private float horizontalDilutionOfPrecision = Float.NaN;
        private boolean positionUpdated;
        private Clock clock;

        @Override
        public void commit(String reason)
        {
            if (positionUpdated)
            {
                update(longitude, latitude, clock.millis(), horizontalDilutionOfPrecision);
            }
        }

        @Override
        public String[] getPrefixes()
        {
            return Prefixes;
        }

        @Override
        public void close() throws Exception
        {
        }
        
        @Override
        public void set(String property, double arg)
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
        public void set(String property, float arg)
        {
            switch (property)
            {
                case "horizontalDilutionOfPrecision":
                    horizontalDilutionOfPrecision = arg;
                    break;
            }
        }

        @Override
        public void set(String property, Object arg)
        {
            switch (property)
            {
                case "clock":
                    clock = (Clock) arg;
                    break;
            }
        }

        @Override
        protected void setProperty(String property, Object arg)
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }
}
