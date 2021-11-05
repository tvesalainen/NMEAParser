/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.lang.invoke.MethodHandles;
import org.vesalainen.code.InterfaceDispatcher;
import org.vesalainen.code.InterfaceDispatcherAnnotation;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
@InterfaceDispatcherAnnotation
public abstract class NMEADispatcher extends InterfaceDispatcher implements NMEAObserver, XdrObserver
{
    public static NMEADispatcher newInstance()
    {
        return newInstance(NMEADispatcher.class);
    }

    @Override
    public void xdrGroup(char type, float value, char unit, String name)
    {
        switch (name)
        {
            case "YAW":
                setYaw(value);
                break;
            case "PITCH":
                setPitch(value);
                break;
            case "ROLL":
                setRoll(value);
                break;
            default: 
                warning("xdr %s not supported", name);
        }
    }

}
