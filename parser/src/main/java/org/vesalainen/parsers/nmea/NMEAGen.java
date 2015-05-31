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

import java.io.IOException;
import java.util.zip.CheckedOutputStream;
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
    public static void dbt(String talkerId, CheckedOutputStream out, float depth) throws IOException
    {
        put(out, '$');
        put(out, talkerId);
        put(out, "DBT,");
        float meters = toMeters(depth, Ft);
        put(out, Meters.toFeets(meters));
        put(out, ',');
        put(out, Ft);
        put(out, ',');
        put(out, meters);
        put(out, ',');
        put(out, M);
        put(out, ',');
        put(out, Meters.toFathoms(meters));
        put(out, ',');
        put(out, Fath);
        putChecksum(out);
        put(out, "\r\n");
    }
    public static void vhw(String talkerId, CheckedOutputStream out, float knots) throws IOException
    {
        put(out, '$');
        put(out, talkerId);
        put(out, "VHW,,,,,");
        put(out, knots);
        put(out, ',');
        put(out, Kts);
        put(out, ',');
        put(out, Knots.toKiloMetersInHour(knots));
        put(out, ',');
        put(out, KMH);
        putChecksum(out);
        put(out, "\r\n");
    }

    public static void mtw(String talkerId, CheckedOutputStream out, float c) throws IOException
    {
        put(out, '$');
        put(out, talkerId);
        put(out, "MTW,");
        put(out, c);
        put(out, ',');
        put(out, Celcius);
        putChecksum(out);
        put(out, "\r\n");
    }
    public static void txt(String talkerId, CheckedOutputStream out, String msg) throws IOException
    {
        if (msg.indexOf(',') != -1 || msg.indexOf('*') != -1)
        {
            throw new IllegalArgumentException(msg+" contains (,) or (*)");
        }
        put(out, '$');
        put(out, talkerId);
        put(out, "TXT,1,1,,");
        put(out, msg);
        putChecksum(out);
        put(out, "\r\n");
    }

    private static void putChecksum(CheckedOutputStream out) throws IOException
    {
        put(out, '*');
        int cs = (int) out.getChecksum().getValue();
        put(out, String.format(Locale.US, "%02X", cs));
    }
    private static void put(CheckedOutputStream out, double d) throws IOException
    {
        put(out, String.format(Locale.US, "%.1f", d));
    }
    private static void put(CheckedOutputStream out, String s) throws IOException
    {
        out.write(s.getBytes(StandardCharsets.US_ASCII));
    }
    private static void put(CheckedOutputStream out, char c) throws IOException
    {
        out.write((byte)c);
    }

}
