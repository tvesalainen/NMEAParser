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

import static java.lang.Math.abs;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class AISUtil
{
    /**
    /**
     * <p> Turn rate is encoded as follows: <p> 0 = not turning 
     * <p> 1…126 = turning right at up to 708 degrees per minute or higher 
     * <p> 1…-126 = turning left at up to 708 degrees per minute or higher 
     * <p> 127 = turning right at more than 5deg/30s (No TI available) 
     * <p> -127 = turning left at more than 5deg/30s (No TI available) 
     * <p> 128 (80 hex) indicates no turn information available (default) 
     * <p>Values between 0 and 708 degrees/min coded by 
     * ROTAIS=4.733 * SQRT(ROTsensor) degrees/min where ROTsensor is
     * the Rate of Turn as input by an external Rate of Turn Indicator. ROTAIS
     * is rounded to the nearest integer value. Thus, to decode the field value,
     * divide by 4.733 and then square it. Sign of the field value should be
     * preserved when squaring it, otherwise the left/right indication will be
     * lost.
     * @param turn
     * @return 
     */
    public static float rot(int turn)
    {
        switch (turn)
        {
            case 0:
                return 0;
            case 127:
                return 10;
            case -127:
                return -10;
            case -128:  // 0x80
                return Float.NaN;
            default:
                float f = turn;
                f = f / 4.733F;
                return Math.signum(f) * f * f;
        }
    }
    public static int rot(float turn)
    {
        if (Float.isNaN(turn) || abs(turn) > 708.7092F)
        {
            return -128;
        }
        if (turn == 0)
        {
            return 0;
        }
        if (turn == 10)
        {
            return 127;
        }
        if (turn == -10)
        {
            return -127;
        }
        return (int) Math.round(Math.signum(turn)*Math.sqrt(Math.abs(turn))*4.733F);
    }
    public static String makeString(CharSequence bin)
    {
        return makeString(bin, 0, bin.length());
    }
    public static String makeString(CharSequence bin, int offset, int length)
    {
        StringBuilder sb = new StringBuilder();
        length = 6*(length / 6);    // force to 6 bit
        int bit = 0;
        int cc = 0;
        for (int ii = offset; ii < length; ii++)
        {
            bit++;
            cc <<= 1;
            cc += bin.charAt(ii) - '0';
            if (bit == 6)
            {
                if (cc == 0)    // terminating '@'
                {
                    break;
                }
                if (cc < 32)
                {
                    sb.append((char) (cc + '@'));
                }
                else
                {
                    sb.append((char) cc);
                }
                bit = 0;
                cc = 0;
            }
        }
        return sb.toString().trim();
    }
    public static byte[] makeArray(byte[] txt)
    {
        int length = txt.length;
        int l = 6*length;
        int mod6 = l%8;
        if (mod6 != 0)
        {
            throw new IllegalArgumentException();
        }
        byte[] array = new byte[l/8];
        int index = 0;
        int res = 0;
        int bits = 0;
        int b6 = 0;
        for (int ii=0;ii<length;ii++)
        {
            byte cc = txt[ii];
            if (cc > 64 && cc <= 95)
            {
                b6 = cc - 64;
            }
            else
            {
                if (cc >= 32 && cc <= 63)
                {
                    b6 = cc;
                }
                else
                {
                    throw new IllegalArgumentException(cc+" cannot be encoded in "+txt);
                }
            }
            res <<= 6;
            res |= b6;
            bits += 6;
            if (bits >= 8)
            {
                int sht = bits-8;
                if (sht > 0)
                {
                    array[index++] = (byte) (res>>>(sht));
                    res &= (-1>>>(32-sht));
                }
                else
                {
                    array[index++] = (byte) res;
                    res = 0;
                }
                bits -= 8;
            }
        }
        return array;
    }
}