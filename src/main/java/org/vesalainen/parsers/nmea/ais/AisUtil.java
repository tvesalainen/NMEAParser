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

import org.vesalainen.parser.util.InputReader;

/**
 *
 * @author tkv
 */
public class AisUtil
{
    public static String makeString(CharSequence seq)
    {
        StringBuilder sb = new StringBuilder();
        int length = seq.length();
        length = 6*(length / 6);    // force to 6 bit
        int bit = 0;
        int cc = 0;
        for (int ii = 0; ii < length; ii++)
        {
            bit++;
            cc <<= 1;
            cc += seq.charAt(ii) - '0';
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
}
