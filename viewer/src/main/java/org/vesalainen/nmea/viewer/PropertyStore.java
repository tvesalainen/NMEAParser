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

import org.vesalainen.fx.BasicObservable;
import static java.lang.Math.*;
import java.lang.invoke.MethodHandles;
import java.time.Clock;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;
import static java.util.concurrent.TimeUnit.*;
import java.util.function.Predicate;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.FloatBinding;
import javafx.beans.binding.LongBinding;
import javafx.beans.binding.NumberBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import org.vesalainen.code.AnnotatedPropertyStore;
import org.vesalainen.code.Property;
import org.vesalainen.fx.FunctionalDoubleBinding;
import org.vesalainen.fx.FunctionalFloatBinding;
import org.vesalainen.fx.FunctionalIntegerBinding;
import org.vesalainen.fx.FunctionalLongBinding;
import org.vesalainen.math.UnitType;
import static org.vesalainen.math.UnitType.*;
import org.vesalainen.navi.Navis;
import org.vesalainen.navi.SolarWatch;
import org.vesalainen.navi.SolarWatch.DayPhase;
import org.vesalainen.parsers.nmea.NMEACategory;
import static org.vesalainen.parsers.nmea.NMEACategory.*;
import org.vesalainen.parsers.nmea.NMEAProperties;
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
    private final ViewerPreferences preferences;
    private final FloatBinding keelOffsetBinding;
    private final FloatBinding transducerOffsetBinding;
    private final Map<String,NumberBinding> boundMap = new HashMap<>();
    private final Map<String,ObservableBooleanValue> disableMap = new HashMap<>();
    private final NavigableSet<String> modified = new ConcurrentSkipListSet<>();
    private final TimeToLiveSet<String> actives;
    private final LongBinding timeToLiveBinding;
    private final DoubleBinding solarDepressionAngleBinding;
    private final SolarWatch solarWatch;
    private final ObjectBinding<DayPhase> dayPhaseProperty;
    private final BasicObservable trendPulse = new BasicObservable(this, "trendPulse");
    private final Binding<Number> trendPeriod;
    private final FunctionalDoubleBinding radRelativeAngleOverGround;
    private final FunctionalDoubleBinding radTrackMadeGood;
    private final FunctionalDoubleBinding windOverGroundX;
    private final FunctionalDoubleBinding windOverGroundY;
    private final FunctionalDoubleBinding windSpeedOverGround;
    private final FunctionalDoubleBinding windAngleOverGround;
    private final FloatBinding depthOfWaterBinding;
    private final FloatBinding depthBelowKeelBinding;
    private final FloatBinding depthBelowTransducerBinding;
    private final FunctionalDoubleBinding radTrueHeading;
    private final FunctionalDoubleBinding currentOverGroundX;
    private final FunctionalDoubleBinding currentOverGroundY;
    private final FunctionalDoubleBinding currentSpeedOverGround;
    private final FunctionalDoubleBinding currentAngleOverGround;

    public PropertyStore(CachedScheduledThreadPool executor, ViewerPreferences preferences)
    {
        super(MethodHandles.lookup());
        this.executor = executor;
        this.preferences = preferences;
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
            disableMap.put(property, new SimpleBooleanProperty(this, property, true));
        }
        this.trendPeriod = preferences.getNumberBinding("trendPeriod");
        // depth
        depthOfWaterBinding = (FloatBinding) boundMap.get("depthOfWater");
        depthBelowKeelBinding = new FunctionalFloatBinding("depthBelowKeelBinding",
                ()->depthOfWaterBinding.get() - keelOffsetBinding.get(),
                depthOfWaterBinding,
                keelOffsetBinding
        );
        boundMap.put("depthBelowKeel", depthBelowKeelBinding);
        depthBelowTransducerBinding = new FunctionalFloatBinding("depthBelowTransducerBinding",
                ()->depthOfWaterBinding.get() - transducerOffsetBinding.get(),
                depthOfWaterBinding,
                transducerOffsetBinding
        );
        boundMap.put("depthBelowTransducer", depthBelowTransducerBinding);
        bindDisable("depthBelowKeel", "depthOfWater");
        bindDisable("depthBelowTransducer", "depthOfWater");
        bindDisable("depthBelowSurface", "depthOfWater");
        // wind
        FloatBinding trueHeadingBinding = (FloatBinding) boundMap.get("trueHeading");
        FloatBinding relativeWindAngleBinding = (FloatBinding) boundMap.get("relativeWindAngle");
        FloatBinding trackMadeGoodBinding = (FloatBinding) boundMap.get("trackMadeGood");
        FloatBinding relativeWindSpeedBinding = (FloatBinding) boundMap.get("relativeWindSpeed");
        FloatBinding speedOverGroundBinding = (FloatBinding) boundMap.get("speedOverGround");
        
        radRelativeAngleOverGround = new FunctionalDoubleBinding("radRelativeAngleOverGround",
                ()->toRadians(Navis.normalizeAngle(trueHeadingBinding.doubleValue() + relativeWindAngleBinding.doubleValue())), 
                trueHeadingBinding, 
                relativeWindAngleBinding);
        radTrackMadeGood = new FunctionalDoubleBinding("radTrackMadeGood",
                ()->toRadians(trackMadeGoodBinding.doubleValue()), 
                trackMadeGoodBinding);
        windOverGroundX = new FunctionalDoubleBinding("windOverGroundX",
                ()->
                cos(radRelativeAngleOverGround.doubleValue())*relativeWindSpeedBinding.doubleValue() - cos(radTrackMadeGood.doubleValue())*speedOverGroundBinding.doubleValue(),
                radRelativeAngleOverGround,
                radTrackMadeGood,
                relativeWindSpeedBinding,
                speedOverGroundBinding);
        windOverGroundY = new FunctionalDoubleBinding("windOverGroundY",
                ()->
                sin(radRelativeAngleOverGround.doubleValue())*relativeWindSpeedBinding.doubleValue() - sin(radTrackMadeGood.doubleValue())*speedOverGroundBinding.doubleValue(),
                radRelativeAngleOverGround,
                radTrackMadeGood,
                relativeWindSpeedBinding,
                speedOverGroundBinding);
        windSpeedOverGround = new FunctionalDoubleBinding("windSpeedOverGround",
                ()->
                Math.hypot(windOverGroundY.doubleValue(), windOverGroundX.doubleValue()), 
                windOverGroundX, windOverGroundY);
        windAngleOverGround = new FunctionalDoubleBinding("windAngleOverGround",
                ()->
                Navis.normalizeAngle(Math.toDegrees(Math.atan2(windOverGroundY.doubleValue(), windOverGroundX.doubleValue()))), 
                windOverGroundX, windOverGroundY);
        boundMap.put("windSpeedOverGround", windSpeedOverGround);
        boundMap.put("windAngleOverGround", windAngleOverGround);
        ObservableBooleanValue windDisableBind = bindDisable("windSpeedOverGround", "trueHeading", "relativeWindAngle", "trackMadeGood", "relativeWindSpeed", "speedOverGround");
        disableMap.put("windAngleOverGround", windDisableBind);
        // current
        FloatBinding waterSpeedBinding = (FloatBinding) boundMap.get("waterSpeed");
        radTrueHeading = new FunctionalDoubleBinding("radTrueHeading",
                ()->toRadians(trueHeadingBinding.doubleValue()), 
                trueHeadingBinding);
        currentOverGroundX = new FunctionalDoubleBinding("currentOverGroundX",
                ()->
                cos(radTrueHeading.doubleValue())*waterSpeedBinding.doubleValue() - cos(radTrackMadeGood.doubleValue())*speedOverGroundBinding.doubleValue(),
                radTrueHeading,
                radTrackMadeGood,
                waterSpeedBinding,
                speedOverGroundBinding);
        currentOverGroundY = new FunctionalDoubleBinding("currentOverGroundY",
                ()->
                sin(radTrueHeading.doubleValue())*waterSpeedBinding.doubleValue() - sin(radTrackMadeGood.doubleValue())*speedOverGroundBinding.doubleValue(),
                radTrueHeading,
                radTrackMadeGood,
                waterSpeedBinding,
                speedOverGroundBinding);
        currentSpeedOverGround = new FunctionalDoubleBinding("currentSpeedOverGround",
                ()->
                Math.hypot(currentOverGroundY.doubleValue(), currentOverGroundX.doubleValue()), 
                currentOverGroundX, currentOverGroundY);
        currentAngleOverGround = new FunctionalDoubleBinding("currentAngleOverGround",
                ()->
                Navis.normalizeAngle(Math.toDegrees(Math.atan2(currentOverGroundY.doubleValue(), currentOverGroundX.doubleValue()))), 
                currentOverGroundX, currentOverGroundY);
        boundMap.put("currentSpeedOverGround", currentSpeedOverGround);
        boundMap.put("currentAngleOverGround", currentAngleOverGround);
        ObservableBooleanValue currentDisableBind = bindDisable("currentSpeedOverGround", "trueHeading", "trackMadeGood", "waterSpeed", "speedOverGround");
        disableMap.put("currentAngleOverGround", currentDisableBind);
        
        checkDisabled();
        scheduleTrendPulse();
    }
    private ObservableBooleanValue bindDisable(String property, String... dependencies)
    {
        ObservableBooleanValue disableBind = getDisableBind(dependencies);
        disableMap.put(property, disableBind);
        return disableBind;
    }
    public ObservableBooleanValue getDisableBind(String... properties)
    {
        ObservableBooleanValue sbp = disableMap.get(properties[0]);
        for (int ii=1;ii<properties.length;ii++)
        {
            ObservableBooleanValue  sbpx = disableMap.get(properties[ii]);
            sbp = Bindings.or(sbp, sbpx);
        }
        return sbp;
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
                return new FunctionalIntegerBinding(property, getIntSupplier(property));
            case "long":
                return new FunctionalLongBinding(property, getLongSupplier(property));
            case "float":
                return new FunctionalFloatBinding(property, getDoubleSupplier(property));
            case "double":
                return new FunctionalDoubleBinding(property, getDoubleSupplier(property));
            default:
                throw new UnsupportedOperationException(type+" not supported");
        }
    }

    private void scheduleTrendPulse()
    {
        trendPulse.invalidate();
        executor.schedule(()->scheduleTrendPulse(), trendPeriod.getValue().longValue(), SECONDS);
    }

    public Observable getTrendPulse()
    {
        return trendPulse;
    }
    
    public CachedScheduledThreadPool getExecutor()
    {
        return executor;
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
    
    @Property public float getCurrentAngleOverGround()
    {
        return currentAngleOverGround.floatValue();
    }
    @Property public float getCurrentSpeedOverGround()
    {
        return currentSpeedOverGround.floatValue();
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
        return depthBelowKeelBinding.get();
    }
    @Property public float getDepthBelowSurface()
    {
        return depthOfWaterBinding.get();
    }
    @Property public float getDepthBelowTransducer()
    {
        return depthBelowTransducerBinding.get();
    }
    @Property public void setDepthBelowKeel(float meters)
    {
        setDepthOfWater(meters + keelOffsetBinding.get());
    }
    @Property public void setDepthBelowSurface(float meters)
    {
        setDepthOfWater(meters);
    }
    @Property public void setDepthBelowTransducer(float meters)
    {
        setDepthOfWater(meters + transducerOffsetBinding.get());
    }
    private void setDepthOfWater(float dow)
    {
        activate("depthOfWater");
        if (depthOfWater != dow)
        {
            modified.add("depthOfWater");
            depthOfWater = dow;
        }
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
        SimpleBooleanProperty sbp = (SimpleBooleanProperty) disableMap.get(property);
        sbp.set(disabled);
    }

    @Override
    public void commit(String reason, Collection<String> updatedProperties, Predicate<String> isModified)
    {
        boolean newModified = false;
        for (String property : updatedProperties)
        {
            activate(property);
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
    private void activate(String property)
    {
        long ttl = timeToLiveBinding.get();
        boolean newEntry = actives.add(property, ttl, SECONDS);
        if (newEntry)
        {
            Platform.runLater(()->setDisable(property, false));
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
    /**
     * Returns property's original unit sent by parser.
     * @param property
     * @return 
     */
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
                case "windAngleOverGround":
                case "currentAngleOverGround":
                    return DEGREE;
                case "windSpeedOverGround":
                case "currentSpeedOverGround":
                    return KNOT;
                default:
                    throw new UnsupportedOperationException(property+" has no unit");
            }
        }
    }
    public NMEACategory getNMEACategory(String property)
    {
        NMEAProperties nmeaProperties = NMEAProperties.getInstance();
        if (nmeaProperties.isProperty(property))
        {
            return nmeaProperties.getCategory(property);
        }
        else
        {
            switch (property)
            {
                case "windAngleOverGround":
                case "currentAngleOverGround":
                    return BEARING;
                case "windSpeedOverGround":
                case "currentSpeedOverGround":
                    return SPEED;
                default:
                    throw new UnsupportedOperationException(property+" has no unit");
            }
        }
    }
    public Binding<UnitType> getCategoryBinding(String property)
    {
        NMEACategory cat = getNMEACategory(property);
        if (cat != null)
        {
            switch (cat)
            {
                case DEPTH:
                    return preferences.getBinding("depthUnit");
                case SPEED:
                    return preferences.getBinding("speedUnit");
                case TEMPERATURE:
                    return preferences.getBinding("temperatureUnit");
                case BEARING:
                    return (Binding<UnitType>) ViewerPreferences.DEGREE_BINDING;
                default:
                    throw new UnsupportedOperationException(cat+" not supported");
            }
        }
        else
        {
            switch (property)
            {
                default:
                    throw new UnsupportedOperationException(property+" has no category");
            }
        }
    }
}
