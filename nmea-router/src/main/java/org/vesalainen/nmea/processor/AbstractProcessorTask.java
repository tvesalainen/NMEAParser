/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import static java.util.concurrent.TimeUnit.SECONDS;
import org.vesalainen.code.AnnotatedPropertyStore;
import org.vesalainen.nmea.util.Stoppable;
import org.vesalainen.util.CollectionHelp;
import org.vesalainen.util.TimeToLiveSet;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AbstractProcessorTask extends AnnotatedPropertyStore implements Stoppable
{
    private Set<String> neededProperties = new HashSet<>();
    private Set<String> currentProperties;
    
    protected AbstractProcessorTask(MethodHandles.Lookup lookup, String... neededProperties)
    {
        this(lookup, 2, SECONDS, neededProperties);
    }
    protected AbstractProcessorTask(MethodHandles.Lookup lookup, long timeout, TimeUnit unit, String... neededProperties)
    {
        super(lookup);
        this.currentProperties = new TimeToLiveSet<>(timeout, unit);
        if (neededProperties.length > 0)
        {
            CollectionHelp.addAll(this.neededProperties, neededProperties);
        }
        else
        {
            CollectionHelp.addAll(this.neededProperties, getProperties());
        }
        this.neededProperties.remove("clock");  // clock has special treatment
    }

    @Override
    public final void commit(String reason, Collection<String> updatedProperties)
    {
        currentProperties.addAll(updatedProperties);
        if (currentProperties.containsAll(neededProperties))
        {
            commitTask(reason, updatedProperties);
        }
        else
        {
            Set<String> set = new HashSet<>(neededProperties);
            set.removeAll(currentProperties);
            warning(getClass().getSimpleName()+" task doesn't have all needed properties "+set);
        }
    }

    protected abstract void commitTask(String reason, Collection<String> updatedProperties);
    
}
