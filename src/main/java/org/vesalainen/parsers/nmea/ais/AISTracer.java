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
@GenClassname("org.vesalainen.parsers.nmea.ais.AISTracerImpl")
public class AISTracer extends InterfaceTracer
{

    @GenRegex("[01]+")
    protected Regex sixbit;
    

    public AISTracer()
    {
        super(null);
    }

    public static AISObserver getTracer()
    {
        return InterfaceTracer.getTracer(
                AISObserver.class, 
                (AISTracer) GenClassFactory.loadGenInstance(AISTracer.class), 
                new AbstractAISObserver()
        );
    }
    
    public static AISObserver getTracer(Appendable appendable)
    {
        return InterfaceTracer.getTracer(
                AISObserver.class, 
                (AISTracer) GenClassFactory.loadGenInstance(AISTracer.class), 
                new AbstractAISObserver(), 
                appendable
        );
    }
    
    @Override
    public synchronized Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        if (
                args != null &&
                args.length == 2 &&
                (args[0] instanceof InputReader) &&
                (args[1] instanceof Integer) &&
                sixbit.isMatch((CharSequence) args[0])
                )
        {
            printer.print(method.getName());
            printer.print("(");
            printer.print(AisUtil.makeString((CharSequence) args[0]));
            printer.println(")");
            return null;
        }
        else
        {
            return super.invoke(proxy, method, args);
        }
    }
    
}
