/*
 * Copyright (C) 2014 tkv
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

import java.io.IOException;
import java.io.StringReader;

/**
 *
 * @author tkv
 */
public class AisContentHelper
{
    private String content;
    public AisContentHelper(String nmea)
    {
        try
        {
            String ais = getAisData(nmea);
            AISReader ar = new AISReader(new StringReader(ais));
            int cc = ar.read();
            StringBuilder sb = new StringBuilder();
            while (cc != -1 && cc != '\n')
            {
                sb.append((char)cc);
                cc = ar.read();
            }
            content = sb.toString();
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    public int getBits()
    {
        return content.length();
    }
    public boolean getBoolean(int index)
    {
        return content.charAt(index) == '1';
    }
    public String getString(int begin, int end)
    {
        String sub = content.substring(begin, end);
        return AisUtil.makeString(sub);
    }
    public int getUInt(int begin, int end)
    {
        String sub = content.substring(begin, end);
        return Integer.parseInt(sub, 2);
    }
    public int getInt(int begin, int end)
    {
        int result = getUInt(begin, end);
        int l = end - begin;
        if (result < (1<<(l-1)))
        {
            return result;
        }
        else
        {
            return result + (-1<<l);
        }
    }
    public static void dump(String nmea) throws IOException
    {
        String ais = getAisData(nmea);
        AISReader ar = new AISReader(new StringReader(ais));
        int cc = ar.read();
        int count = 0;
        while (cc != -1 && cc != '\n')
        {
            System.err.println(count+": "+(char)cc);
            cc = ar.read();
            count++;
        }
    }
    /**
     * Return's ais content of nmea sentence(s) with '\\n' suffix
     * @param nmea
     * @return 
     */
    public static String getAisData(String nmea)
    {
        StringBuilder sb = new StringBuilder();
        String[] lines = nmea.split("\r\n");
        for (String line : lines)
        {
            String[] split = line.split(",");
            sb.append(split[5]);
        }
        sb.append('\n');
        return sb.toString();
    }

}
