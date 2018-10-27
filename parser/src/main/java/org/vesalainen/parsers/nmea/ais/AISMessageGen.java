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
    public static NMEASentence[] msg1(CacheEntry entry)
    {
        return new Bldr(PositionReportClassA, entry.getProperties())
                .integer(4, NavigationStatus.class, "navigationStatus")
                .rot()
                .decimal(10, 10, "speed")
                .bool("positionAccuracy")
                .decimal(28, 600000, "longitude")
                .decimal(27, 600000, "latitude")
                .decimal(12, 10, "course")
                .integer(9, "heading")
                .integer(6, "second")
                .integer(2, ManeuverIndicator.class, "maneuver")
                .spare(3)
                .bool("raim")
                .integer(19, "radioStatus")
                .build();
    }
    public static NMEASentence[] msg5(CacheEntry entry)
    {
        return new Bldr(StaticAndVoyageRelatedData, entry.getProperties())
                .integer(2, "aisVersion")
                .integer(30, "imoNumber")
                .string(42, "callSign")
                .string(120, "vesselName")
                .integer(8, CodesForShipType.class, "shipType")
                .dimensions()
                .integer(4, EPFDFixTypes.class, "epfd")
                .integer(4, "etaMonth")
                .integer(5, "etaDay")
                .integer(5, "etaHour")
                .integer(6, "etaMinute")
                .decimal(8, 10, "draught")
                .string(120, "destination")
                .bool("dte")
                .spare(1)
                .build();
    }
    public static NMEASentence[] msg18(CacheEntry entry)
    {
        return new Bldr(StandardClassBCSPositionReport, entry.getProperties())
                .spare(8)
                .decimal(10, 10, "speed")
                .bool("positionAccuracy")
                .decimal(28, 600000, "longitude")
                .decimal(27, 600000, "latitude")
                .decimal(12, 10, "course")
                .integer(9, "heading")
                .integer(6, "second")
                .spare(2)
                .bool("csUnit")
                .bool("display")
                .bool("dsc")
                .bool("band")
                .bool("msg22")
                .bool("assignedMode")
                .bool("raim")
                .integer(20, "radioStatus")
                .build();
    }
    public static NMEASentence[] msg24A(CacheEntry entry)
    {
        return new Bldr(StaticDataReport, entry.getProperties())
                .integer(2, 0)
                .string(120, "vesselName")
                .spare(8)
                .build();
    }
    public static NMEASentence[] msg24B(CacheEntry entry)
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
        return bldr.build();
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

        public NMEASentence[] build()
        {
            return builder.build();
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
        private Bldr rot()
        {
            String prop = getProperty("rateOfTurn");
            float value = Float.parseFloat(prop);
            builder.rot(value);
            return this;
        }
        private Bldr decimal(int bits, float coef, String property)
        {
            String prop = getProperty(property);
            float value = Float.parseFloat(prop);
            builder.decimal(bits, value, coef);
            return this;
        }
        private Bldr integer(int bits, String property)
        {
            String prop = getProperty(property);
            int value = Integer.parseInt(prop);
            builder.integer(bits, value);
            return this;
        }
        private Bldr bool(String property)
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
