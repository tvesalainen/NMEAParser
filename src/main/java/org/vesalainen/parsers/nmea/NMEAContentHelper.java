/*
 * Copyright (C) 2014 Timo Vesalainen
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

/**
 *
 * @author Timo Vesalainen
 */
public class NMEAContentHelper
{
    private final String[] fields;

    public NMEAContentHelper(String msg)
    {
        this.fields = msg.split("[,\\*]");
    }
    
    public String getString(int index)
    {
        return fields[index];
    }
    public char getChar(int index)
    {
        String f = fields[index];
        if (f.length() > 1)
        {
            throw new IllegalArgumentException(f+" not a char");
        }
        if (f.isEmpty())
        {
            return 0;
        }
        return fields[index].charAt(0);
    }
    public float getFloat(int index)
    {
        String f = fields[index];
        if (f.isEmpty())
        {
            return Float.NaN;
        }
        return Float.parseFloat(f);
    }
    
    public float getDegree(int index)
    {
        String f = fields[index];
        if (f.isEmpty())
        {
            return Float.NaN;
        }
        int idx = f.indexOf('.')-2;
        float deg = Float.parseFloat(f.substring(0, idx));
        float min = Float.parseFloat(f.substring(idx));
        return deg+min/60F;
    }
    
}
