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

import java.lang.invoke.MethodHandles;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import static java.util.concurrent.TimeUnit.SECONDS;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Binding;
import javafx.beans.binding.FloatBinding;
import javafx.beans.binding.LongBinding;
import javafx.scene.Node;
import org.vesalainen.code.AnnotatedPropertyStore;
import org.vesalainen.code.Property;
import org.vesalainen.util.CollectionHelp;
import org.vesalainen.util.TimeToLiveSet;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PropertyStore extends AnnotatedPropertyStore
{
    private @Property double latitude;
    private @Property double longitude;
    private @Property float depthOfWater;
    private @Property float waterSpeed;
    private @Property float waterTemperature;
    
    private final CachedScheduledThreadPool executor;
    private final FloatBinding keelOffsetBinding;
    private final FloatBinding transducerOffsetBinding;
    private final Map<String,ObservableProperty> listenerMap = new HashMap<>();
    private final NavigableSet<String> updated = new ConcurrentSkipListSet<>();
    private final TimeToLiveSet<String> actives;
    private final LongBinding timeToLiveBinding;
    private final Map<String,Node> nodes = new ConcurrentHashMap<>();

    public PropertyStore(CachedScheduledThreadPool executor, ViewerPreferences preferences)
    {
        super(MethodHandles.lookup());
        this.executor = executor;
        this.keelOffsetBinding = (FloatBinding) preferences.getNumberBinding("keelOffset");
        this.transducerOffsetBinding = (FloatBinding) preferences.getNumberBinding("transducerOffset");
        this.timeToLiveBinding = (LongBinding)preferences.getNumberBinding("timeToLive");
        this.actives = new TimeToLiveSet<>(Clock.systemUTC(), preferences.getLong("timeToLive"), SECONDS, this::setDisable);
        for (String property : getProperties())
        {
            listenerMap.put(property, new ObservableProperty());
            actives.add(property);
        }
        checkDisabled();
    }
    
    @Property public float getDepthBelowKeel()
    {
        return depthOfWater - keelOffsetBinding.get();
    }
    @Property public float getDepthBelowSurface()
    {
        return depthOfWater;
    }
    @Property public float getDepthBelowTransducer()
    {
        return depthOfWater - transducerOffsetBinding.get();
    }
    @Property public void setDepthBelowKeel(float meters)
    {
        depthOfWater = meters + keelOffsetBinding.get();
        updated.add("depthOfWater");
    }
    @Property public void setDepthBelowSurface(float meters)
    {
        depthOfWater = meters;
        updated.add("depthOfWater");
    }
    @Property public void setDepthBelowTransducer(float meters)
    {
        depthOfWater = meters + transducerOffsetBinding.get();
        updated.add("depthOfWater");
    }
    private void checkDisabled()
    {
        Platform.runLater(()->actives.size());
        executor.schedule(this::checkDisabled, timeToLiveBinding.get(), SECONDS);
    }
    /**
     * this is run in platform thread
     * @param property 
     */
    private void setDisable(String property)
    {
        Node node = nodes.get(property);
        if (node != null)
        {
            node.setDisable(true);
        }
    }
    @Override
    public void commit(String reason, Collection<String> updatedProperties)
    {
        invalidate(updatedProperties);
    }
    
    private void invalidate(String... updatedProperties)
    {
        invalidate(CollectionHelp.create(updatedProperties));
    }
    private void invalidate(Collection<String> updatedProperties)
    {
        updated.addAll(updatedProperties);
        Platform.runLater(this::invalidate);
    }
    /**
     * this is run in platform thread
     */
    private void invalidate()
    {
        long ttl = timeToLiveBinding.get();
        Iterator<String> iterator = updated.iterator();
        while (iterator.hasNext())
        {
            String property = iterator.next();
            iterator.remove();
            Node node = nodes.get(property);
            if (node != null)
            {
                node.setDisable(false);
            }
            actives.add(property, ttl, SECONDS);
            ObservableProperty observableProperty = listenerMap.get(property);
            observableProperty.invalidate();
        }
    }

    public Observable registerNode(String property, Node node)
    {
        nodes.put(property, node);
        node.setDisable(true);
        return listenerMap.get(property);
    }
    private class ObservableProperty implements Observable
    {
        private List<InvalidationListener> listeners = new ArrayList<>();
        
        public void invalidate()
        {
            listeners.forEach((l)->l.invalidated(this));
        }
        @Override
        public void addListener(InvalidationListener listener)
        {
            listeners.add(listener);
        }

        @Override
        public void removeListener(InvalidationListener listener)
        {
            listeners.remove(listener);
        }
        
    }
}
