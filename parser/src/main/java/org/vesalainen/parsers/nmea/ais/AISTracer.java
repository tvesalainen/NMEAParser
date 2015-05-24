/*
 * Copyright (C) 2013 Timo Vesalainen
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

package org.vesalainen.parsers.nmea.ais;

import java.lang.reflect.Method;
import org.vesalainen.parser.GenClassFactory;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.GenRegex;
import org.vesalainen.parser.util.InputReader;
import org.vesalainen.regex.Regex;
import org.vesalainen.util.InterfaceTracer;

/**
 * @author Timo Vesalainen
 */
public class AISTracer extends InterfaceTracer
{

    public AISTracer()
    {
        super(null);
    }

    public static AISObserver getTracer()
    {
        return InterfaceTracer.getTracer(
                AISObserver.class, 
                new AbstractAISObserver()
        );
    }
    
    public static AISObserver getTracer(Appendable appendable)
    {
        return InterfaceTracer.getTracer(
                AISObserver.class, 
                new AbstractAISObserver(), 
                appendable
        );
    }
    
}
