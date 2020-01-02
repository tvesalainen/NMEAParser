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

import java.util.Collections;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.Vector;
import javafx.beans.InvalidationListener;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableStringValue;
import org.vesalainen.parsers.nmea.NMEAProperties;
import org.vesalainen.text.CamelCase;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class I18n extends ResourceBundle
{

    private Vector<String> resources = new Vector<>();
    public I18n()
    {
        NMEAProperties nmeaProperties = NMEAProperties.getInstance();
        resources.addAll(nmeaProperties.getAllProperties());
    }

    @Override
    protected Object handleGetObject(String key)
    {
        return CamelCase.delimited(key, " ");
    }

    @Override
    public Enumeration<String> getKeys()
    {
        return resources.elements();
    }
    
    public static void bind(StringProperty target, ResourceBundle resources, StringProperty key)
    {
        target.bind(new ObservableResource(resources, key));
    }
    private static class ObservableResource implements ObservableStringValue
    {
        private ResourceBundle resources;
        private StringProperty key;

        public ObservableResource(ResourceBundle resources, StringProperty key)
        {
            this.resources = resources;
            this.key = key;
        }
        
        @Override
        public String get()
        {
            return resources.getString(key.get());
        }

        @Override
        public void addListener(ChangeListener<? super String> listener)
        {
        }

        @Override
        public void removeListener(ChangeListener<? super String> listener)
        {
        }

        @Override
        public String getValue()
        {
            return get();
        }

        @Override
        public void addListener(InvalidationListener listener)
        {
            key.addListener(listener);
        }

        @Override
        public void removeListener(InvalidationListener listener)
        {
            key.removeListener(listener);
        }
        
    }
}
