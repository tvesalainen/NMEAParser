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
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.LongBinding;
import javafx.beans.binding.NumberBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableNumberValue;
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
import org.vesalainen.nmea.viewer.store.FXPropertySetter;
import org.vesalainen.nmea.viewer.store.FloatPropertyValue;
import org.vesalainen.parsers.nmea.NMEACategory;
import static org.vesalainen.parsers.nmea.NMEACategory.*;
import org.vesalainen.parsers.nmea.NMEAProperties;
import org.vesalainen.util.TimeToLiveSet;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class PropertyStore extends FXPropertySetter
{
    
    private final CachedScheduledThreadPool executor;
    private final ViewerPreferences preferences;
    private final FloatBinding keelOffsetBinding;
    private final FloatBinding transducerOffsetBinding;
    private final TimeToLiveSet<String> actives;
    private final LongBinding timeToLiveBinding;
    private final DoubleBinding solarDepressionAngleBinding;
    private final SolarWatch solarWatch;
    private final ObjectBinding<DayPhase> dayPhaseProperty;
    private final BasicObservable trendPulse = new BasicObservable(this, "trendPulse");
    private final Binding<Number> trendPeriod;

    public PropertyStore(CachedScheduledThreadPool executor, ViewerPreferences preferences)
    {
        this.executor = executor;
        this.preferences = preferences;
        this.keelOffsetBinding = (FloatBinding) preferences.getNumberBinding("keelOffset");
        this.transducerOffsetBinding = (FloatBinding) preferences.getNumberBinding("transducerOffset");
        this.timeToLiveBinding = (LongBinding)preferences.getNumberBinding("timeToLive");
        this.actives = new TimeToLiveSet<>(Clock.systemUTC(), preferences.getLong("timeToLive"), SECONDS, (p)->setDisable(p, true));
        
        addNmea(true, true,
                "epochMillis", 
                "latitude", 
                "longitude", 
                "depthOfWater", 
                "waterSpeed", 
                "waterTemperature", 
                "trueHeading",
                "speedOverGround",
                "trackMadeGood",
                "magneticVariation",
                "relativeWindAngle",
                "relativeWindSpeed"
        );
        addNmea(true, false,
                "year",
                "month",
                "day",
                "hour",
                "minute",
                "second"
        );
        this.solarDepressionAngleBinding = (DoubleBinding) preferences.getNumberBinding("solarDepressionAngle");
        this.solarWatch = new SolarWatch(getLongGetter("epochMillis"), executor, ()->preferences.getLong("solarUpdateSeconds"), getDoubleGetter("latitude"), getDoubleGetter("longitude"), ()->solarDepressionAngleBinding.doubleValue());
        this.dayPhaseProperty = Bindings.createObjectBinding(()->solarWatch.getPhase());
        solarWatch.addObserver((p)->Platform.runLater(dayPhaseProperty::invalidate));
        this.trendPeriod = preferences.getNumberBinding("trendPeriod");
        
        // depth
        ObservableNumberValue keelOffset = (ObservableNumberValue) preferences.getNumberBinding("keelOffset");
        ObservableNumberValue transducerOffset = (ObservableNumberValue) preferences.getNumberBinding("transducerOffset");
        addExt(true, "keelOffset", keelOffset);
        addExt(true, "transducerOffset", transducerOffset);
        FloatPropertyValue depthOfWater = (FloatPropertyValue) getProperty("depthOfWater");
        addFloatSetter("depthBelowKeel", (v)->depthOfWater.set(v + keelOffset.floatValue()));
        addFloatSetter("depthBelowTransducer", (v)->depthOfWater.set(v + transducerOffset.floatValue()));
        bind(true, "depthBelowKeel", (dow, ko)->dow-ko, "depthOfWater", "keelOffset");
        bind(true, "depthBelowTransducer", (dow, to)->dow-to, "depthOfWater", "transducerOffset");
        
        // wind
        bind(false, "radRelativeAngleOverGround", (th, rwa)->toRadians(Navis.normalizeAngle(th + rwa)), "trueHeading", "relativeWindAngle");
        bind(false, "radTrackMadeGood", (tmg)->toRadians(tmg), "trackMadeGood");
        bind(false, "windOverGroundX", (rraog, rws, rtmg, sog)->cos(rraog)*rws - cos(rtmg)*sog, "radRelativeAngleOverGround", "relativeWindSpeed", "radTrackMadeGood", "speedOverGround");
        bind(false, "windOverGroundY", (rraog, rws, rtmg, sog)->sin(rraog)*rws - sin(rtmg)*sog, "radRelativeAngleOverGround", "relativeWindSpeed", "radTrackMadeGood", "speedOverGround");
        bind(true, "windSpeedOverGround", (wogy, wogx)->Math.hypot(wogy, wogx), "windOverGroundY", "windOverGroundX");
        bind(true, "windAngleOverGround", (wogy, wogx)->Navis.normalizeAngle(Math.toDegrees(Math.atan2(wogy, wogx))), "windOverGroundY", "windOverGroundX");

        // current
        bind(false, "radTrueHeading", (th)->toRadians(th), "trueHeading");
        bind(false, "currentOverGroundX", (rth, ws, rtmg, sog)->cos(rth)*ws - cos(rtmg)*sog, "radTrueHeading", "waterSpeed", "radTrackMadeGood", "speedOverGround");
        bind(false, "currentOverGroundY", (rth, ws, rtmg, sog)->sin(rth)*ws - sin(rtmg)*sog, "radTrueHeading", "waterSpeed", "radTrackMadeGood", "speedOverGround");
        bind(true, "currentSpeedOverGround", (cogy, cogx)->Math.hypot(cogy, cogx), "currentOverGroundY", "currentOverGroundX");
        bind(true, "currentAngleOverGround", (cogy, cogx)->Navis.normalizeAngle(Math.toDegrees(Math.atan2(cogy, cogx))), "currentOverGroundY", "currentOverGroundX");
        
        //time
        bind(true, "utcDate", (y, m, d)->10000*y+100*m+d, "year", "month", "day");
        bind(true, "utcTime", (h, m, s)->10000*h+100*m+s, "hour", "minute", "secons");
        
        checkDisabled();
        scheduleTrendPulse();
    }

    private void scheduleTrendPulse()
    {
        trendPulse.signal();
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
    private void checkDisabled()
    {
        Platform.runLater(()->actives.size());
        executor.schedule(this::checkDisabled, timeToLiveBinding.get(), SECONDS);
    }
    @Override
    public void commit(String reason, Collection<String> updatedProperties)
    {
        updatedProperties.forEach((property) ->
        {
            activate(property);
        });
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
                case "utcDate":
                case "utcTime":
                case "epochMillis":
                    return UNITLESS;
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
                case "utcDate":
                case "utcTime":
                case "epochMillis":
                    return MISCELLENEOUS;
                default:
                    throw new UnsupportedOperationException(property+" has no category");
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
                case COORDINATE:
                    return preferences.getBinding("coordinateUnit");
                case WIND:
                    switch (NMEAProperties.getInstance().getUnit(property).getCategory())
                    {
                        case SPEED:
                            return preferences.getBinding("windSpeedUnit");
                        case PLANE_ANGLE:
                            return preferences.getBinding("windAngleUnit");
                        default:
                            throw new UnsupportedOperationException(cat+"/"+NMEAProperties.getInstance().getUnit(property).getCategory()+" not supported");
                    }
                case BEARING:
                    return preferences.getBinding("bearingUnit");
                default:
                    return Bindings.createObjectBinding(()->UNITLESS);
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
