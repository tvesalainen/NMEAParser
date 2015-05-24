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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A storage for MMSI MID's
 * @author Timo Vesalainen
 */
public final class MID
{
    private static final Map<Integer,MIDEntry> map = new HashMap<>();
    static
    {
        populate();
    }
    private static void populate()
    {
        try (
            InputStream is = MID.class.getClassLoader().getResourceAsStream(MID.class.getCanonicalName().replace('.', '/')+".txt");
            InputStreamReader isr = new InputStreamReader(is, "utf-8");
            LineNumberReader lnr = new LineNumberReader(isr)
                )
        {
           String midString = lnr.readLine();
            while (midString != null)
            {
                String country = lnr.readLine();
                String alpha2Code = lnr.readLine();
                ISO3166Entry iso3166Entry = null;
                if (!alpha2Code.isEmpty())
                {
                    iso3166Entry = ISO3166.getForAlpha2(alpha2Code);
                }
                int mid = Integer.parseInt(midString);
                add(mid, country, iso3166Entry);
                midString = lnr.readLine();
            }
        }
        catch (UnsupportedEncodingException ex)
        {
            throw new IllegalArgumentException(ex);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    private static void add(int mid, String country, ISO3166Entry iso3166Entry)
    {
        MIDEntry entry = new MIDEntry(mid, country, iso3166Entry);
        map.put(mid, entry);
    }
    /**
     * Returns all entries
     * @return 
     */
    public static final Collection<MIDEntry> getAllEntries()
    {
        return map.values();
    }
    /**
     * Returns entry for MID code
     * @param mid
     * @return 
     */
    public static final MIDEntry get(int mid)
    {
        return map.get(mid);
    }
}
