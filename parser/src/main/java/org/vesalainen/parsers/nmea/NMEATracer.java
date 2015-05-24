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

package org.vesalainen.parsers.nmea;

import java.util.ArrayList;
import java.util.List;
import org.vesalainen.parser.util.InputReader;
import org.vesalainen.io.AppendablePrinter;
import org.vesalainen.util.InterfaceTracer;

/**
 * @author Timo Vesalainen
 */
public class NMEATracer extends InterfaceTracer
{

    public NMEATracer()
    {
        super(null);
    }
    
    public static NMEAObserver getTracer()
    {
        return getTracer(NMEAObserver.class, new NMEATracer(), null);
    }

    public static NMEAObserver getTracer(Appendable appendable)
    {
        return getTracer(NMEAObserver.class, new NMEATracer(), null, appendable);
    }
    
}
