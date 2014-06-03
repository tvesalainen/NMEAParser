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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A storage for ISO3166 data. Data is populated from resource 
 * org/vesalainen/parsers/mmsi/ISO3166.txt
 * 
 * @author Timo Vesalainen
 */
public final class ISO3166
{
    private static final Map<String,ISO3166Entry> alpha2Map = new HashMap<>();
    private static final Map<String,ISO3166Entry> alpha3Map = new HashMap<>();
    private static final Map<Integer,ISO3166Entry> numericMap = new HashMap<>();
    static
    {
        populate();
    }
    private static void populate()
    {
        try (
            InputStream is = ISO3166.class.getClassLoader().getResourceAsStream(ISO3166.class.getCanonicalName().replace('.', '/')+".txt");
            InputStreamReader isr = new InputStreamReader(is, "utf-8");
            LineNumberReader lnr = new LineNumberReader(isr);
                )
        {
            String englishShortName = lnr.readLine();
            while (englishShortName != null)
            {
                String frenchShortName = lnr.readLine();
                String alpha2Code = lnr.readLine();
                String alpha3Code = lnr.readLine();
                String num = lnr.readLine();
                int numeric = Integer.parseInt(num);
                add(englishShortName, frenchShortName, alpha2Code, alpha3Code, numeric);
                englishShortName = lnr.readLine();
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
    private static void add(
        String englishShortName,
        String frenchShortName,
        String alpha2Code,
        String alpha3Code,
        int numeric
    )
    {
        ISO3166Entry entry = new ISO3166Entry(englishShortName, frenchShortName, alpha2Code, alpha3Code, numeric);
        alpha2Map.put(alpha2Code, entry);
        alpha3Map.put(alpha3Code, entry);
        numericMap.put(numeric, entry);
    }
    /**
     * Returns all entries
     * @return 
     */
    public static Collection<ISO3166Entry> getAllEntries()
    {
        return alpha2Map.values();
    }
    /**
     * Return entries that match pattern
     * @param pattern Regular expression pattern
     * @return 
     */
    public static Collection<ISO3166Entry> getEntries(Pattern pattern)
    {
        Set<ISO3166Entry> set = new HashSet<>();
        for (ISO3166Entry e : alpha2Map.values())
        {
            Matcher matcher = pattern.matcher(e.getEnglishShortName());
            if (matcher.matches())
            {
                set.add(e);
            }
        }
        return set;
    }
    /**
     * Returns entry for 2 letter code
     * @param alpha2
     * @return 
     */
    public static ISO3166Entry getForAlpha2(String alpha2)
    {
        return alpha2Map.get(alpha2);
    }
    /**
     * Returns entry for 3 letter code
     * @param alpha3
     * @return 
     */
    public static ISO3166Entry getForAlpha3(String alpha3)
    {
        return alpha3Map.get(alpha3);
    }
    /**
     * Returns entry for numeric code
     * @param numeric
     * @return 
     */
    public static ISO3166Entry getForNumeric(int numeric)
    {
        return numericMap.get(numeric);
    }
}
