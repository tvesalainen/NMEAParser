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
package org.vesalainen.nmea.router;

/**
 *
 * @author tkv
 */
public class RouterThreadGroup extends ThreadGroup implements AutoCloseable
{
    private Throwable throwable;

    public RouterThreadGroup(String name)
    {
        super(name);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e)
    {
        throwable = e;
    }

    public Throwable getThrowable()
    {
        try
        {
            return throwable;
        }
        finally
        {
            throwable = null;
        }
    }

    @Override
    public void close() throws Exception
    {
        interrupt();
    }
    
    
}
