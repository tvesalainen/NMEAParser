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
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import org.vesalainen.io.Printer;
import org.vesalainen.math.UnitType;
import static org.vesalainen.math.UnitType.*;
import org.vesalainen.navi.Navis;
import org.vesalainen.nio.PrintBuffer;
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
    private Consumer<Printer>[] binds;
    private NMEAChecksum checksum = new NMEAChecksum();
    private static ThreadLocal<PrintBuffer> printer = ThreadLocal.withInitial(()->new PrintBuffer(US_ASCII, ByteBuffer.allocate(100)));
    /**
     * Creates NMEASentence from array
     * @param buffer 
     */
    private NMEASentence(Consumer<Printer>[] buffer)
    {
        this.binds = buffer;
    }
    /**
     * Recommended Minimum Specific GNSS Data - only declination
     * @param magneticVariation
     * @return 
     */
    public static NMEASentence rmc(double magneticVariation)
    {
        return rmc(null, null, null, null, null, ()->magneticVariation);
    }
    public static NMEASentence rmc(Clock clock, double latitude, double longitude, double sog, double tmg, double magneticVariation)
    {
        return rmc(()->clock, ()->latitude, ()->longitude, ()->sog, ()->tmg, ()->magneticVariation);
    }
    public static NMEASentence rmc(Supplier<Clock> clock, DoubleSupplier latitude, DoubleSupplier longitude, DoubleSupplier speedOverGround, DoubleSupplier trackMadeGood, DoubleSupplier magneticVariation)
    {
        return rmc(()->IN, clock, ()->'A', latitude, longitude, speedOverGround, KNOT, trackMadeGood, magneticVariation, ()->'A');
    }
    public static NMEASentence rmc(Supplier<TalkerId> talkerId, Supplier<Clock> clock, IntSupplier status, DoubleSupplier latitude, DoubleSupplier longitude, DoubleSupplier speedOverGround, UnitType speedUnit, DoubleSupplier trackMadeGood, DoubleSupplier magneticVariation, IntSupplier faa)
    {
        return builder(talkerId, RMC)
                .bindLocalTime(clock)      // utc
                .bindChar(status)   // status
                .bindCoordinates(latitude, longitude)
                .bind(speedUnit, speedOverGround, KNOT)      // sog
                .bindDouble(trackMadeGood)      // tmg
                .bindLocalDate(clock)      // ddmmyy
                .bindDegrees(magneticVariation)    // Magnetic variation degrees
                .bindChar(faa)   // FAA
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
        return dpt(()->SD, meters, offset, ()->0, from);
    }
    public static NMEASentence dpt(Supplier<TalkerId> talkerId, DoubleSupplier meters, DoubleSupplier offset, DoubleSupplier range, UnitType from)
    {
        return builder(talkerId, DPT)
                .bind(from, meters, METER)
                .bind(from, offset, METER)
                .bind(from, range, METER)
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
        return dbt(()->SD, depth, from);
    }
    public static NMEASentence dbt(Supplier<TalkerId> talkerId, DoubleSupplier depth, UnitType from)
    {
        return builder(talkerId, DBT)
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
        return vhw(()->VW, speed, from);
    }
    public static NMEASentence vhw(Supplier<TalkerId> talkerId, DoubleSupplier speed, UnitType from)
    {
        return builder(talkerId, VHW)
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
    public static NMEASentence mwv(DoubleSupplier windAngle, DoubleSupplier windSpeed, UnitType from, boolean trueWind)
    {
        return mwv(()->UP, windAngle, windSpeed, from, trueWind);
    }
    public static NMEASentence mwv(Supplier<TalkerId> talkerId, DoubleSupplier windAngle, DoubleSupplier windSpeed, UnitType from, boolean trueWind)
    {
        return builder(talkerId, MWV)
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
        return mtw(()->YC, temperature, from);
    }
    public static NMEASentence mtw(Supplier<TalkerId> talkerId, DoubleSupplier temperature, UnitType from)
    {
        return builder(talkerId, MTW)
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
     * @param clock UTC of data
     * @param status Status (L=lost, Q=acquisition, T=tracking)
     * @param referenceTarget R= reference target; null (,,)= otherwise
     * @return 
     */
    public static NMEASentence tll(int target, double latitude, double longitude, CharSequence name, Clock clock, char status, CharSequence referenceTarget)
    {
        return tll(()->target, ()->latitude, ()->longitude, ()->name, ()->clock, ()->String.valueOf(status), ()->referenceTarget);
    }
    public static NMEASentence tll(IntSupplier target, DoubleSupplier latitude, DoubleSupplier longitude, Supplier<CharSequence> name, Supplier<Clock> clock ,Supplier<CharSequence> status, Supplier<CharSequence> referenceTarget)
    {
        return tll(()->YL, target, latitude, longitude, name, clock, status, referenceTarget);
    }
    public static NMEASentence tll(Supplier<TalkerId> talkerId, IntSupplier target, DoubleSupplier latitude, DoubleSupplier longitude, Supplier<CharSequence> name, Supplier<Clock> clock ,Supplier<CharSequence> status, Supplier<CharSequence> referenceTarget)
    {
        return builder(talkerId, TLL)
                .bind(target)
                .bindCoordinates(latitude, longitude)
                .bindString(name)
                .bindLocalTime(clock)
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
        return txt(()->0, ()->msg);
    }
    public static NMEASentence txt(IntSupplier id, Supplier<CharSequence> msg)
    {
        return txt(()->U0, id, msg);
    }
    public static NMEASentence txt(Supplier<TalkerId> talkerId, IntSupplier id, Supplier<CharSequence> msg)
    {
        return builder(talkerId, TXT)
                .add(1)
                .add(1)
                .bind(id)
                .bindString(msg)
                .build();
    }
    public static NMEASentence hdm(double magneticHeading)
    {
        return hdm(()->magneticHeading);
    }
    public static NMEASentence hdm(DoubleSupplier magneticHeading)
    {
        return hdm(()->HC, magneticHeading);
    }
    public static NMEASentence hdm(Supplier<TalkerId> talkerId, DoubleSupplier magneticHeading)
    {
        return builder(talkerId, HDM)
                .bindDouble(magneticHeading)
                .add('M')
                .build();
    }
    public static NMEASentence hdt(double trueHeading)
    {
        return hdt(()->trueHeading);
    }
    public static NMEASentence hdt(DoubleSupplier trueHeading)
    {
        return hdt(()->HC, trueHeading);
    }
    public static NMEASentence hdt(Supplier<TalkerId> talkerId, DoubleSupplier trueHeading)
    {
        return builder(talkerId, HDT)
                .bindDouble(trueHeading)
                .add('T')
                .build();
    }
    public static NMEASentence hdg(double magneticHeading, double deviation, double variation)
    {
        return hdg(()->magneticHeading, ()->deviation, ()->variation);
    }
    public static NMEASentence hdg(DoubleSupplier magneticHeading, DoubleSupplier deviation, DoubleSupplier variation)
    {
        return hdg(()->HC, magneticHeading, deviation, variation);
    }
    public static NMEASentence hdg(Supplier<TalkerId> talkerId, DoubleSupplier magneticHeading, DoubleSupplier deviation, DoubleSupplier variation)
    {
        return builder(talkerId, HDG)
                .bindDouble(magneticHeading)
                .bindDegrees(deviation)
                .bindDegrees(variation)
                .build();
    }
    public static NMEASentence hdg(Supplier<TalkerId> talkerId, DoubleSupplier trueHeading, DoubleSupplier variation)
    {
        return builder(talkerId, HDG)
                .bindDouble(()->Navis.normalizeAngle(trueHeading.getAsDouble()-variation.getAsDouble()))
                .bindDegrees(()->0)
                .bindDegrees(variation)
                .build();
    }
    public static NMEASentence attitude(Supplier<TalkerId> talkerId, DoubleSupplier yaw, DoubleSupplier pitch, DoubleSupplier roll)
    {
        return builder(talkerId, XDR)
                .bindXdrGroup('A', yaw, 'D', "YAW", null)
                .bindXdrGroup('A', pitch, 'D', "PITCH", null)
                .bindXdrGroup('A', roll, 'D', "ROLL", null)
                .build();
    }
    public static NMEASentence battery(Supplier<TalkerId> talkerId, IntSupplier instance, DoubleSupplier voltage, DoubleSupplier amp, DoubleSupplier temp)
    {
        return builder(talkerId, XDR)
                .bindXdrGroup('V', voltage, 'V', "BAT", instance) 
                .bindXdrGroup('I', amp, 'A', "BAT", instance)
                .bindXdrGroup('T', temp, 'C', "BAT", instance)
                .build();
    }
    public static NMEASentence dcDetailedStatus(Supplier<TalkerId> talkerId, IntSupplier instance, IntSupplier soc, IntSupplier soh)
    {
        return builder(talkerId, XDR)
                .bindXdrGroup('C', soc, 'P', "BAT", instance) 
                .bindXdrGroup('H', soh, 'P', "BAT", instance)
                .build();
    }
    public static NMEASentence environmental(Supplier<TalkerId> talkerId, DoubleSupplier outTemp, DoubleSupplier waterTemp, DoubleSupplier pressure)
    {
        return builder(talkerId, XDR)
                .bindXdrGroup('C', outTemp, 'C', "ENV_OUTAIR_T", null)
                .bindXdrGroup('C', waterTemp, 'C', "ENV_WATER_T", null)
                .bindXdrGroup3('P', pressure, 'B', "Barometer", null)
                .build();
    }
    public static NMEASentence windOverGround(DoubleSupplier speed, DoubleSupplier angle)
    {
        return windOverGround(()->UP, speed, angle);
    }
    public static NMEASentence windOverGround(Supplier<TalkerId> talkerId, DoubleSupplier angle, DoubleSupplier speed)
    {
        return builder(talkerId, XDR)
                .bindXdrGroup('A', angle, 'D', "WOG", null)
                .bindXdrGroup('S', speed, 'N', "WOG", null)
                .build();
    }
    public static NMEASentence drift(DoubleSupplier angle, DoubleSupplier speed)
    {
        return builder(()->UP, XDR)
                .bindXdrGroup('A', angle, 'D', "DRIFT", null)
                .bindXdrGroup('S', speed, 'N', "DRIFT", null)
                .build();
    }
    public static NMEASentence tide(Supplier<TalkerId> talkerId, DoubleSupplier range, DoubleSupplier phase)
    {
        return builder(talkerId, XDR)
                .bindXdrGroup('R', range, 'M', "TIDE", null)
                .bindXdrGroup('P', phase, 'D', "TIDE", null)
                .build();
    }
    /**
     * Returns byte buffer containing the sentence. Changes to returned buffer
     * will not affect this sentence.
     * @return 
     */
    public ByteBuffer getByteBuffer()
    {
        ByteBuffer bb = ByteBuffer.allocate(100);
        ByteBuffer fb = fillBuffer();
        bb.put(fb);
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
     * Write sentence to out
     * @param out
     * @throws IOException 
     */
    public void writeTo(OutputStream out) throws IOException
    {
        ByteBuffer bb = fillBuffer();
        out.write(bb.array(), 0, bb.remaining());
    }
    /**
     * Write sentence to out
     * @param out
     * @throws IOException 
     */
    public void writeTo(Appendable out) throws IOException
    {
        ByteBuffer bb = fillBuffer();
        out.append(CharSequences.getAsciiCharSequence(bb));
    }
    public void writeTo(WritableByteChannel channel) throws IOException
    {
        ByteBuffer bb = fillBuffer();
        channel.write(bb);
    }
    /**
     * Returns sentence as string
     * @return 
     */
    @Override
    public String toString()
    {
        ByteBuffer bb = fillBuffer();
        return CharSequences.getAsciiCharSequence(bb).toString();
    }
    private ByteBuffer fillBuffer()
    {
        PrintBuffer pb = printer.get();
        pb.clear();
        for (Consumer<Printer> b : binds)
        {
            b.accept(pb);
        }
        pb.flush();
        ByteBuffer bb = pb.getByteBuffer();
        checksum.update(bb.array(), 0, bb.position());
        checksum.fillSuffix(bb);
        bb.flip();
        return bb;
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
     * Creates NMEASentence builder.Builder is initialized with '$'/'!', talker-id
     * and message-id
     * @param talkerIdSupplier
     * @param messageType
     * @return 
     */
    public static Builder builder(Supplier<TalkerId> talkerIdSupplier, MessageType messageType)
    {
        return new Builder(talkerIdSupplier, messageType);
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
        private List<Consumer<Printer>> buffer = new ArrayList<>();

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

        private Builder(Supplier<TalkerId> talkerIdSupplier, MessageType messageType)
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
            bindSupplier(talkerIdSupplier);
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
            literal(fld);
            return this;
        }
        public Builder bindSupplier(Supplier<?> supplier)
        {
            bind((p)->
            {
                p.print(supplier.get());
            });
            return this;
        }
        /**
         * Bind double field
         * @param fldsup
         * @return 
         */
        public Builder bindDouble(DoubleSupplier fldsup)
        {
            if (fldsup != null)
            {
                bind((p)->
                {
                    double fld = fldsup.getAsDouble();
                    if (Double.isFinite(fld))
                    {
                        String str = String.format(Locale.US, ",%.1f", fld);
                        p.print(str.endsWith(".0") ? str.substring(0, str.length()-2) : str);
                    }
                    else
                    {
                        p.print(',');
                    }
                });
            }
            else
            {
                bind((p)->p.print(','));
            }
            return this;
        }
        public Builder bindCoordinates(DoubleSupplier latsup, DoubleSupplier  lonsup)
        {
            if (latsup != null && lonsup != null)
            {
                bind((p)->
                {
                    double latitude = latsup.getAsDouble();
                    double longitude = lonsup.getAsDouble();
                    if (Double.isFinite(latitude) && Double.isFinite(longitude))
                    {
                        double alat = Math.abs(latitude);
                        double alon = Math.abs(longitude);
                        int lat = (int) alat;
                        int lon = (int) alon;
                        p.format(",%02d%08.5f,%c,%03d%08.5f,%c", 
                                lat, 
                                (alat-lat)*60,
                                latitude>0?'N':'S',
                                lon, 
                                (alon-lon)*60,
                                longitude>0?'E':'W'
                        );
                    }
                    else
                    {
                        p.print(",,,,");
                    }
                });
            }
            else
            {
                bind((p)->p.print(",,,,"));
            }
            return this;
        }
        public Builder bindLocalTime(Supplier<Clock> clock)
        {
            if (clock != null)
            {
                bind((p)->
                {
                    LocalTime t = LocalTime.now(clock.get());
                    p.format(Locale.US, ",%02d%02d%02d", t.getHour(), t.getMinute(), t.getSecond());
                });
            }
            else
            {
                bind((p)->p.print(','));
            }
            return this;
        }
        public Builder bindLocalDate(Supplier<Clock> clock)
        {
            if (clock != null)
            {
                bind((p)->
                {
                    LocalDate t = LocalDate.now(clock.get());
                    p.format(Locale.US, ",%02d%02d%02d", t.getDayOfMonth(), t.getMonthValue(), t.getYear()%100);
                });
            }
            else
            {
                bind((p)->p.print(','));
            }
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
            return bind((p)->p.print(String.valueOf(fld)));
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
            return new NMEASentence(buffer.toArray(new Consumer[buffer.size()]));
        }

        private Builder bind(Consumer<Printer> p)
        {
            buffer.add(p);
            return this;
        }
        private Builder literal(char c)
        {
            buffer.add((p)->p.print(c));
            return this;
        }
        private Builder literal(CharSequence seq)
        {
            buffer.add((p)->p.print(seq));
            return this;
        }
        private Builder bindString(Supplier<? extends CharSequence> seq)
        {
            if (seq != null)
            {
                bind((p)->
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
                    p.print(String.format(Locale.US, ",%s", value));
                });
            }
            else
            {
                bind((p)->p.print(','));
            }
            return this;
        }

        private Builder bind(IntSupplier supplier)
        {
            if (supplier != null)
            {
                bind((p)->
                {
                    int value = supplier.getAsInt();
                    p.format(Locale.US, ",%d", value);
                });
            }
            else
            {
                bind((p)->p.print(','));
            }
            return this;
        }

        private Builder bindChar(IntSupplier supplier)
        {
            if (supplier != null)
            {
                bind((p)->
                {
                    int value = supplier.getAsInt();
                    p.format(Locale.US, ",%c", value);
                });
            }
            else
            {
                bind((p)->p.print(','));
            }
            return this;
        }

        private Builder bind(DoubleSupplier supplier)
        {
            if (supplier != null)
            {
                bind((p)->
                {
                    double value = supplier.getAsDouble();
                    if (Double.isFinite(value))
                    {
                        p.format(Locale.US, ",%.1f", value);
                    }
                    else
                    {
                        p.print(',');
                    }
                });
            }
            else
            {
                bind((p)->p.print(','));
            }
            return this;
        }

        private Builder bindDegrees(DoubleSupplier degsup)
        {
            if (degsup != null)
            {
                bind((p)->
                {
                    double degrees = degsup.getAsDouble();
                    if (Double.isFinite(degrees))
                    {
                        p.format(Locale.US, ",%.1f,%c", Math.abs(degrees), degrees > 0 ? 'E' : 'W');
                    }
                    else
                    {
                        p.print(",,");
                    }
                });
            }
            else
            {
                bind((p)->p.print(",,"));
            }
            return this;
        }

        private Builder bind(UnitType from, DoubleSupplier supplier, UnitType to)
        {
            if (supplier != null)
            {
                bind((p)->
                {
                    double value = supplier.getAsDouble();
                    if (Double.isFinite(value))
                    {
                        p.format(Locale.US, ",%.1f", from.convertTo(value, to));
                    }
                    else
                    {
                        p.print(',');
                    }
                });
            }
            else
            {
                bind((p)->p.print(','));
            }
            return this;
        }
        private Builder bindXdrGroup(char type, DoubleSupplier supplier, char unit, String name, IntSupplier instance)
        {
                bind((p)->
                {
                    double v = supplier.getAsDouble();
                    if (Double.isFinite(v))
                    {
                        p.print(',');
                        p.print(type);
                        p.format(Locale.US, ",%.1f,", v);
                        p.print(unit);
                        p.print(',');
                        p.print(name);
                        if (instance != null)
                        {
                            p.print(instance.getAsInt());
                        }
                    }
                });
                return this;
        }
        private Builder bindXdrGroup(char type, IntSupplier supplier, char unit, String name, IntSupplier instance)
        {
                bind((p)->
                {
                    int v = supplier.getAsInt();
                    p.print(',');
                    p.print(type);
                    p.format(Locale.US, ",%d,", v);
                    p.print(unit);
                    p.print(',');
                    p.print(name);
                    if (instance != null)
                    {
                        p.print(instance.getAsInt());
                    }
                });
                return this;
        }
        private Builder bindXdrGroup3(char type, DoubleSupplier supplier, char unit, String name, IntSupplier instance)
        {
                bind((p)->
                {
                    double v = supplier.getAsDouble();
                    if (Double.isFinite(v))
                    {
                        p.print(',');
                        p.print(type);
                        p.format(Locale.US, ",%.4f,", v);
                        p.print(unit);
                        p.print(',');
                        p.print(name);
                        if (instance != null)
                        {
                            p.print(instance.getAsInt());
                        }
                    }
                });
                return this;
        }
    }
}
