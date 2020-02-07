/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vesalainen.math.UnitType;
import static org.vesalainen.math.UnitType.*;
import static org.vesalainen.parsers.nmea.Converter.*;
import static org.vesalainen.parsers.nmea.MessageType.*;
import static org.vesalainen.parsers.nmea.TalkerId.*;
import org.vesalainen.util.CharSequences;

/**
 * A utility for creating NMEA sentences
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class NMEASentence
{
    private Bind[] buffer;
    private NMEAChecksum checksum = new NMEAChecksum();
    /**
     * Creates NMEASentence from array
     * @param buffer 
     */
    private NMEASentence(Bind[] buffer)
    {
        this.buffer = buffer;
        if (!NMEA.isNMEAOrAIS(toString()))
        {
            throw new IllegalArgumentException(toString()+" not valid");
        }
    }
    /**
     * Recommended Minimum Specific GNSS Data - only declination
     * @param declination
     * @return 
     */
    public static NMEASentence rmc(double declination)
    {
        return rmc(()->declination);
    }
    public static NMEASentence rmc(DoubleSupplier declination)
    {
        return builder(IN, RMC)
                .add()      // utc
                .add('A')   // status
                .add()      // lat
                .add()      // N / S
                .add()      // lon
                .add()      // E / W
                .add()      // sog
                .add()      // tmg
                .add()      // ddmmyy
                .bindDegrees(declination)    // Magnetic variation degrees
                .add('A')   // FAA
                .build();
    }
    /**
     * Depth of Water
     * @param meters Water depth relative to transducer, meters
     * @param offset Offset from transducer, meters positive means distance from transducer to water line negative means distance from transducer to keel
     * @param from
     * @return 
     */
    public static NMEASentence dpt(double meters, double offset, UnitType from)
    {
        return dpt(()->meters, ()->offset, from);
    }
    public static NMEASentence dpt(DoubleSupplier meters, DoubleSupplier offset, UnitType from)
    {
        return builder(SD, DPT)
                .bind(from, meters, METER)
                .bind(from, offset, METER)
                .build();
    }
    /**
     * Depth Below Transducer
     * @param depth
     * @return 
     */
    public static NMEASentence dbt(double depth, UnitType from)
    {
        return dbt(()->depth, from);
    }
    public static NMEASentence dbt(DoubleSupplier depth, UnitType from)
    {
        return builder(SD, DBT)
                .bind(from, depth, FOOT)
                .add(FT)
                .bind(from, depth, METER)
                .add(M)
                .bind(from, depth, FATHOM)
                .add(FATH)
                .build();
    }
    /**
     * Water Speed and Heading
     * @param speed
     * @return 
     */
    public static NMEASentence vhw(double speed, UnitType from)
    {
        return vhw(()->speed, from);
    }
    public static NMEASentence vhw(DoubleSupplier speed, UnitType from)
    {
        return builder(VW, VHW)
                .add().add().add().add()
                .bind(from, speed, KNOT)
                .add(KTS)
                .add().add()
                .build();
    }
    /**
     * Wind speed and Angle
     * @param windAngle
     * @param windSpeed
     * @param trueWind
     * @return 
     */
    public static NMEASentence mwv(int windAngle, double windSpeed, UnitType from, boolean trueWind)
    {
        return mwv(()->windAngle, ()->windSpeed, from, trueWind);
    }
    public static NMEASentence mwv(IntSupplier windAngle, DoubleSupplier windSpeed, UnitType from, boolean trueWind)
    {
        return builder(UP, MWV)
                .bind(windAngle)
                .add(trueWind ? 'T' : 'R')
                .bind(from, windSpeed, KNOT)
                .add(KTS)
                .add('A')
                .build();
    }
    /**
     * Water Temperature
     * @param temperature
     * @param from
     * @return 
     */
    public static NMEASentence mtw(double temperature, UnitType from)
    {
        return mtw(()->temperature, from);
    }
    public static NMEASentence mtw(DoubleSupplier temperature, UnitType from)
    {
        return builder(YC, MTW)
                .bind(from, temperature, CELSIUS)
                .add(CELCIUS)
                .build();
    }
    /**
     * 
     * @param target Target Number (0-99)
     * @param latitude Target Latitude
     * @param longitude Target Longitude
     * @param name Target name
     * @param time UTC of data
     * @param status Status (L=lost, Q=acquisition, T=tracking)
     * @param referenceTarget R= reference target; null (,,)= otherwise
     * @return 
     */
    public static NMEASentence tll(int target, double latitude, double longitude, CharSequence name, LocalTime time, char status, CharSequence referenceTarget)
    {
        return tll(()->target, ()->latitude, ()->longitude, ()->name, ()->time, ()->String.valueOf(status), ()->referenceTarget);
    }
    public static NMEASentence tll(IntSupplier target, DoubleSupplier latitude, DoubleSupplier longitude, Supplier<CharSequence> name, Supplier<LocalTime> time, Supplier<CharSequence> status, Supplier<CharSequence> referenceTarget)
    {
        return builder(II, TLL)
                .bind(target)
                .bind(latitude, longitude)
                .bindString(name)
                .bindLocalTime(time)
                .bindString(status)
                .bindString(referenceTarget)
                .build();
    }
    /**
     * Text Transmission
     * @param msg
     * @return 
     */
    public static NMEASentence txt(CharSequence msg)
    {
        return txt(()->msg);
    }
    public static NMEASentence txt(Supplier<CharSequence> msg)
    {
        return builder(U0, TXT)
                .add(1)
                .add(1)
                .add()
                .bindString(msg)
                .build();
    }
    public static NMEASentence hdm(double magneticHeading)
    {
        return hdm(()->magneticHeading);
    }
    public static NMEASentence hdm(DoubleSupplier magneticHeading)
    {
        return builder(HC, HDM)
                .bind(magneticHeading)
                .add('M')
                .build();
    }
    public static NMEASentence hdt(double trueHeading)
    {
        return hdt(()->trueHeading);
    }
    public static NMEASentence hdt(DoubleSupplier trueHeading)
    {
        return builder(HC, HDT)
                .bind(trueHeading)
                .add('T')
                .build();
    }
    public static NMEASentence hdg(double magneticHeading, double deviation, double variation)
    {
        return hdg(()->magneticHeading, ()->deviation, ()->variation);
    }
    public static NMEASentence hdg(DoubleSupplier magneticHeading, DoubleSupplier deviation, DoubleSupplier variation)
    {
        return builder(HC, HDG)
                .bind(magneticHeading)
                .bindDegrees(deviation)
                .bindDegrees(variation)
                .build();
    }
    /**
     * Returns byte buffer containing the sentence.
     * @return 
     */
    public ByteBuffer getByteBuffer()
    {
        ByteBuffer bb = ByteBuffer.allocate(100);
        writeTo(bb);
        bb.flip();
        return bb.slice();
    }
    /**
     * Returns true if NMEA sentence
     * @return 
     */
    public boolean isNMEA()
    {
        return NMEA.isNMEA(toString());
    }
    /**
     * Returns true if AIS sentence
     * @return 
     */
    public boolean isAIS()
    {
        return NMEA.isAIS(toString());
    }
    /**
     * Returns true if proprietary sentence
     * @return 
     */
    public boolean isProprietary()
    {
        return NMEA.isProprietory(toString());
    }
    /**
     * Returns talker-id if NMEA/AIS sentence or null
     * @return 
     */
    public TalkerId getTalkerId()
    {
        return NMEA.getTalkerId(toString());
    }
    /**
     * Returns message-id if NMEA/AIS sentence or null
     * @return 
     */
    public MessageType getMessageType()
    {
        return NMEA.getMessageType(toString());
    }
    /**
     * Returns sentence prefix. (before first ',')
     * @return 
     */
    public CharSequence getPrefix()
    {
        return NMEA.getPrefix(toString());
    }
    /**
     * Returns size of sentence.
     * @return 
     */
    public int size()
    {
        return getByteBuffer().limit();
    }
    /**
     * Write sentence to bb
     * @param bb 
     */
    public void writeTo(ByteBuffer bb)
    {
        for (Bind b : buffer)
        {
            ByteBuffer arr = b.array();
            bb.put(arr);
            checksum.update(arr.array());
        }
        checksum.fillSuffix(bb);
    }
    /**
     * Write sentence to out
     * @param out
     * @throws IOException 
     */
    public void writeTo(OutputStream out) throws IOException
    {
        for (Bind b : buffer)
        {
            byte[] array = b.array().array();
            out.write(array);
            checksum.update(array, 0, array.length);
        }
        byte[] cs = new byte[5];
        checksum.fillSuffix(cs);
        out.write(cs);
    }
    /**
     * Write sentence to out
     * @param out
     * @throws IOException 
     */
    public void writeTo(Appendable out) throws IOException
    {
        out.append(toString());
    }
    public void writeTo(WritableByteChannel channel) throws IOException
    {
        for (Bind b : buffer)
        {
            ByteBuffer array = b.array();
            channel.write(array);
            checksum.update(array.array());
        }
        ByteBuffer cs = ByteBuffer.allocate(5);
        checksum.fillSuffix(cs);
        channel.write(cs);
    }
    /**
     * Returns sentence as string
     * @return 
     */
    @Override
    public String toString()
    {
        return CharSequences.getAsciiCharSequence(getByteBuffer()).toString();
    }
    /**
     * Creates NMEASentence builder. Builder is initialized with '$'/'!', talker-id
     * and message-id
     * @param talkerId
     * @param messageType
     * @return 
     */
    public static Builder builder(TalkerId talkerId, MessageType messageType)
    {
        return new Builder(talkerId, messageType);
    }
    /**
     * Creates NMEASentence builder. Builder is initialized with prefix which
     * can contain the whole sentence until '*' including commas. Each field
     * if present are concatenated and separated with commas
     * @param prefix
     * @param fields
     * @return 
     */
    public static Builder builder(CharSequence prefix, CharSequence... fields)
    {
        return new Builder(prefix, fields);
    }
    public static final class Builder
    {
        private List<Bind> buffer = new ArrayList<>();

        private Builder(TalkerId talkerId, MessageType messageType)
        {
            switch (messageType)
            {
                case VDM:
                case VDO:
                    literal('!');
                    break;
                default:
                    literal('$');
                    break;
            }
            literal(talkerId.name());
            literal(messageType.name());
        }

        private Builder(CharSequence prefix, CharSequence... fields)
        {
            literal(prefix);
            for (CharSequence field : fields)
            {
                add(field);
            }
        }
        /**
         * Add empty field
         * @return 
         */
        public Builder add()
        {
            literal(',');
            return this;
        }
        /**
         * Add string field
         * @param fld
         * @return 
         */
        public Builder add(CharSequence fld)
        {
            add();
            try
            {
                literal(fld.toString().getBytes("NMEA"));
            }
            catch (UnsupportedEncodingException ex)
            {
                throw new RuntimeException(ex);
            }
            return this;
        }
        /**
         * Add double field
         * @param fld
         * @return 
         */
        public Builder bind(DoubleSupplier fldsup)
        {
            Builder.this.bind(()->
            {
                double fld = fldsup.getAsDouble();
                StringBuilder sb = new StringBuilder();
                sb.append(',');
                String str = String.format(Locale.US, "%.1f", fld);
                return wrap(str.endsWith(".0") ? str.substring(0, str.length()-2) : str);
            });
            return this;
        }
        public Builder bind(DoubleSupplier latsup, DoubleSupplier  lonsup)
        {
            Builder.this.bind(()->
            {
                double latitude = latsup.getAsDouble();
                double longitude = lonsup.getAsDouble();
                StringBuilder sb = new StringBuilder();
                double alat = Math.abs(latitude);
                double alon = Math.abs(longitude);
                int lat = (int) alat;
                int lon = (int) alon;
                sb.append(',');
                sb.append(String.format("%02d%07.4f", lat, (alat-lat)*60));
                sb.append(',');
                sb.append(latitude>0?'N':'S');
                sb.append(',');
                sb.append(String.format("%03d%07.4f", lon, (alon-lon)*60));
                sb.append(',');
                sb.append(longitude>0?'E':'W');
                return wrap(sb);
            });
            return this;
        }
        public Builder bindLocalTime(Supplier<LocalTime> time)
        {
            Builder.this.bind(()->
            {
                LocalTime t = time.get();
                return wrap(String.format(Locale.US, ",%02d%02d%02d", t.getHour(), t.getMinute(), t.getSecond()));
            });
            return this;
        }
        /**
         * Add int field
         * @param fld
         * @return 
         */
        public Builder add(int fld)
        {
            add();
            return Builder.this.bind(()->wrap(String.valueOf(fld)));
        }
        /**
         * Add char field
         * @param fld
         * @return 
         */
        public Builder add(char fld)
        {
            add();
            return literal(fld);
        }
        /**
         * Adds '*' starting suffix with checksum and returns NMEASentence.
         * @return 
         */
        public NMEASentence build()
        {
            return new NMEASentence(buffer.toArray(new Bind[buffer.size()]));
        }

        private Builder bind(Bind b)
        {
            buffer.add(b);
            return this;
        }
        private Builder literal(char c)
        {
            buffer.add(()->wrap(c));
            return this;
        }
        private Builder literal(byte[] arr)
        {
            buffer.add(()->wrap(arr));
            return this;
        }
        private Builder literal(CharSequence seq)
        {
            buffer.add(()->wrap(seq));
            return this;
        }
        private Builder bindArray(Supplier<byte[]> b)
        {
            buffer.add(new ArrayBind(b));
            return this;
        }

        private Builder bindString(Supplier<CharSequence> seq)
        {
            buffer.add(()->
            {
                CharSequence value;
                try
                {
                    value = CharSequences.getAsciiCharSequence(seq.get().toString().getBytes("NMEA"));
                }
                catch (UnsupportedEncodingException ex)
                {
                    throw new IllegalArgumentException(ex);
                }
                return wrap(String.format(Locale.US, ",%s", value));
            });
            return this;
        }

        private Builder bind(IntSupplier supplier)
        {
            buffer.add(()->
            {
                int value = supplier.getAsInt();
                return wrap(String.format(Locale.US, ",%d", value));
            });
            return this;
        }

        private Builder bindDegrees(DoubleSupplier declsup)
        {
            buffer.add(()->
            {
                double declination = declsup.getAsDouble();
                return wrap(String.format(Locale.US, ",%.1f,%c", Math.abs(declination), declination > 0 ? 'E' : 'W'));
            });
            return this;
        }

        private Builder bind(UnitType from, DoubleSupplier supplier, UnitType to)
        {
            buffer.add(()->
            {
                double value = supplier.getAsDouble();
                return wrap(String.format(Locale.US, ",%.1f", from.convertTo(value, to)));
            });
            return this;
        }

    }
    private static ByteBuffer wrap(char cc)
    {
        return ByteBuffer.wrap(new byte[]{(byte)cc});
    }
    private static ByteBuffer wrap(byte cc)
    {
        return ByteBuffer.wrap(new byte[]{cc});
    }
    private static ByteBuffer wrap(byte[] arr)
    {
        return ByteBuffer.wrap(arr);
    }
    private static ByteBuffer wrap(CharSequence seq)
    {
        return ByteBuffer.wrap(seq.toString().getBytes(US_ASCII));
    }
    @FunctionalInterface
    private interface Bind
    {
        ByteBuffer array();
    }
    private static class LiteralBind implements Bind
    {
        private ByteBuffer array;

        public LiteralBind(byte cc)
        {
            this(wrap(cc));
        }

        public LiteralBind(byte[] arr)
        {
            this(wrap(arr));
        }

        public LiteralBind(ByteBuffer array)
        {
            this.array = array;
        }

        @Override
        public ByteBuffer array()
        {
            return array;
        }
    }
    private static class ArrayBind implements Bind
    {
        private Supplier<byte[]> array;

        public ArrayBind(Supplier<byte[]> array)
        {
            this.array = array;
        }
        
        @Override
        public ByteBuffer array()
        {
            return wrap(array.get());
        }
        
    }
    private static class CharSequenceBind implements Bind
    {
        private Supplier<CharSequence> seq;

        public CharSequenceBind(Supplier<CharSequence> seq)
        {
            this.seq = seq;
        }
        
        @Override
        public ByteBuffer array()
        {
            String str = seq.get().toString();
            if (str.indexOf(',') != -1 || str.indexOf('*') != -1)
            {
                throw new IllegalArgumentException(str+" contains (,) or (*)");
            }
            return wrap(str.getBytes(US_ASCII));
        }
        
    }
}
