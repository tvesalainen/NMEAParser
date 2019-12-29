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

import javafx.util.StringConverter;
import org.vesalainen.text.CamelCase;

/**
 * 
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PropertyTitleConverter extends StringConverter<String>
{
    public static final PropertyTitleConverter PROPERTY_TITLE_CONVERTER = new PropertyTitleConverter();
    /**
     * Returns Camel Case from camelCase
     * @param object
     * @return 
     */
    @Override
    public String toString(String object)
    {
        if (object != null)
        {
            return CamelCase.delimited(object, " ");
        }
        else
        {
            return null;
        }
    }
    /**
     * Returns camelCase from Camel Case
     * @param string
     * @return 
     */
    @Override
    public String fromString(String string)
    {
        if (string != null)
        {
            return CamelCase.property(string);
        }
        else
        {
            return null;
        }
    }
    
}
