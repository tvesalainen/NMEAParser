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
package org.vesalainen.parsers.nmea.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author tkv
 */
public class NMEACharset extends Charset
{

    public NMEACharset()
    {
        super("NMEA", null);
    }

    @Override
    public boolean contains(Charset cs)
    {
        if (NMEACharset.class == cs.getClass())
        {
            return true;
        }
        if (cs.equals(StandardCharsets.US_ASCII))
        {
            return true;
        }
        return false;
    }

    @Override
    public CharsetDecoder newDecoder()
    {
        return new DecoderImpl();
    }

    @Override
    public CharsetEncoder newEncoder()
    {
        return new EncoderImpl();
    }

    public class DecoderImpl extends CharsetDecoder
    {

        public DecoderImpl()
        {
            super(NMEACharset.this, 1, 1);
        }

        @Override
        protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out)
        {
            while (in.hasRemaining())
            {
                if (!out.hasRemaining())
                {
                    return CoderResult.OVERFLOW;
                }
                byte b = in.get();
                if (b == '^')
                {
                    if (in.remaining() >= 2)
                    {
                        int i1 = Character.digit(in.get(), 16);
                        int i2 = Character.digit(in.get(), 16);
                        out.put((char) ((i1<<4)+i2));
                    }
                    else
                    {
                        in.position(in.position()-1);
                        return CoderResult.UNDERFLOW;
                    }
                }
                else
                {
                    out.put((char) b);
                }
            }
            return CoderResult.UNDERFLOW;
        }
    }

    private class EncoderImpl extends CharsetEncoder
    {

        public EncoderImpl()
        {
            super(NMEACharset.this, 1.1F, 3);
        }

        @Override
        protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out)
        {
            while (in.hasRemaining())
            {
                if (!out.hasRemaining())
                {
                    return CoderResult.OVERFLOW;
                }
                char c = in.get();
                if (Character.isBmpCodePoint(c))
                {
                    if (c <= '~')
                    {
                        out.put((byte) c);
                    }
                    else
                    {
                        if (out.remaining() < 3)
                        {
                            return CoderResult.OVERFLOW;
                        }
                        out.put((byte)'^');
                        out.put((byte) Character.toUpperCase(Character.forDigit(c>>4, 16)));
                        out.put((byte) Character.toUpperCase(Character.forDigit(c&0xf, 16)));
                    }
                }
                else
                {
                    return CoderResult.unmappableForLength(1);
                }
            }
            return CoderResult.UNDERFLOW;
        }
    }
    
}
