/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nmea.processor.deviation;

import org.vesalainen.lang.Primitives;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class Accumulator
{
    private double weight;
    private double sumX;
    private double sumY;

    public Accumulator(String line)
    {
        String[] split = line.split(" ");
        switch (split.length)
        {
            case 2:
                this.sumX = Primitives.parseDouble(split[0]);
                this.sumY = Primitives.parseDouble(split[1]);
                this.weight = 1;
                break;
            case 3:
                this.sumX = Primitives.parseDouble(split[0]);
                this.sumY = Primitives.parseDouble(split[1]);
                this.weight = Primitives.parseDouble(split[2]);
                break;
            default:
                throw new IllegalArgumentException(line);
        }
    }

    public Accumulator()
    {
    }

    public Accumulator(double sumX, double sumY, double weight)
    {
        this.weight = weight;
        this.sumX = sumX;
        this.sumY = sumY;
    }

    public void add(double x, double y)
    {
        add(x, y, 1);
    }
    public void add(double x, double y, double weight)
    {
        if (!Double.isFinite(weight))
        {
            throw new IllegalArgumentException("weight is not finite");
        }
        this.sumX += x*weight;
        this.sumY += y*weight;
        this.weight += weight;
    }
    
    public double getX()
    {
        return sumX / weight;
    }
    
    public double getY()
    {
        return sumY / weight;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 17 * hash + (int) (Double.doubleToLongBits(this.weight) ^ (Double.doubleToLongBits(this.weight) >>> 32));
        hash = 17 * hash + (int) (Double.doubleToLongBits(this.sumX) ^ (Double.doubleToLongBits(this.sumX) >>> 32));
        hash = 17 * hash + (int) (Double.doubleToLongBits(this.sumY) ^ (Double.doubleToLongBits(this.sumY) >>> 32));
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
        final Accumulator other = (Accumulator) obj;
        if (Double.doubleToLongBits(this.weight) != Double.doubleToLongBits(other.weight))
        {
            return false;
        }
        if (Double.doubleToLongBits(this.sumX) != Double.doubleToLongBits(other.sumX))
        {
            return false;
        }
        if (Double.doubleToLongBits(this.sumY) != Double.doubleToLongBits(other.sumY))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return sumX + " " + sumY + " " + weight;
    }
    
}
