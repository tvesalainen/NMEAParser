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
import org.vesalainen.parsers.nmea.ais.AISMonitor.CacheEntry;
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
                .integer(4, NavigationStatus.class, "navigationStatus", NavigationStatus.NotDefinedDefault)
                .rot()
                .decimal(10, 10, "speed", 1023)
                .bool("positionAccuracy", false)
                .decimal(28, 600000, "longitude", 181)
                .decimal(27, 600000, "latitude", 91)
                .decimal(12, 10, "course", 360)
                .integer(9, "heading", 511)
                .integer(6, "second", 60)
                .integer(2, ManeuverIndicator.class, "maneuver", ManeuverIndicator.NotAvailableDefault)
                .spare(3)
                .bool("raim", false)
                .integer(19, "radioStatus", 0)
                .build();
    }
    public static NMEASentence[] msg5(CacheEntry entry)
    {
        return new Bldr(StaticAndVoyageRelatedData, entry.getProperties())
                .integer(2, "aisVersion", 0)
                .integer(30, "imoNumber")
                .string(42, "callSign")
                .string(120, "vesselName")
                .integer(8, CodesForShipType.class, "shipType", CodesForShipType.NotAvailableDefault)
                .dimensions()
                .integer(4, EPFDFixTypes.class, "epfd", EPFDFixTypes.UndefinedDefault)
                .integer(4, "etaMonth", 0)
                .integer(5, "etaDay", 0)
                .integer(5, "etaHour", 24)
                .integer(6, "etaMinute", 60)
                .decimal(8, 10, "draught")
                .string(120, "destination")
                .bool("dte", true)
                .spare(1)
                .build();
    }
    public static NMEASentence[] msg18(CacheEntry entry)
    {
        return new Bldr(StandardClassBCSPositionReport, entry.getProperties())
                .spare(8)
                .decimal(10, 10, "speed", 1023)
                .bool("positionAccuracy", false)
                .decimal(28, 600000, "longitude", 181)
                .decimal(27, 600000, "latitude", 91)
                .decimal(12, 10, "course", 360)
                .integer(9, "heading", 511)
                .integer(6, "second", 60)
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
                .integer(8, CodesForShipType.class, "shipType", CodesForShipType.NotAvailableDefault)
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
            integer(9, Integer.parseInt(properties.getProperty("dimensionToBow", "0")));
            integer(9, Integer.parseInt(properties.getProperty("dimensionToStern", "0")));
            integer(6, Integer.parseInt(properties.getProperty("dimensionToPort", "0")));
            integer(6, Integer.parseInt(properties.getProperty("dimensionToStarboard", "0")));
            return this;
        }
        private Bldr integer(int bits, Class<? extends Enum> ecls, String property, Enum defaultValue)
        {
            String prop = properties.getProperty(property, defaultValue.name());
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
            String prop = properties.getProperty("rateOfTurn", "Nan");
            float value = !prop.equals("Nan") ? Float.parseFloat(prop) : Float.NaN;
            builder.rot(value);
            return this;
        }
        private Bldr decimal(int bits, float coef, String property)
        {
            check(property);
            return decimal(bits, coef, property, Float.NaN);
        }
        private Bldr decimal(int bits, float coef, String property, float defaultValue)
        {
            String prop = properties.getProperty(property, Float.toString(defaultValue));
            float value = Float.parseFloat(prop);
            builder.decimal(bits, value, coef);
            return this;
        }
        private Bldr integer(int bits, String property)
        {
            check(property);
            return integer(bits, property, -1);
        }
        private Bldr integer(int bits, String property, int defaultValue)
        {
            String prop = properties.getProperty(property, Integer.toString(defaultValue));
            int value = Integer.parseInt(prop);
            builder.integer(bits, value);
            return this;
        }
        private Bldr bool(String property)
        {
            check(property);
            return bool(property, true);
        }
        private Bldr bool(String property, boolean defaultValue)
        {
            String prop = properties.getProperty(property, Boolean.toString(defaultValue));
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
            check(property);
            return string(bits, property, null);
        }
        private Bldr string(int bits, String property, String defaultValue)
        {
            String prop = properties.getProperty(property, defaultValue);
            builder.string(bits, prop);
            return this;
        }
        private void check(String property)
        {
            if (!properties.containsKey(property))
            {
                throw new IllegalArgumentException(property+" not found");
            }
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
