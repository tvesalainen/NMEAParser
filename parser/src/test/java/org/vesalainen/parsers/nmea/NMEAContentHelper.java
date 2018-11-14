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

import java.util.ArrayList;
import java.util.List;

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
            return null;
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
    public float getSign(int index)
    {
        String f = fields[index];
        if (f.length() > 1)
        {
            return Float.NaN;
        }
        if (f.isEmpty())
        {
            return Float.NaN;
        }
        switch (fields[index].charAt(0))
        {
            case 'R':
            case 'E':
                return 1;
            case 'L':
            case 'W':
                return -1;
            default:
            return Float.NaN;
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
    
    public double getDegree(int index)
    {
        String f = fields[index];
        if (f.isEmpty())
        {
            return Double.NaN;
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
        double deg = Double.parseDouble(f.substring(0, idx));
        double min = Double.parseDouble(f.substring(idx));
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
    public List<String> getList(int start, int length)
    {
        List<String> list = new ArrayList<>();
        for (int ii=0;ii<length;ii++)
        {
            list.add(fields[start+ii]);
        }
        return list;
    }

    public int parseYear(String yy)
    {
        int year = Integer.parseInt(yy);
        if (year > 70)
        {
            return 1900+year;
        }
        else
        {
            return 2000+year;
        }
    }
}
