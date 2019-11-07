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
package org.vesalainen.parsers.nmea.ais;

import org.vesalainen.lang.Primitives;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Area
{
    public static Area getInstance(CharSequence seq)
    {
        if (seq.length() != 90)
        {
            throw new IllegalArgumentException("length should be 90");
        }
        int shape = Primitives.parseInt(seq, 2, 0, 3);
        switch (shape)
        {
            case 0:
                return new CircleArea(seq);
            case 1:
                return new RectangleArea(seq);
            case 2:
                return new SectorArea(seq);
            case 3:
            case 4:
                return new PolylineArea(seq);
            case 5:
                return new AssociatedText(seq);
            default:
                throw new UnsupportedOperationException(shape+" not supported");
        }
    }

    public static class CircleArea extends Area
    {

        private final int scale;
        private final double longitude;
        private final double latitude;
        private final int precision;
        private final int radius;

        public CircleArea(CharSequence seq)
        {
            scale = Primitives.parseInt(seq, 2, 3, 5);
            longitude = Primitives.parseInt(seq, -2, 5, 33)/600000.0;
            latitude = Primitives.parseInt(seq, -2, 33, 60)/600000.0;
            precision = Primitives.parseInt(seq, 2, 60, 63);
            radius = (int) (Primitives.parseInt(seq, 2, 63, 75)*Math.pow(10, scale));
        }

        public double getLongitude()
        {
            return longitude;
        }

        public double getLatitude()
        {
            return latitude;
        }

        public int getRadius()
        {
            return radius;
        }
        
    }

    public static class RectangleArea extends Area
    {

        private final int scale;
        private final double longitude;
        private final double latitude;
        private final int east;
        private final int north;
        private final int orientation;

        public RectangleArea(CharSequence seq)
        {
            scale = Primitives.parseInt(seq, 2, 3, 5);
            longitude = Primitives.parseInt(seq, -2, 5, 33)/600000.0;
            latitude = Primitives.parseInt(seq, -2, 33, 60)/600000.0;
            east = (int) (Primitives.parseInt(seq, 2, 60, 68)*Math.pow(10, scale));
            north = (int) (Primitives.parseInt(seq, 2, 68, 76)*Math.pow(10, scale));
            orientation = Primitives.parseInt(seq, 2, 76, 85);
        }

        public double getLongitude()
        {
            return longitude;
        }

        public double getLatitude()
        {
            return latitude;
        }

        public int getEast()
        {
            return east;
        }

        public int getNorth()
        {
            return north;
        }

        public int getOrientation()
        {
            return orientation;
        }
        
    }
    public static class SectorArea extends Area
    {

        private final int scale;
        private final double longitude;
        private final double latitude;
        private final int radius;
        private final int left;
        private final int right;

        public SectorArea(CharSequence seq)
        {
            scale = Primitives.parseInt(seq, 2, 3, 5);
            longitude = Primitives.parseInt(seq, -2, 5, 33)/600000.0;
            latitude = Primitives.parseInt(seq, -2, 33, 60)/600000.0;
            radius = (int) (Primitives.parseInt(seq, 2, 60, 72)*Math.pow(10, scale));
            left = Primitives.parseInt(seq, 2, 72, 81);
            right = Primitives.parseInt(seq, 2, 81, 90);
        }

        public double getLongitude()
        {
            return longitude;
        }

        public double getLatitude()
        {
            return latitude;
        }

        public int getRadius()
        {
            return radius;
        }

        public int getLeft()
        {
            return left;
        }

        public int getRight()
        {
            return right;
        }
        
    }

    public static class PolylineArea extends Area
    {
        private final int scale;
        private final int angle1;
        private final int distance1;
        private final int angle2;
        private final int distance2;
        private final int angle3;
        private final int distance3;
        private final int angle4;
        private final int distance4;

        public PolylineArea(CharSequence seq)
        {
            scale = Primitives.parseInt(seq, 2, 3, 5);
            angle1 = Primitives.parseInt(seq, 2, 5, 15);
            distance1 = (int) (Primitives.parseInt(seq, 2, 15, 26)*Math.pow(10, scale));
            angle2 = Primitives.parseInt(seq, 2, 26, 36);
            distance2 = (int) (Primitives.parseInt(seq, 2, 36, 47)*Math.pow(10, scale));
            angle3 = Primitives.parseInt(seq, 2, 47, 57);
            distance3 = (int) (Primitives.parseInt(seq, 2, 57, 68)*Math.pow(10, scale));
            angle4 = Primitives.parseInt(seq, 2, 68, 78);
            distance4 = (int) (Primitives.parseInt(seq, 2, 78, 89)*Math.pow(10, scale));
        }

        public int getScale()
        {
            return scale;
        }

        public int getAngle1()
        {
            return angle1;
        }

        public int getDistance1()
        {
            return distance1;
        }

        public int getAngle2()
        {
            return angle2;
        }

        public int getDistance2()
        {
            return distance2;
        }

        public int getAngle3()
        {
            return angle3;
        }

        public int getDistance3()
        {
            return distance3;
        }

        public int getAngle4()
        {
            return angle4;
        }

        public int getDistance4()
        {
            return distance4;
        }
        
    }

    public static class AssociatedText extends Area
    {

        private final String text;

        public AssociatedText(CharSequence seq)
        {
            text = AISUtil.makeString(seq, 3, 87);
        }

        public String getText()
        {
            return text;
        }
        
    }

}
