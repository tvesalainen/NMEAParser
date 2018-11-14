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

import java.time.Clock;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.vesalainen.code.AbstractPropertySetter;
import org.vesalainen.parsers.nmea.MessageType;
import org.vesalainen.parsers.nmea.NMEADispatcher;
import org.vesalainen.parsers.nmea.TalkerId;
import org.vesalainen.util.CollectionHelp;
import org.vesalainen.util.stream.ObserverSpliterator;

/**
 * NMEASampler acts as bridge from observer to stream.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class NMEASampler extends AbstractPropertySetter implements Runnable
{
    private NMEADispatcher dispatcher;
    private ObserverSpliterator<NMEASample> spliterator;
    private Set<String> properties = new HashSet<>();
    private NMEASample sample;
    private Clock clock;
    private Consumer<NMEASampler> initializer;
    /**
     * Creates new NMEASampler and add's it as observer to dispatcher.
     * @param dispatcher
     * @param properties Observed properties. Properties 'clock', 'messageType'
     * and 'talkerId' are always included. (but can be included)
     */
    public NMEASampler(NMEADispatcher dispatcher, String... properties)
    {
        this(dispatcher, 0, Long.MAX_VALUE, TimeUnit.MILLISECONDS, null, properties);
    }
    public NMEASampler(NMEADispatcher dispatcher, long offerTimeout, long takeTimeout, TimeUnit timeUnit, Consumer<NMEASampler> initializer, String... properties)
    {
        this.dispatcher = dispatcher;
        this.initializer = initializer;
        this.properties.addAll(Arrays.asList(properties));
        this.properties.add("clock");
        this.properties.add("messageType");
        this.properties.add("talkerId");
        this.properties.add("origin");
        spliterator = new ObserverSpliterator<>(
            Long.MAX_VALUE, 
            Spliterator.CONCURRENT | Spliterator.NONNULL | Spliterator.ORDERED |Spliterator.SORTED,
            offerTimeout,
            takeTimeout,
            timeUnit,
            null,
            (o)->init()
    );        
    }
    protected void init()
    {
        addProperties(getPrefixes());
        if (initializer != null)
        {
            initializer.accept(this);
            initializer = null;
        }
    }
    /**
     * Add offer properties to running set.
     * @param properties 
     */
    public void addProperties(String... properties)
    {
        this.properties.addAll(CollectionHelp.create(properties));
        dispatcher.addObserver(this, properties);
    }
    /**
     * Removes observers from running set.
     * @param properties 
     */
    public void removeProperties(String... properties)
    {
        dispatcher.removeObserver(this, properties);
        this.properties.removeAll(CollectionHelp.create(properties));
    }
    /**
     * Creates a stream from observer. Behavior of two or more running streams
     * is unpredictable. (will split the streams? not tested)
     * @return 
     */
    public Stream<NMEASample> stream()
    {
        return StreamSupport.stream(spliterator, false)
                .onClose(this);
    }
    
    @Override
    public void start(String reason)
    {
        if (clock != null)
        {
            if (sample == null)
            {
                sample = new NMEASample();
            }
        }
    }

    @Override
    public void commit(String reason)
    {
        if (sample != null && sample.hasProperties())
        {
            sample.setTime(clock.millis());
            spliterator.offer(sample);
            sample = null;
        }
    }

    @Override
    public void rollback(String reason)
    {
        if (sample != null && sample.hasProperties())
        {
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
            case "origin":
                if (sample != null)
                {
                    sample.setOrigin(arg);
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
    public void set(String property, double arg)
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

    @Override
    public void run()
    {
        dispatcher.removeObserver(this, getPrefixes());
    }

}
