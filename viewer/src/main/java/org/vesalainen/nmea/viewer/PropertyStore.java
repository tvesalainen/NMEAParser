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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import org.vesalainen.code.AnnotatedPropertyStore;
import org.vesalainen.code.Property;
import org.vesalainen.util.CollectionHelp;

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
    
    private final Map<String,ObservableProperty> listenerMap = new HashMap<>();
    private final NavigableSet<String> updated = new ConcurrentSkipListSet<>();

    public PropertyStore()
    {
        super(MethodHandles.lookup());
        for (String property : getProperties())
        {
            listenerMap.put(property, new ObservableProperty());
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
    private void invalidate()
    {
        Iterator<String> iterator = updated.iterator();
        while (iterator.hasNext())
        {
            String property = iterator.next();
            iterator.remove();
            ObservableProperty observableProperty = listenerMap.get(property);
            observableProperty.invalidate();
        }
    }

    public Observable getObservable(String property)
    {
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
