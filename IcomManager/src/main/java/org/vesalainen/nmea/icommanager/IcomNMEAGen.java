/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.nmea.icommanager;

import java.io.IOException;
import java.util.Locale;
import java.util.zip.CheckedOutputStream;
import org.vesalainen.parsers.nmea.NMEAGen;

/**
 *
 * @author tkv
 */
public class IcomNMEAGen extends NMEAGen
{
    public static void all(CheckedOutputStream out, int id) throws IOException
    {
        put(out, "$PICOA,90,");
        put(out, String.format(Locale.US, "%02d", id));
        put(out, ",ALL");
        putChecksum(out);
        put(out, "\r\n");
    }

    public static void set(CheckedOutputStream out, int id, String key, String value) throws IOException
    {
        put(out, "$PICOA,90,");
        put(out, String.format(Locale.US, "%02d", id));
        put(out, ",");
        put(out, key);
        put(out, ",");
        put(out, value);
        putChecksum(out);
        put(out, "\r\n");
    }

}
