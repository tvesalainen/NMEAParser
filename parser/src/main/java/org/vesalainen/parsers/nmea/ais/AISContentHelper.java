/*
 * Copyright (C) 2014 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.util.Objects;
import org.vesalainen.parsers.nmea.ais.AISUtil;
import org.vesalainen.parsers.nmea.ais.AISUtil;
import org.vesalainen.parsers.nmea.ais.AISUtil;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AISContentHelper
{
    private final String content;
    public AISContentHelper(String nmea)
    {
        content = getAisData(nmea);
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
        return AISUtil.makeString(sub);
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

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.content);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final AISContentHelper other = (AISContentHelper) obj;
        if (!Objects.equals(this.content, other.content))
        {
            return false;
        }
        return true;
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
            sb.append(getAisBinary(split[5]));
            int pad = split[6].charAt(0)-'0';
            sb.setLength(sb.length()-pad);
        }
        return sb.toString();
    }
    public static String getAisBinary(String str)
    {
        StringBuilder sb = new StringBuilder();
        for (int ii=0;ii<str.length();ii++)
        {
            int cc = str.charAt(ii);
            if ((cc >= '0' && cc <= 'W') || (cc >= '`' && cc <= 'w'))
            {
                cc -= '0';
                if (cc > 40)
                {
                    cc -= 8;
                }
            }
            else
            {
                throw new IllegalArgumentException(str);
            }
            for (int bit=5;bit>=0;bit--)
            {
                if ((cc & (1<<bit)) == 0)
                {
                    sb.append('0');
                }
                else
                {
                    sb.append('1');
                }
            }
        }
        return sb.toString();
    }
    public int getContentLength()
    {
        return content.length();
    }
    public String dumpLen(int... lengths)
    {
        StringBuilder sb = new StringBuilder();
        int length = lengths.length;
        int index = 0;
        for (int ii=0;ii<length;ii++)
        {
            sb.append(index).append('[').append(lengths[ii]).append(']').append(": ");
            if (ii < length-1)
            {
                if (content.length() >= index+lengths[ii])
                {
                    sb.append(content.substring(index, index+lengths[ii]));
                }
                else
                {
                    sb.append("...");
                }
            }
            else
            {
                if (content.length() >= index)
                {
                    sb.append(content.substring(index));
                }
                else
                {
                    sb.append("...");
                }
            }
            sb.append('\n');
            index += lengths[ii];
        }
        return sb.toString();
    }
    public String dumpInd(int... indeces)
    {
        StringBuilder sb = new StringBuilder();
        int length = indeces.length;
        for (int ii=0;ii<length;ii++)
        {
            sb.append(indeces[ii]).append(": ");
            if (ii < length-1)
            {
                sb.append(content.substring(indeces[ii], indeces[ii+1]));
            }
            else
            {
                sb.append(content.substring(indeces[ii]));
            }
            sb.append('\n');
        }
        return sb.toString();
    }
    
}
