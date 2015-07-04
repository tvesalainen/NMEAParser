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

import java.util.Objects;

/**
 *
 * @author tkv
 */
public class NMEAPrefix
{
    private final String prefix;
    private boolean strict;

    public NMEAPrefix(String prefix)
    {
        this.prefix = prefix;
        this.strict = prefix.indexOf('?') == -1;
    }

    public String getPrefix()
    {
        return prefix;
    }

    public boolean isStrict()
    {
        return strict;
    }

    @Override
    public String toString()
    {
        return "NMEAPrefix{" + prefix + '}';
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.prefix);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final NMEAPrefix other = (NMEAPrefix) obj;
        if (!Objects.equals(this.prefix, other.prefix))
        {
            return false;
        }
        return true;
    }
    
    public static boolean matchesSame(String p1, String p2)
    {
        int len = Math.min(p1.length(), p2.length());
        for (int ii=0;ii<len;ii++)
        {
            char c1 = p1.charAt(ii);
            char c2 = p2.charAt(ii);
            if (!((c1=='?' || c2=='?') || c1 == c2))
            {
                return false;
            }
        }
        return true;
    }

    
}
