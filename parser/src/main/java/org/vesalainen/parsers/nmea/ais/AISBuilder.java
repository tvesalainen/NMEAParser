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

import org.vesalainen.math.Circle;
import org.vesalainen.math.Polygon;
import org.vesalainen.math.Sector;
import org.vesalainen.parsers.nmea.MessageType;
import org.vesalainen.parsers.nmea.NMEASentence;
import org.vesalainen.parsers.nmea.TalkerId;
import org.vesalainen.parsers.nmea.ais.areanotice.AssociatedText;
import org.vesalainen.parsers.nmea.ais.areanotice.CircleArea;
import org.vesalainen.parsers.nmea.ais.areanotice.PolygonBuilder;
import org.vesalainen.parsers.nmea.ais.areanotice.PolylineBuilder;
import org.vesalainen.parsers.nmea.ais.areanotice.SectorArea;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class AISBuilder
{
    public static final NMEASentence[] EMPTY = new NMEASentence[]{};
    
    private StringBuilder sb = new StringBuilder();

    AISBuilder()    // for testing
    {
    }

    public AISBuilder(MessageTypes type, int mmsi)
    {
        integer(6, type.ordinal());
        integer(2, 0);  // repeat
        integer(30, mmsi);
    }
    
    public NMEASentence[] build()
    {
        int length = sb.length();
        while ((length % 8) != 0)
        {
            sb.append('0');
            length = sb.length();
        }
        int padding = (6 - sb.length() % 6) % 6;
        integer(padding, 0);
        PayloadParser pp = PayloadParser.getInstance();
        StringBuilder payload = pp.parse(sb);
        int fragmentCount = payload.length()/56+1;
        NMEASentence[] sentences = new NMEASentence[fragmentCount];
        for (int fragment=1;fragment<=fragmentCount;fragment++)
        {
            NMEASentence.Builder builder = NMEASentence.builder(TalkerId.AI, MessageType.VDM);
            builder.add(fragmentCount);
            builder.add(fragment);
            builder.add('0');
            builder.add('A');
            int beg = (fragment-1)*56;
            int end = Math.min(fragment*56, payload.length());
            builder.add(payload.substring(beg, end));
            if (fragment == fragmentCount)
            {
                builder.add(padding);
            }
            else
            {
                builder.add(0);
            }
            sentences[fragment-1] = builder.build();
        }
        return sentences;
    }
    public AISBuilder string(int bits, CharSequence txt)
    {
        if ((bits % 6) != 0)
        {
            throw new IllegalArgumentException(bits+" not / 6");
        }
        for (int ii=1;ii<=bits/6;ii++)
        {
            if (ii <= txt.length())
            {
                char cc = txt.charAt(ii-1);
                if (cc > 64 && cc <= 95)
                {
                    integer(6, cc - 64);
                }
                else
                {
                    if (cc >= 32 && cc <= 63)
                    {
                        integer(6, cc);
                    }
                    else
                    {
                        throw new IllegalArgumentException(cc+" cannot be encoded in "+txt);
                    }
                }
            }
            else
            {
                sb.append("000000");    //integer(6, 0);  // @
            }
        }
        return this;
    }
    public AISBuilder rot(float rot)
    {
        integer(8, AISUtil.rot(rot));
        return this;
    }
    public AISBuilder bool(boolean b)
    {
        sb.append(b ? '1' : '0');
        return this;
    }
    public AISBuilder spare(int bits)
    {
        return integer(bits, 0);
    }
    public AISBuilder decimal(int bits, double value, double coef)
    {
        return integer(bits, (int) Math.round(value*coef));
    }
    public AISBuilder integer(int bits, Enum value, Enum def)
    {
        if (value != null)
        {
            return integer(bits, value.ordinal());
        }
        else
        {
            return integer(bits, def.ordinal());
        }
    }
    public AISBuilder integer(int bits, int value)
    {
        for (int ii=bits-1;ii>=0;ii--)
        {
            sb.append((value>>ii) & 1);
        }
        return this;
    }
    public AISBuilder polygon(Polygon polygon)
    {
        PolygonBuilder bldr = new PolygonBuilder();
        polygon.forEach(bldr::add);
        bldr.build(this);
        return this;
    }
    public AISBuilder polyline(Polygon polygon)
    {
        PolylineBuilder bldr = new PolylineBuilder();
        polygon.forEach(bldr::add);
        bldr.build(this);
        return this;
    }
    public AISBuilder circle(Circle circle)
    {
        new CircleArea(circle).build(this);
        return this;
    }
    public AISBuilder sector(Sector sector)
    {
        new SectorArea(sector).build(this);
        return this;
    }
    public AISBuilder associatedText(String text)
    {
        new AssociatedText(text).build(this);
        return this;
    }
    /**
     * For testing
     * @return 
     */
    String bits()
    {
        return sb.toString();
    }
}
