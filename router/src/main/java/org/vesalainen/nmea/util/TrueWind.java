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
package org.vesalainen.nmea.util;

/**
 *
 * @author tkv
 */
public class TrueWind
{
    private double boatSpeed;   // kts
    private double relativeAngle;   // radians
    private double relativeSpeed;   // Kts
    private double trueAngle;   // radians
    private double trueSpeed;   // Kts

    public void calc()
    {
        double x = Math.cos(relativeAngle)*relativeSpeed - boatSpeed;
        double y = Math.sin(relativeAngle)*relativeSpeed;
        trueSpeed = Math.hypot(x, y);
        trueAngle = Math.toDegrees(Math.atan2(y, x));
        if (trueAngle < 0)
        {
            trueAngle += 360.0;
        }
    }
    /**
     * 
     * @return Deg
     */
    public double getTrueAngle()
    {
        return trueAngle;
    }
    /**
     * 
     * @return Kts
     */
    public double getTrueSpeed()
    {
        return trueSpeed;
    }
    /**
     * 
     * @param boatSpeed Kts
     */
    public void setBoatSpeed(double boatSpeed)
    {
        this.boatSpeed = boatSpeed;
    }
    /**
     * 
     * @param degrees Degrees
     */
    public void setRelativeAngle(double degrees)
    {
        this.relativeAngle = Math.toRadians(degrees);
    }
    /**
     * 
     * @param relativeSpeed Kts
     */
    public void setRelativeSpeed(double relativeSpeed)
    {
        this.relativeSpeed = relativeSpeed;
    }

    @Override
    public String toString()
    {
        return "TrueWind{" + "boatSpeed=" + boatSpeed + ", relativeAngle=" + relativeAngle + ", relativeSpeed=" + relativeSpeed + ", trueAngle=" + trueAngle + ", trueSpeed=" + trueSpeed + '}';
    }
    
}
