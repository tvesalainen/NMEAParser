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
package org.vesalainen.nmea.util;

import java.time.Clock;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import org.vesalainen.code.AbstractPropertySetter;
import org.vesalainen.parsers.nmea.MessageType;
import org.vesalainen.parsers.nmea.NMEADispatcher;
import org.vesalainen.parsers.nmea.TalkerId;
import org.vesalainen.util.Recycler;
import org.vesalainen.util.stream.Generator;

/**
 *
 * @author tkv
 */
public class NMEASampler extends AbstractPropertySetter
{
    private NMEADispatcher dispatcher;
    private Generator<NMEASample> generator = new Generator<>();
    private Set<String> properties = new HashSet<>();
    private NMEASample sample;
    private Clock clock;

    public NMEASampler(NMEADispatcher dispatcher, String... properties)
    {
        this.dispatcher = dispatcher;
        this.properties.addAll(Arrays.asList(properties));
        this.properties.add("clock");
        this.properties.add("messageType");
        this.properties.add("talkerId");
        addProperties(getPrefixes());
    }

    public final void addProperties(String... properties)
    {
        dispatcher.addObserver(this, properties);
    }
    
    public void removeProperties(String... properties)
    {
        dispatcher.removeObserver(this, properties);
    }
    
    public Stream<NMEASample> stream()
    {
        return Stream.generate(()->generator.generate());
    }
    
    @Override
    public void start(String reason)
    {
        if (clock != null)
        {
            if (sample == null)
            {
                sample = Recycler.get(NMEASample.class);
            }
            sample.setTime(clock.millis());
        }
    }

    @Override
    public void commit(String reason)
    {
        if (sample != null && sample.hasProperties())
        {
            generator.provide(sample);
            sample = null;
        }
    }

    @Override
    public void rollback(String reason)
    {
        if (sample != null && sample.hasProperties())
        {
            Recycler.recycle(sample);
            sample = null;
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
            case "talkerId":
                if (sample != null)
                {
                    sample.setTalkerId((TalkerId) arg);
                }
                break;
            case "messageType":
                if (sample != null)
                {
                    sample.setMessageType((MessageType) arg);
                }
                break;
        }
    }

    @Override
    public void set(String property, float arg)
    {
        if (sample != null)
        {
            sample.setProperty(property, arg);
        }
    }

    @Override
    public final String[] getPrefixes()
    {
        return properties.toArray(new String[properties.size()]);
    }

}
