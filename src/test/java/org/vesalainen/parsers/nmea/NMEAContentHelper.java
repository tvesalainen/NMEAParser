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
    
    public int getSize()
    {
        return fields.length;
    }
    
    public String getString(int index)
    {
        String f = fields[index];
        if (f.isEmpty())
        {
            return null;
        }
        return f;
    }
    public Character getChar(int index)
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
    public String getPrefix(int index)
    {
        String f = fields[index];
        if (f.length() > 1)
        {
            throw new IllegalArgumentException(f+" not a char");
        }
        if (f.isEmpty())
        {
            return null;
        }
        switch (fields[index].charAt(0))
        {
            case 'R':
                return "relative";
            case 'T':
                return "true";
            case 'M':
                return "magnetic";
            default:
                throw new IllegalArgumentException(fields[index].charAt(0)+" cannot made prefix");
        }
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
        String s = fields[index+1];
        int sign;
        switch (s)
        {
            case "N":
            case "E":
                sign = 1;
                break;
            case "S":
            case "W":
                sign = -1;
                break;
            default:
                throw new IllegalArgumentException("got '"+s+"' expected N/S/E/W");
        }
        int idx = f.indexOf('.')-2;
        float deg = Float.parseFloat(f.substring(0, idx));
        float min = Float.parseFloat(f.substring(idx));
        return sign*(deg+min/60F);
    }

    public Integer getInt(int index)
    {
        String i = fields[index];
        if (i.isEmpty())
        {
            return null;
        }
        return Integer.parseInt(i);
    }
    public Integer getHex(int index)
    {
        String i = fields[index];
        if (i.isEmpty())
        {
            return null;
        }
        return Integer.parseInt(i, 16);
    }
    
}
