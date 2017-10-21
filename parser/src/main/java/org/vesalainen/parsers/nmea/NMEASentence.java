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
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Locale;
import org.vesalainen.math.UnitType;
import static org.vesalainen.math.UnitType.*;
import static org.vesalainen.parsers.nmea.Converter.*;
import static org.vesalainen.parsers.nmea.MessageType.*;
import static org.vesalainen.parsers.nmea.TalkerId.*;
import org.vesalainen.util.CharSequences;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class NMEASentence
{
    private byte[] buffer;
    private CharSequence seq;

    public NMEASentence(CharSequence sentence) throws IOException
    {
        if (!NMEA.isNMEAOrAIS(sentence))
        {
            throw new IllegalArgumentException(sentence+" not valid");
        }
        buffer = sentence.toString().getBytes("NMEA");
        seq = CharSequences.getAsciiCharSequence(buffer);
    }

    public NMEASentence(byte[] buffer)
    {
        this.buffer = buffer;
        seq = CharSequences.getAsciiCharSequence(buffer);
        if (!NMEA.isNMEAOrAIS(seq))
        {
            throw new IllegalArgumentException(seq+" not valid");
        }
    }
    /**
     * Recommended Minimum Specific GNSS Data - only declination
     * @param declination
     * @return
     * @throws IOException 
     */
    public static NMEASentence rmc(double declination) throws IOException
    {
        String variation = String.format(Locale.US, "%.1f", Math.abs(declination));
        char ew = declination > 0 ? 'E' : 'W';
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
                .add(variation)    // Magnetic variation degrees
                .add(ew)    // E / W
                .add('A')   // FAA
                .build();
    }
    /**
     * Depth Below Transducer
     * @param depth
     * @return
     * @throws IOException 
     */
    public static NMEASentence dbt(float depth, UnitType unit) throws IOException
    {
        return builder(SD, DBT)
                .add(unit.convertTo(depth, Foot))
                .add(FT)
                .add(unit.convertTo(depth, Meter))
                .add(M)
                .add(unit.convertTo(depth, Fathom))
                .add(FATH)
                .build();
    }
    /**
     * Water Speed and Heading
     * @param speed
     * @return
     * @throws IOException 
     */
    public static NMEASentence vhw(double speed, UnitType unit) throws IOException
    {
        return builder(VW, VHW)
                .add().add().add().add()
                .add(unit.convertTo(speed, Knot))
                .add(KTS)
                .add(unit.convertTo(speed, UnitType.KMH))
                .add(Converter.KMH)
                .build();
    }
    /**
     * Wind speed and Angle
     * @param windAngle
     * @param windSpeed
     * @param trueWind
     * @return
     * @throws IOException 
     */
    public static NMEASentence mwv(int windAngle, float windSpeed, UnitType unit, boolean trueWind) throws IOException
    {
        return builder(UP, MWV)
                .add(windAngle)
                .add(trueWind ? 'T' : 'R')
                .add(unit.convertTo(windSpeed, Knot))
                .add(KTS)
                .add('A')
                .build();
    }
    /**
     * Water Temperature
     * @param temperature
     * @param unit
     * @return
     * @throws IOException 
     */
    public static NMEASentence mtw(float temperature, UnitType unit) throws IOException
    {
        return builder(YC, MTW)
                .add(unit.convertTo(temperature, Celsius))
                .add(CELCIUS)
                .build();
    }
    /**
     * Text Transmission
     * @param msg
     * @return
     * @throws IOException 
     */
    public static NMEASentence txt(String msg) throws IOException
    {
        if (msg.indexOf(',') != -1 || msg.indexOf('*') != -1)
        {
            throw new IllegalArgumentException(msg+" contains (,) or (*)");
        }
        return builder(U0, TXT)
                .add(1)
                .add(1)
                .add()
                .add(msg)
                .build();
    }

    
    public boolean isNMEA()
    {
        return NMEA.isNMEA(seq);
    }
    public boolean isAIS()
    {
        return NMEA.isAIS(seq);
    }
    public boolean isProprietary()
    {
        return NMEA.isProprietory(seq);
    }
    public TalkerId getTalkerId()
    {
        return NMEA.getTalkerId(seq);
    }
    public MessageType getMessageType()
    {
        return NMEA.getMessageType(seq);
    }
    public CharSequence getPrefix()
    {
        return NMEA.getPrefix(seq);
    }
    public void writeTo(ByteBuffer bb)
    {
        bb.put(buffer);
    }
    public void writeTo(OutputStream out) throws IOException
    {
        out.write(buffer);
    }
    public void writeTo(Appendable out) throws IOException
    {
        out.append(seq);
    }

    @Override
    public String toString()
    {
        return seq.toString();
    }
    
    public static Builder builder(TalkerId talkerId, MessageType messageType) throws IOException
    {
        return new Builder(talkerId, messageType);
    }
    public static Builder builder(CharSequence prefix, CharSequence... fields) throws IOException
    {
        return new Builder(prefix, fields);
    }
    public static class Builder
    {
        private byte[] buffer = new byte[128];
        private int index;

        public Builder(TalkerId talkerId, MessageType messageType) throws IOException
        {
            switch (messageType)
            {
                case VDM:
                case VDO:
                    write('!');
                    break;
                default:
                    write('$');
                    break;
            }
            write(talkerId.name().getBytes("NMEA"));
            write(messageType.name().getBytes("NMEA"));
        }

        public Builder(CharSequence prefix, CharSequence... fields) throws IOException
        {
            write(prefix.toString().getBytes("NMEA"));
            for (CharSequence field : fields)
            {
                add(field);
            }
        }
        
        public Builder add() throws IOException
        {
            write(',');
            return this;
        }
        public Builder add(CharSequence fld) throws IOException
        {
            add();
            write(fld.toString().getBytes("NMEA"));
            return this;
        }
        public Builder add(double fld) throws IOException
        {
            return add(String.format(Locale.US, "%.1f", fld));
        }
        public Builder add(int fld) throws IOException
        {
            return add(String.format(Locale.US, "%d", fld));
        }
        public Builder add(char fld) throws IOException
        {
            return add(String.valueOf(fld));
        }
        public NMEASentence build()
        {
            NMEAChecksum checksum = new NMEAChecksum();
            checksum.update(buffer, 0, index);
            checksum.fillSuffix(buffer, index);
            index += 5;
            return new NMEASentence(Arrays.copyOf(buffer, index));
        }

        private void write(char c)
        {
            buffer[index++] = (byte) c;
        }
        private void write(byte[] b)
        {
            System.arraycopy(b, 0, buffer, index, b.length);
            index += b.length;
        }
    }
}
