/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.binding.When;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableObjectValue;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import org.vesalainen.math.UnitType;
import org.vesalainen.navi.SolarWatch;
import org.vesalainen.navi.SolarWatch.DayPhase;
import org.vesalainen.parsers.nmea.NMEACategory;
import org.vesalainen.parsers.nmea.NMEAProperties;
import org.vesalainen.parsers.nmea.NMEAService;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ViewerService implements InvalidationListener
{
    private final CachedScheduledThreadPool executor;
    private final ViewerPreferences preferences;
    private final Locale locale;
    private NMEAService nmeaService;
    private final PropertyStore propertyStore;
    private final NMEAProperties nmeaProperties = NMEAProperties.getInstance();
    private final Simulator simulator;
    private boolean isInvalid;
    private final Binding<String> hostBinding;
    private final Binding<Integer> portBinding;
    private final Binding<Color> dayBackgroundColorBinding;
    private final Binding<Color> nightBackgroundColorBinding;
    private final Binding<Color> twilightBackgroundColorBinding;
    
    public ViewerService(CachedScheduledThreadPool executor, ViewerPreferences preferences, Locale locale)
    {
        this.executor = executor;
        this.preferences = preferences;
        this.locale = locale;
        this.propertyStore = new PropertyStore(executor, preferences);
        this.simulator = new Simulator(preferences, executor);
        hostBinding = preferences.getBinding("host");
        hostBinding.addListener(this);
        portBinding = preferences.getBinding("port");
        portBinding.addListener(this);
        dayBackgroundColorBinding = preferences.getBinding("dayBackgroundColor");
        nightBackgroundColorBinding = preferences.getBinding("nightBackgroundColor");
        twilightBackgroundColorBinding = preferences.getBinding("twilightBackgroundColor");
    }

    public void register(Set<Node> nodes)
    {
        nodes.forEach(this::register);
    }

    public void start()
    {
        openNMEAService();
        propertyStore.start();
    }
    void stop()
    {
        propertyStore.stop();
    }
    private void register(Node node)
    {
        if (node instanceof Gauge)
        {
            registerGauge((Gauge) node);
        }
        if (node instanceof PropertyBindable)
        {
            PropertyBindable bindable = (PropertyBindable) node;
            String[] bound = bindable.bind(preferences, propertyStore);
            propertyStore.registerNode(node, bound);
        }
    }
    private void registerGauge(Gauge gauge)
    {
        String property = gauge.getName();
        if (!nmeaProperties.isProperty(property))
        {
            throw new IllegalArgumentException(property+" is not NMEAProperty");
        }
        Observable dependency = propertyStore.registerNode(property, gauge);
        if (dependency == null)
        {
            throw new UnsupportedOperationException(property+" is not supported");
        }
        double max = nmeaProperties.getMax(property);
        double min = nmeaProperties.getMin(property);
        Class<?> type = nmeaProperties.getType(property);
        UnitType unit = nmeaProperties.getUnit(property);
        Binding<UnitType> unitBinding = getUnitBinding(property);
        StringBinding unitStringBinding = Bindings.createStringBinding(()->unitBinding.getValue().getUnit(), unitBinding);
        gauge.unitProperty().bind(unitStringBinding);
        StringBinding stringBinding;
        switch (type.getSimpleName())
        {
            case "float":
                stringBinding = Bindings.createStringBinding(()->String.format(locale, "%.1f", unit.convertTo(propertyStore.getFloat(property), unitBinding.getValue())), dependency, unitBinding);
                break;
            default:
                throw new UnsupportedOperationException(type+" not supported");
        }
        gauge.valueProperty().bind(stringBinding);
    }
    private Binding<UnitType> getUnitBinding(String property)
    {
        return preferences.getCategoryBinding(property);
    }

    private void openNMEAService()
    {
        try
        {
            if (nmeaService != null)
            {
                nmeaService.stop();
                nmeaService = null;
            }
            nmeaService = new NMEAService(hostBinding.getValue(), portBinding.getValue(), executor);
            nmeaService.addNMEAObserver(propertyStore);
            nmeaService.start();
            isInvalid = false;
        }
        catch (Exception ex)
        {
            Logger.getLogger(ViewerService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @Override
    public void invalidated(Observable observable)
    {
        if (!isInvalid)
        {
            isInvalid = true;
            executor.execute(this::openNMEAService);
        }
    }

    public StringBinding bindBackgroundColors()
    {
        ObjectBinding<DayPhase> autoPhaseProperty = propertyStore.dayPhaseProperty();
        Binding<DayPhase> forcedPhaseProperty = preferences.getBinding("dayPhase");
        Binding<Boolean> solarAutomation = preferences.getBinding("solarAutomation");
        
        Binding<DayPhase> dayPhaseProperty = new When((ObservableBooleanValue) solarAutomation)
                .then(autoPhaseProperty)
                .otherwise((ObservableObjectValue)forcedPhaseProperty);
                
        StringBinding colorBinding = Bindings.createStringBinding(()->
        {
            switch (dayPhaseProperty.getValue())
            {
                case DAY:
                    return colorToString(dayBackgroundColorBinding.getValue());
                case NIGHT:
                    return colorToString(nightBackgroundColorBinding.getValue());
                case TWILIGHT:
                    return colorToString(twilightBackgroundColorBinding.getValue());
                default:
                    throw new UnsupportedOperationException(autoPhaseProperty.getValue()+" not supported");
            }
        }, solarAutomation, autoPhaseProperty, forcedPhaseProperty, dayBackgroundColorBinding, nightBackgroundColorBinding, twilightBackgroundColorBinding);
        return colorBinding;
    }
    private String colorToString(Color color)
    {
        return String.format(Locale.US, "#%02X%02X%02X", 
                (int)(color.getRed()*255.0),
                (int)(color.getGreen()*255.0),
                (int)(color.getBlue()*255.0)
        );
    }
}
