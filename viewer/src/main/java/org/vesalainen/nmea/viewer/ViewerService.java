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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import org.vesalainen.math.UnitType;
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
    private final PropertyStore propertyStore = new PropertyStore();
    private final NMEAProperties nmeaProperties = NMEAProperties.getInstance();
    private boolean isInvalid;
    private final Binding<String> hostBinding;
    private final Binding<Integer> portBinding;
    private final Map<String,ObservableProperty> listenerMap = new HashMap<>();
    
    public ViewerService(CachedScheduledThreadPool executor, ViewerPreferences preferences, Locale locale)
    {
        this.executor = executor;
        this.preferences = preferences;
        this.locale = locale;
        hostBinding = preferences.get("host");
        hostBinding.addListener(this);
        portBinding = preferences.get("port");
        portBinding.addListener(this);
        
        for (String property : propertyStore.getProperties())
        {
            listenerMap.put(property, new ObservableProperty());
        }
    }

    public void register(Set<Node> nodes)
    {
        nodes.forEach(this::register);
    }

    public void start()
    {
        openNMEAService();
    }
    private void register(Node node)
    {
        if (node instanceof Gauge)
        {
            registerGauge((Gauge) node);
        }
    }
    private void registerGauge(Gauge gauge)
    {
        String property = gauge.getPropertyProperty();
        if (!nmeaProperties.isProperty(property))
        {
            throw new IllegalArgumentException(property+" is not NMEAProperty");
        }
        ObservableProperty dependency = listenerMap.get(property);
        if (dependency == null)
        {
            throw new UnsupportedOperationException(property+" is not supported");
        }
        double max = nmeaProperties.getMax(property);
        double min = nmeaProperties.getMin(property);
        Class<?> type = nmeaProperties.getType(property);
        Binding<UnitType> unitBinding = getUnitBinding(property);
        StringBinding unitStringBinding = Bindings.createStringBinding(()->unitBinding.getValue().getUnit(), unitBinding);
        gauge.propertyUnitProperty().bind(unitStringBinding);
        StringBinding stringBinding;
        switch (type.getSimpleName())
        {
            case "float":
                stringBinding = Bindings.createStringBinding(()->String.format(locale, "%.1f", unitBinding.getValue().convertTo(propertyStore.getFloat(property), unitBinding.getValue())), dependency, unitBinding);
                break;
            default:
                throw new UnsupportedOperationException(type+" not supported");
        }
        gauge.propertyValueProperty().bind(stringBinding);
    }
    private Binding<UnitType> getUnitBinding(String property)
    {
        NMEACategory cat = nmeaProperties.getCategory(property);
        UnitType unit = nmeaProperties.getUnit(property);
        switch (cat)
        {
            case DEPTH:
                return preferences.get("depth");
            case SPEED:
                return preferences.get("speed");
            default:
                throw new UnsupportedOperationException(cat+" not supported");
        }
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
        catch (IOException ex)
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

    void stop()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
