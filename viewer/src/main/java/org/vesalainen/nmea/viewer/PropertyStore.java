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
import java.util.concurrent.ConcurrentSkipListSet;
import static java.util.concurrent.TimeUnit.*;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.FloatBinding;
import javafx.beans.binding.LongBinding;
import javafx.beans.binding.NumberBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.scene.Node;
import org.vesalainen.code.AnnotatedPropertyStore;
import org.vesalainen.code.Property;
import org.vesalainen.math.UnitType;
import org.vesalainen.navi.SolarWatch;
import org.vesalainen.navi.SolarWatch.DayPhase;
import org.vesalainen.parsers.nmea.NMEAProperties;
import org.vesalainen.util.CollectionHelp;
import org.vesalainen.util.ConcurrentHashMapList;
import org.vesalainen.util.MapList;
import org.vesalainen.util.TimeToLiveSet;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PropertyStore extends AnnotatedPropertyStore
{
    private @Property long epochMillis;
    private @Property double latitude;
    private @Property double longitude;
    private @Property float depthOfWater;
    private @Property float waterSpeed;
    private @Property float waterTemperature;
    private @Property float trueHeading;
    private @Property float speedOverGround;
    private @Property float trackMadeGood;
    private @Property float magneticVariation;
    private @Property float relativeWindAngle;
    private @Property float relativeWindSpeed;
    
    private final CachedScheduledThreadPool executor;
    private final FloatBinding keelOffsetBinding;
    private final FloatBinding transducerOffsetBinding;
    private final Map<String,NumberBinding> boundMap = new HashMap<>();
    private final NavigableSet<String> updated = new ConcurrentSkipListSet<>();
    private final TimeToLiveSet<String> actives;
    private final LongBinding timeToLiveBinding;
    private final MapList<String,Node> nodes = new ConcurrentHashMapList<>();
    private final DoubleBinding solarDepressionAngleBinding;
    private SolarWatch solarWatch;
    private ObjectBinding<DayPhase> dayPhaseProperty;

    public PropertyStore(CachedScheduledThreadPool executor, ViewerPreferences preferences)
    {
        super(MethodHandles.lookup());
        this.executor = executor;
        this.keelOffsetBinding = (FloatBinding) preferences.getNumberBinding("keelOffset");
        this.transducerOffsetBinding = (FloatBinding) preferences.getNumberBinding("transducerOffset");
        this.timeToLiveBinding = (LongBinding)preferences.getNumberBinding("timeToLive");
        this.actives = new TimeToLiveSet<>(Clock.systemUTC(), preferences.getLong("timeToLive"), SECONDS, this::setDisable);
        this.solarDepressionAngleBinding = (DoubleBinding) preferences.getNumberBinding("solarDepressionAngle");
        this.solarWatch = new SolarWatch(()->epochMillis, executor, ()->preferences.getLong("solarUpdateSeconds"), ()->latitude, ()->longitude, ()->solarDepressionAngleBinding.doubleValue());
        this.dayPhaseProperty = Bindings.createObjectBinding(()->solarWatch.getPhase());
        solarWatch.addObserver((p)->Platform.runLater(dayPhaseProperty::invalidate));
        for (String property : getProperties())
        {
            boundMap.put(property, createBinding(property));
            actives.add(property);
        }
        checkDisabled();
    }
    private NumberBinding createBinding(String property)
    {
        Class<?> type = getType(property);
        if (type == null)
        {
            throw new IllegalArgumentException(type+" not a property");
        }
        switch (type.getSimpleName())
        {
            case "int":
                return Bindings.createIntegerBinding(()->getInt(property));
            case "long":
                return Bindings.createLongBinding(()->getLong(property));
            case "float":
                return Bindings.createFloatBinding(()->getFloat(property));
            case "double":
                return Bindings.createDoubleBinding(()->getDouble(property));
            default:
                throw new UnsupportedOperationException(type+" not supported");
        }
    }
    public NumberBinding getBinding(String property)
    {
        return boundMap.get(property);
    }
    public ObjectBinding<DayPhase> dayPhaseProperty()
    {
        return dayPhaseProperty;
    }
    public void start()
    {
        solarWatch.start();
    }
    public void stop()
    {
        solarWatch.stop();
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
        List<Node> node = nodes.get(property);
        if (node != null)
        {
            node.forEach((n)->n.setDisable(true));
        }
    }
    @Override
    public void commit(String reason, Collection<String> updatedProperties)
    {
        System.err.println(updatedProperties);
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
            setDisable(property);
            actives.add(property, ttl, SECONDS);
            NumberBinding binding = boundMap.get(property);
            binding.invalidate();
        }
    }

    public Observable registerNode(String property, Node node)
    {
        nodes.add(property, node);
        node.setDisable(true);
        return boundMap.get(property);
    }

    public UnitType getOriginalUnit(String property)
    {
        UnitType unit = NMEAProperties.getInstance().getUnit(property);
        if (unit != null)
        {
            return unit;
        }
        else
        {
            switch (property)
            {
                default:
                    throw new UnsupportedOperationException(property+" has no unit");
            }
        }
    }

    public void registerNode(Node node, String... boundProperties)
    {
        for (String property : boundProperties)
        {
            nodes.add(property, node);
            node.setDisable(true);
        }
    }
}
