/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nmea.server;

import java.util.function.DoubleConsumer;
import org.vesalainen.math.UnitType;
import org.vesalainen.parsers.nmea.NMEAProperties;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Observer
{
    private String name;

    public Observer(String name)
    {
        this.name = name;
    }
    
    public static Observer getInstance(String name, String unit, String decimals)
    {
        NMEAProperties p = NMEAProperties.getInstance();
        Class<?> type = p.getType(name);
        switch (type.getSimpleName())
        {
            case "int":
            case "long":
            case "float":
            case "double":
                return new DoubleObserver(name, unit, decimals);
            case "char":
            case "String":
                return new StringObserver(name);
            default:
                if (type.isEnum())
                {
                    return new StringObserver(name);
                }
                else
                {
                    throw new UnsupportedOperationException(type+" not supported");
                }
        }
    }

    public String getName()
    {
        return name;
    }
    
    public void accept(long time, double arg)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void accept(long time, String arg)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public static class DoubleObserver extends Observer
    {
        private UnitType from;
        private UnitType to;
        private int decimals;
        
        public DoubleObserver(String name, String unit, String decimals)
        {
            super(name);
            this.from = NMEAProperties.getInstance().getUnit(name);
            this.to = UnitType.valueOf(unit);
            this.decimals = Integer.parseInt(decimals);
        }

        @Override
        public void accept(long time, double arg)
        {
            super.accept(time, arg); //To change body of generated methods, choose Tools | Templates.
        }

    }
    public static class StringObserver extends Observer
    {

        public StringObserver(String name)
        {
            super(name);
        }

        @Override
        public void accept(long time, String arg)
        {
            super.accept(time, arg); //To change body of generated methods, choose Tools | Templates.
        }

    }
}
