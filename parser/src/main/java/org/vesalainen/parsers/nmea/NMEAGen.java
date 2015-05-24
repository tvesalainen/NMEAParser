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
package org.vesalainen.parsers.nmea;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import static org.vesalainen.parsers.nmea.Converter.Celcius;
import static org.vesalainen.parsers.nmea.Converter.Fath;
import static org.vesalainen.parsers.nmea.Converter.Ft;
import static org.vesalainen.parsers.nmea.Converter.KMH;
import static org.vesalainen.parsers.nmea.Converter.Kts;
import static org.vesalainen.parsers.nmea.Converter.M;
import static org.vesalainen.parsers.nmea.Converter.toMeters;
import org.vesalainen.util.navi.Knots;
import org.vesalainen.util.navi.Meters;

/**
 *
 * @author tkv
 */
public class NMEAGen
{
    /**
     * Calculates the checksum for nmea sentence. Sentence starts at 0 and
     * ends in position. 
     * @param bb 
     * @return 
     */
    public static final int checkSum(ByteBuffer bb)
    {
        boolean on = false;
        int value = 0;
        int limit = bb.limit();
        bb.flip();
        while (bb.hasRemaining())
        {
            int b = bb.get();
            if (b == '*')
            {
                on = false;
            }
            if (on)
            {
                value ^= b;
            }
            if (b == '$' || b == '!')
            {
                value = 0;
                on = true;
            }
        }
        bb.limit(limit);
        return value;
    }

    public static void dbt(String talkerId, ByteBuffer bb, float depth)
    {
        put(bb, '$');
        put(bb, talkerId);
        put(bb, "DBT,");
        float meters = toMeters(depth, Ft);
        put(bb, Meters.toFeets(meters));
        put(bb, ',');
        put(bb, Ft);
        put(bb, ',');
        put(bb, meters);
        put(bb, ',');
        put(bb, M);
        put(bb, ',');
        put(bb, Meters.toFathoms(meters));
        put(bb, ',');
        put(bb, Fath);
        putChecksum(bb);
        put(bb, "\r\n");
    }
    public static void vhw(String talkerId, ByteBuffer bb, float knots)
    {
        put(bb, '$');
        put(bb, talkerId);
        put(bb, "VHW,,,,,");
        put(bb, knots);
        put(bb, ',');
        put(bb, Kts);
        put(bb, ',');
        put(bb, Knots.toKiloMetersInHour(knots));
        put(bb, ',');
        put(bb, KMH);
        putChecksum(bb);
        put(bb, "\r\n");
    }

    public static void mtw(String talkerId, ByteBuffer bb, float c)
    {
        put(bb, '$');
        put(bb, talkerId);
        put(bb, "MTW,");
        put(bb, c);
        put(bb, ',');
        put(bb, Celcius);
        putChecksum(bb);
        put(bb, "\r\n");
    }
    public static void txt(String talkerId, ByteBuffer bb, String msg)
    {
        if (msg.indexOf(',') != -1 || msg.indexOf('*') != -1)
        {
            throw new IllegalArgumentException(msg+" contains (,) or (*)");
        }
        put(bb, '$');
        put(bb, talkerId);
        put(bb, "TXT,1,1,,");
        put(bb, msg);
        putChecksum(bb);
        put(bb, "\r\n");
    }

    private static void putChecksum(ByteBuffer bb)
    {
        put(bb, '*');
        int cs = checkSum(bb);
        put(bb, String.format(Locale.US, "%02X", cs));
    }
    private static void put(ByteBuffer bb, double d)
    {
        put(bb, String.format(Locale.US, "%.1f", d));
    }
    private static void put(ByteBuffer bb, String s)
    {
        bb.put(s.getBytes(StandardCharsets.US_ASCII));
    }
    private static void put(ByteBuffer bb, char c)
    {
        bb.put((byte)c);
    }

}
