/*
 * Copyright (C) 2016 tkv
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.vesalainen.code.SimplePropertySetterDispatcher;
import org.vesalainen.nmea.util.NMEASample;
import org.vesalainen.nmea.util.NMEASampler;

/**
 *
 * @author tkv
 */
public class NMEAStream
{
    public static final <I> Stream<NMEASample> parse(I input, String... properties)
    {
        return parse(input, 0, 10, TimeUnit.SECONDS, properties);
    }
    public static final <I> Stream<NMEASample> parse(I input, long offerTimeout, long takeTimeout, TimeUnit timeUnit, String... properties)
    {
        SimplePropertySetterDispatcher dispatcher = new SimplePropertySetterDispatcher();
        NMEADispatcher nmeaDispatcher = NMEADispatcher.getInstance(NMEADispatcher.class, dispatcher);
        NMEASampler sampler = new NMEASampler(nmeaDispatcher, offerTimeout, takeTimeout, timeUnit, (s)->init(input, nmeaDispatcher), properties);
        return sampler.stream();
    }
    private static <I> void init(I input, NMEADispatcher nmeaDispatcher)
    {
        Runner runner = new Runner(input, nmeaDispatcher);
        Thread thread = new Thread(runner, NMEAStream.class.getSimpleName());
        thread.start();
    }
    private static class Runner<I> implements Runnable
    {
        I input;
        NMEADispatcher nmeaDispatcher;

        public Runner(I input, NMEADispatcher nmeaDispatcher)
        {
            this.input = input;
            this.nmeaDispatcher = nmeaDispatcher;
        }
        
        @Override
        public void run()
        {
            try
            {
                NMEAParser parser = NMEAParser.newInstance();
                parser.parse(input, false, nmeaDispatcher, null);
            }
            catch (IOException ex)
            {
                throw new IllegalArgumentException(ex);
            }
        }
        
    }
}
