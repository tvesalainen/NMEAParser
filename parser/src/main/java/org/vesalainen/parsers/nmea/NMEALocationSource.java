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
package org.vesalainen.parsers.nmea;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.Clock;
import org.vesalainen.code.AnnotatedPropertyStore;
import org.vesalainen.code.Property;
import org.vesalainen.navi.LocationSource;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class NMEALocationSource extends LocationSource
{
    private NMEAService nmeaService;
    private String host;
    private int port;
    private NMEASource source;
    private long period;
    private long timestamp;
    private long next;

    public NMEALocationSource(String host, int port)
    {
        this(host, port, 1000);
    }
    public NMEALocationSource(String host, int port, long period)
    {
        if (period <= 0)
        {
            throw new IllegalArgumentException("illegal period");
        }
        this.host = host;
        this.port = port;
        this.period = period;
    }
    
    @Override
    protected void start()
    {
        try
        {
            if (nmeaService != null)
            {
                throw new IllegalStateException("started already");
            }
            nmeaService = new NMEAService(host, port);
            nmeaService.start();
            source = new NMEASource();
            nmeaService.addNMEAObserver(source);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    protected void stop()
    {
        if (nmeaService == null)
        {
            throw new IllegalStateException("not started");
        }
        nmeaService.removeNMEAObserver(source);
        nmeaService.stop();
        nmeaService = null;
        source = null;
    }
    
    private class NMEASource extends AnnotatedPropertyStore
    {
        @Property private Clock clock;
        @Property private double latitude;
        @Property private double longitude;
        @Property private float speedOverGround;
        @Property private float horizontalDilutionOfPrecision = 3;

        public NMEASource()
        {
            super(MethodHandles.lookup());
        }

        @Override
        public void commit(String reason)
        {
            timestamp = clock.millis();
            if (timestamp >= next)
            {
                NMEALocationSource.this.update(longitude, latitude, timestamp, horizontalDilutionOfPrecision, speedOverGround);
                next = timestamp + period;
            }
        }
        
    }
}
