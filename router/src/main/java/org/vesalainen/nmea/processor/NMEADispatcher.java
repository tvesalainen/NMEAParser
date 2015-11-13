/*
 * Copyright (C) 2015 tkv
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
package org.vesalainen.nmea.processor;

import org.vesalainen.code.PropertyDispatcher;
import org.vesalainen.code.PropertyDispatcherClass;
import org.vesalainen.parsers.nmea.NMEAObserver;

/**
 *
 * @author tkv
 */
@PropertyDispatcherClass("org.vesalainen.nmea.sender.ValueDispatcherImpl")
public abstract class NMEADispatcher extends PropertyDispatcher implements NMEAObserver
{

    public NMEADispatcher(int[] sizes)
    {
        super(sizes);
    }
    
}
