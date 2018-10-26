/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.util.Properties;
import org.vesalainen.parsers.mmsi.MMSIEntry;
import org.vesalainen.parsers.mmsi.MMSIParser;
import org.vesalainen.parsers.mmsi.MMSIType;
import org.vesalainen.parsers.nmea.NMEASentence;
import org.vesalainen.parsers.nmea.ais.AISCache.CacheEntry;
import static org.vesalainen.parsers.nmea.ais.MessageTypes.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AISMessageGen
{
    public static NMEASentence msg24B(CacheEntry entry)
    {
        return new Bldr(StaticAndVoyageRelatedData, entry.getProperties())
                .integer(2, "aisVersion")
                .integer(30, "imoNumber")
                .string(42, "callSign")
                .string(120, "vesselName")
                .integer(8, CodesForShipType.class, "shipType")
                .dimensions()
                .integer(4, EPFDFixTypes.class, "epfd")
    }
    public static NMEASentence msg24A(CacheEntry entry)
    {
        return new Bldr(StaticDataReport, entry.getProperties())
                .integer(2, 0)
                .string(120, "vesselName")
                .spare(8)
                .build1();
    }
    public static NMEASentence msg24B(CacheEntry entry)
    {
        Bldr bldr = new Bldr(StaticDataReport, entry.getProperties())
                .integer(2, 1)
                .integer(8, CodesForShipType.class, "shipType")
                .string(18, "vendorId")
                .integer(4, "unitModelCode")
                .integer(20, "serialNumber")
                .string(42, "callSign");
        if (entry.getMMSIType() == MMSIType.CraftAssociatedWithParentShip)
        {
            bldr.integer(30, "mothershipMMSI");
        }
        else
        {
            bldr.dimensions();
        }
        return bldr.build1();
    }
    private static int getMMSI(Properties properties)
    {
        String mmsiString = properties.getProperty("mmsi");
        if (mmsiString == null)
        {
            throw new IllegalArgumentException(mmsiString+" not found");
        }
        return Integer.parseInt(mmsiString);
    }
    private static class Bldr
    {
        private AISBuilder builder;
        private Properties properties;

        private Bldr(MessageTypes type, Properties properties)
        {
            int mmsi = getMMSI(properties);
            this.builder = new AISBuilder(type, mmsi);
            this.properties = properties;
        }

        public NMEASentence build1()
        {
            NMEASentence[] arr = builder.build();
            if (arr.length != 1)
            {
                throw new IllegalArgumentException("generated more than 1 sentence");
            }
            return arr[0];
        }
        private Bldr dimensions()
        {
            integer(9, Integer.parseInt(getProperty("dimensionToBow")));
            integer(9, Integer.parseInt(getProperty("dimensionToStern")));
            integer(6, Integer.parseInt(getProperty("dimensionToPort")));
            integer(6, Integer.parseInt(getProperty("dimensionToStarboard")));
            return this;
        }
        private Bldr integer(int bits, Class<? extends Enum> ecls, String property)
        {
            String prop = getProperty(property);
            for (Enum e : ecls.getEnumConstants())
            {
                if (e.name().equals(prop))
                {
                    integer(bits, e.ordinal());
                    return this;
                }
            }
            throw new IllegalArgumentException(prop+" not enum constant");
        }
        private Bldr spare(int bits)
        {
            builder.spare(bits);
            return this;
        }
        private Bldr integer(int bits, String property)
        {
            String prop = getProperty(property);
            int value = Integer.parseInt(prop);
            builder.integer(bits, value);
            return this;
        }
        private Bldr bool(int bits, String property)
        {
            String prop = getProperty(property);
            switch (prop.toLowerCase())
            {
                case "true":
                    builder.bool(true);
                    break;
                case "false":
                    builder.bool(false);
                    break;
                default:
                    throw new IllegalArgumentException(prop+" not boolean");
            }
            return this;
        }
        private Bldr string(int bits, String property)
        {
            String prop = getProperty(property);
            builder.string(bits, prop);
            return this;
        }
        private String getProperty(String property)
        {
            String prop = properties.getProperty(property);
            if (prop == null)
            {
                throw new IllegalArgumentException(prop+" not found");
            }
            return prop;
        }
        public Bldr string(int bits, CharSequence txt)
        {
            builder.string(bits, txt);
            return this;
        }

        public Bldr bool(boolean b)
        {
            builder.bool(b);
            return this;
        }

        public Bldr integer(int bits, int value)
        {
            builder.integer(bits, value);
            return this;
        }
        
    }
}
