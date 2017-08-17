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
package org.vesalainen.parsers.nmea.ais;

import org.vesalainen.code.TransactionalSetter;
import org.vesalainen.code.TransactionalSetterClass;
import org.vesalainen.parsers.nmea.ais.AISObserver;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
@TransactionalSetterClass("org.vesalainen.parsers.nmea.ais.TransactionalAISObserverImpl")
public abstract class TransactionalAISObserver extends TransactionalSetter implements AISObserver
{

    public TransactionalAISObserver(int[] sizes)
    {
        super(sizes);
    }

    public static TransactionalAISObserver getInstance(AISObserver observer)
    {
        return TransactionalAISObserver.getInstance(TransactionalAISObserver.class, observer);
    }
}
