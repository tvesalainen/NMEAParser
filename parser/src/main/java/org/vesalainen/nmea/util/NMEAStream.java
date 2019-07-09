/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.vesalainen.code.SimplePropertySetterDispatcher;
import org.vesalainen.parsers.nmea.MessageType;
import org.vesalainen.parsers.nmea.NMEADispatcher;
import org.vesalainen.parsers.nmea.NMEAParser;
import org.vesalainen.parsers.nmea.TalkerId;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class NMEAStream
{
    public static final <I> Stream<NMEASample> parse(I input, String... properties)
    {
        return parse(input, 0, 10, TimeUnit.SECONDS, null, properties);
    }
    public static final <I> Stream<NMEASample> parse(I input, long offerTimeout, long takeTimeout, TimeUnit timeUnit, Supplier origin, String... properties)
    {
        NMEADispatcher nmeaDispatcher = NMEADispatcher.newInstance();
        NMEASampler sampler = new NMEASampler(nmeaDispatcher, offerTimeout, takeTimeout, timeUnit, (s)->init(input, nmeaDispatcher, origin), properties);
        return sampler.stream();
    }
    private static <I> void init(I input, NMEADispatcher nmeaDispatcher, Supplier origin)
    {
        Runner runner = new Runner(input, nmeaDispatcher, origin);
        Thread thread = new Thread(runner, NMEAStream.class.getSimpleName());
        thread.start();
    }
    public static class Builder
    {
        private Stream.Builder<NMEASample> builder = Stream.builder();
        
        public void addWaypoint(long time, double latitude, double longitude)
        {
            NMEASample sample = new NMEASample();
            sample.setMessageType(MessageType.RMC);
            sample.setTalkerId(TalkerId.GP);
            sample.setTime(time);
            sample.setProperty("latitude", (float) latitude);
            sample.setProperty("longitude", (float) longitude);
            builder.add(sample);
        }
        public NMEASample addSample()
        {
            NMEASample sample = new NMEASample();
            builder.add(sample);
            return sample;
        }
        public Stream<NMEASample> build()
        {
            return builder.build();
        }
    }
    private static class Runner<I> implements Runnable
    {
        private I input;
        private NMEADispatcher nmeaDispatcher;
        private Supplier origin;

        public Runner(I input, NMEADispatcher nmeaDispatcher, Supplier origin)
        {
            this.input = input;
            this.nmeaDispatcher = nmeaDispatcher;
            this.origin = origin;
        }
        
        @Override
        public void run()
        {
            try
            {
                NMEAParser parser = NMEAParser.newInstance();
                parser.parse(input, false, origin, nmeaDispatcher, null);
            }
            catch (IOException ex)
            {
                throw new IllegalArgumentException(ex);
            }
        }
        
    }
}
