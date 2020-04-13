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

import static java.lang.Math.*;
import java.lang.invoke.MethodHandles;
import java.time.Clock;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;
import static java.util.concurrent.TimeUnit.*;
import java.util.function.Predicate;
import javafx.application.Platform;
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
import org.vesalainen.fx.FunctionalDoubleBinding;
import org.vesalainen.fx.FunctionalFloatBinding;
import org.vesalainen.fx.FunctionalIntegerBinding;
import org.vesalainen.fx.FunctionalLongBinding;
import org.vesalainen.math.UnitType;
import org.vesalainen.navi.Navis;
import org.vesalainen.navi.SolarWatch;
import org.vesalainen.navi.SolarWatch.DayPhase;
import org.vesalainen.parsers.nmea.NMEAProperties;
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
    private final NavigableSet<String> modified = new ConcurrentSkipListSet<>();
    private final TimeToLiveSet<String> actives;
    private final LongBinding timeToLiveBinding;
    private final MapList<String,Node> nodes = new ConcurrentHashMapList<>();
    private final DoubleBinding solarDepressionAngleBinding;
    private SolarWatch solarWatch;
    private ObjectBinding<DayPhase> dayPhaseProperty;
    private final FunctionalDoubleBinding radRelativeAngleOverGround;
    private final FunctionalDoubleBinding radTrackMadeGood;
    private final FunctionalDoubleBinding windOverGroundX;
    private final FunctionalDoubleBinding windOverGroundY;
    private final FunctionalDoubleBinding windSpeedOverGround;
    private final FunctionalDoubleBinding windAngleOverGround;

    public PropertyStore(CachedScheduledThreadPool executor, ViewerPreferences preferences)
    {
        super(MethodHandles.lookup());
        this.executor = executor;
        this.keelOffsetBinding = (FloatBinding) preferences.getNumberBinding("keelOffset");
        this.transducerOffsetBinding = (FloatBinding) preferences.getNumberBinding("transducerOffset");
        this.timeToLiveBinding = (LongBinding)preferences.getNumberBinding("timeToLive");
        this.actives = new TimeToLiveSet<>(Clock.systemUTC(), preferences.getLong("timeToLive"), SECONDS, (p)->setDisable(p, true));
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
        FloatBinding depthOfWaterBinding = (FloatBinding) boundMap.get("depthOfWater");
        FloatBinding depthBelowKeelBinding = (FloatBinding) boundMap.get("depthBelowKeel");
        FloatBinding depthBelowSurfaceBinding = (FloatBinding) boundMap.get("depthBelowSurface");
        FloatBinding depthBelowTransducerBinding = (FloatBinding) boundMap.get("depthBelowTransducer");
        depthBelowKeelBinding.addListener(b->depthOfWaterBinding.invalidate());
        depthBelowSurfaceBinding.addListener(b->depthOfWaterBinding.invalidate());
        depthBelowTransducerBinding.addListener(b->depthOfWaterBinding.invalidate());

        FloatBinding trueHeadingBinding = (FloatBinding) boundMap.get("trueHeading");
        FloatBinding relativeWindAngleBinding = (FloatBinding) boundMap.get("relativeWindAngle");
        FloatBinding trackMadeGoodBinding = (FloatBinding) boundMap.get("trackMadeGood");
        FloatBinding relativeWindSpeedBinding = (FloatBinding) boundMap.get("relativeWindSpeed");
        FloatBinding speedOverGroundBinding = (FloatBinding) boundMap.get("speedOverGround");
        
        radRelativeAngleOverGround = new FunctionalDoubleBinding(
                ()->toRadians(Navis.normalizeAngle(trueHeading + relativeWindAngle)), 
                trueHeadingBinding, 
                relativeWindAngleBinding);
        radTrackMadeGood = new FunctionalDoubleBinding(
                ()->toRadians(trackMadeGood), 
                trackMadeGoodBinding);
        windOverGroundX = new FunctionalDoubleBinding(()->
                cos(radRelativeAngleOverGround.doubleValue())*relativeWindSpeed - cos(radTrackMadeGood.doubleValue())*speedOverGround,
                radRelativeAngleOverGround,
                radTrackMadeGood,
                relativeWindSpeedBinding,
                speedOverGroundBinding);
        windOverGroundY = new FunctionalDoubleBinding(()->
                sin(radRelativeAngleOverGround.doubleValue())*relativeWindSpeed - sin(radTrackMadeGood.doubleValue())*speedOverGround,
                radRelativeAngleOverGround,
                radTrackMadeGood,
                relativeWindSpeedBinding,
                speedOverGroundBinding);
        windSpeedOverGround = new FunctionalDoubleBinding(()->
                Math.hypot(windOverGroundY.doubleValue(), windOverGroundX.doubleValue()), 
                windOverGroundX, windOverGroundY);
        windAngleOverGround = new FunctionalDoubleBinding(()->
                Navis.normalizeAngle(Math.toDegrees(Math.atan2(windOverGroundY.doubleValue(), windOverGroundX.doubleValue()))), 
                windOverGroundX, windOverGroundY);
        boundMap.put("windSpeedOverGround", windSpeedOverGround);
        boundMap.put("windAngleOverGround", windAngleOverGround);
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
                return new FunctionalIntegerBinding(getIntSupplier(property));
            case "long":
                return new FunctionalLongBinding(getLongSupplier(property));
            case "float":
                return new FunctionalFloatBinding(getDoubleSupplier(property));
            case "double":
                return new FunctionalDoubleBinding(getDoubleSupplier(property));
            default:
                throw new UnsupportedOperationException(type+" not supported");
        }
    }
    private Observable[] dependencies(String... properties)
    {
        Observable[] res = new Observable[properties.length];
        int idx = 0;
        for (String property : properties)
        {
            res[idx++] = boundMap.get(property);
        }
        return res;
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
    
    @Property public float getWindAngleOverGround()
    {
        return windAngleOverGround.floatValue();
    }
    @Property public float getWindSpeedOverGround()
    {
        return windSpeedOverGround.floatValue();
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
    }
    @Property public void setDepthBelowSurface(float meters)
    {
        depthOfWater = meters;
    }
    @Property public void setDepthBelowTransducer(float meters)
    {
        depthOfWater = meters + transducerOffsetBinding.get();
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
    private void setDisable(String property, boolean disabled)
    {
        List<Node> node = nodes.get(property);
        if (node != null)
        {
            node.forEach((n)->n.setDisable(disabled));
        }
    }

    @Override
    public void commit(String reason, Collection<String> updatedProperties, Predicate<String> isModified)
    {
        boolean newModified = false;
        long ttl = timeToLiveBinding.get();
        for (String property : updatedProperties)
        {
            boolean newEntry = actives.add(property, ttl, SECONDS);
            if (newEntry)
            {
                System.err.println(property+" NEW");
                Platform.runLater(()->setDisable(property, false));
            }
            if (isModified.test(property))
            {
                modified.add(property);
                newModified = true;
            }
        }
        if (newModified)
        {
            Platform.runLater(this::invalidate);
        }
    }
    /**
     * this is run in platform thread
     */
    private void invalidate()
    {
        Iterator<String> iterator = modified.iterator();
        while (iterator.hasNext())
        {
            String property = iterator.next();
            iterator.remove();
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
