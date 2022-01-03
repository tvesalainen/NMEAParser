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
package web;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;
import org.vesalainen.parsers.nmea.NMEAProperties;
import org.vesalainen.text.CamelCase;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class I18n extends ResourceBundle
{
    private static final Map<Locale,ResourceBundle> map = new HashMap<>();
    
    private Vector<String> resources = new Vector<>();
    public I18n()
    {
        NMEAProperties nmeaProperties = NMEAProperties.getInstance();
        resources.addAll(nmeaProperties.getAllProperties());
    }

    public static ResourceBundle get()
    {
        return get(Locale.getDefault());
    }
    public static ResourceBundle get(Locale locale)
    {
        ResourceBundle rb = map.get(locale);
        if (rb == null)
        {
            rb = ResourceBundle.getBundle(I18n.class.getName(), locale);
            map.put(locale, rb);
        }
        return rb;
    }
    @Override
    protected Object handleGetObject(String key)
    {
        return null;//CamelCase.delimited(key, " ");
    }

    @Override
    public Enumeration<String> getKeys()
    {
        return resources.elements();
    }
    
    public static I18nString getI18nString(String key)
    {
        return new I18nString(key, get().getString(key));
    }
    public static class I18nString implements Comparable<I18nString>
    {
        private String key;
        private String string;

        public I18nString(String key, String string)
        {
            this.key = key;
            this.string = string;
        }

        public String getKey()
        {
            return key;
        }

        @Override
        public String toString()
        {
            return string;
        }

        @Override
        public int compareTo(I18nString o)
        {
            return string.compareTo(o.string);
        }
        
    }
}
