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

package org.vesalainen.parsers.mmsi;

import java.util.Objects;

/**
 *
 * @author Timo Vesalainen
 */
public class MMSIEntry
{
    private MMSIType type;
    private MIDEntry mid;
    private int a;
    private int x;
    private int y;
    private int z;

    public MMSIEntry(MMSIType type, MIDEntry mid)
    {
        this.type = type;
        this.mid = mid;
    }

    public MMSIEntry(MMSIType type, int x)
    {
        this.type = type;
        this.x = x;
    }

    public MMSIEntry(MMSIType type, MIDEntry mid, int x)
    {
        this.type = type;
        this.mid = mid;
        this.x = x;
    }

    public MMSIEntry(MMSIType type, MIDEntry mid, int a, int x)
    {
        this.type = type;
        this.mid = mid;
        this.a = a;
        this.x = x;
    }

    public MMSIEntry(MMSIType type, int y, int z)
    {
        this.type = type;
        this.y = y;
        this.z = z;
    }

    public MMSIType getType()
    {
        return type;
    }

    public MIDEntry getMid()
    {
        return mid;
    }

    public int getA()
    {
        return a;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public int getZ()
    {
        return z;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 13 * hash + Objects.hashCode(this.type);
        hash = 13 * hash + Objects.hashCode(this.mid);
        hash = 13 * hash + this.a;
        hash = 13 * hash + this.x;
        hash = 13 * hash + this.y;
        hash = 13 * hash + this.z;
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
        final MMSIEntry other = (MMSIEntry) obj;
        if (this.type != other.type)
        {
            return false;
        }
        if (!Objects.equals(this.mid, other.mid))
        {
            return false;
        }
        if (this.a != other.a)
        {
            return false;
        }
        if (this.x != other.x)
        {
            return false;
        }
        if (this.y != other.y)
        {
            return false;
        }
        if (this.z != other.z)
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        switch (type)
        {
            case ShipStation:
                return String.format("%d%06d", mid.getMid(), x);
            case GroupShipStation:
                return String.format("0%d%05d", mid.getMid(), x);
            case CoastStation:
                return String.format("00%d%04d", mid.getMid(), x);
            case SarAircraft:
                return String.format("111%d%d%02d", mid.getMid(), a, x);
            case HandheldVHF:
                return String.format("8%08d", x);
            case SearchAndRescueTransponder:
                return String.format("970%02d%04d", y, z);
            case MobDevice:
                return String.format("972%02d%04d", y, z);
            case EPIRB:
                return String.format("974%02d%04d", y, z);
            case CraftAssociatedWithParentShip:
                return String.format("98%d%04d", mid.getMid(), x);
            case NavigationalAid:
                return String.format("99%d%d%03d", mid.getMid(), a, x);
            default:
                throw new IllegalArgumentException("unknown mmsi");
        }
    }

}
