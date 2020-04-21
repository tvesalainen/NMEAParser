/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.parsers.seatalk;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.ScatteringByteChannel;
import static java.util.logging.Level.*;
import org.vesalainen.math.UnitType;
import org.vesalainen.parser.GenClassFactory;
import org.vesalainen.parser.ParserConstants;
import static org.vesalainen.parser.ParserFeature.SingleThread;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.ParseMethod;
import org.vesalainen.parser.annotation.ParserContext;
import org.vesalainen.parser.annotation.RecoverMethod;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.annotation.Rules;
import org.vesalainen.parser.annotation.Terminal;
import org.vesalainen.parser.util.InputReader;
import org.vesalainen.parsers.nmea.NMEASentence;
import org.vesalainen.util.HexDump;
import org.vesalainen.util.logging.AttachedLogger;
import org.vesalainen.util.logging.JavaLogging;

/**
 * A parser for Sea Talk protocol using Thomas Knaufs article.
 * <p>
 * 9-bit communication is solved with space parity. Command byte will cause an 
 * error. Both Linux and Windows serial drivers can be configured to send error
 * indication, which is used to locate the start of message.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @see <a href="http://www.thomasknauf.de/seatalk.htm">SeaTalk Technical Reference</a>
 */
@GenClassname("org.vesalainen.parsers.seatalk.SeaTalk2NMEAImpl")
@GrammarDef()
@Rules(
{
    @Rule(left = "statements", value = "(prefix statement)*"),
    @Rule(left = "single", value = "prefix statement"),
    @Rule(left = "prefix", value = "linuxPrefix"),
    @Rule(left = "prefix", value = "winPrefix"),
    @Rule(left = "prefix", value = "winPrefix2"),
    @Rule(left = "prefix", value = "noPrefix"),
    @Rule(left = "statement", value = "m00"),
    @Rule(left = "statement", value = "m01a"),
    @Rule(left = "statement", value = "m01b"),
    @Rule(left = "statement", value = "m01c"),
    @Rule(left = "statement", value = "m01d"),
    @Rule(left = "statement", value = "m01e"),
    @Rule(left = "statement", value = "m01f"),
    @Rule(left = "statement", value = "m20"),
    @Rule(left = "statement", value = "m23"),
    @Rule(left = "statement", value = "m26"),
    @Rule(left = "statement", value = "m24"),
    @Rule(left = "statement", value = "m27"),
    @Rule(left = "statement", value = "m30"),
    @Rule(left = "statement", value = "m60"),
    @Rule(left = "statement", value = "m65")
})
public abstract class SeaTalk2NMEA implements AttachedLogger
{
    private boolean haveBetterMTW;
    private boolean haveBetterVHW;

    @Rule("'\\xff\\xff'")
    protected void winPrefix()
    {
        debug("winPrefix");
    }
    @Rule("'\\xff'")
    protected void winPrefix2()
    {
        debug("winPrefix");
    }
    @Rule("'\\xff\\x00'")
    protected void linuxPrefix()
    {
        debug("linuxPrefix");
    }
    @Rule
    protected void noPrefix()
    {
        debug("noPrefix");
    }
    @Rule("'\\x00\\x02' b integer")
    protected void m00(
            char yz, 
            int xx, 
            @ParserContext("out") OutputStream out
    ) throws IOException
    {
        debug("m00");
        int y = yz >> 4;
        int z = yz & 0xf;
        if ((y & 8) == 8)
        {
            warning("%x anchor alarm %x", (int)yz, xx);
        }
        if ((y & 4) == 4)
        {
            debug("%x metric %x", (int)yz, xx);
        }
        if ((z & 2) == 2)
        {
            warning("%x deep alarm %x", (int)yz, xx);
        }
        if ((z & 1) == 1)
        {
            warning("%x shallow alarm %x", (int)yz, xx);
        }
        boolean defect = (z & 4) == 4;
        if (!defect)
        {
            NMEASentence dbt = NMEASentence.dpt(xx/10, 0, UnitType.FOOT);
            dbt.writeTo(out);
        }
    }
    @Rule("'\\x01\\x05\\x00\\x00\\x00\\x60\\x01\\x00'")
    protected void m01a(
            @ParserContext("out") OutputStream out
    ) throws IOException
    {
        debug("m01a");
        NMEASentence txt = NMEASentence.txt("Course Computer 400G");
        txt.writeTo(out);
    }
    @Rule("'\\x01\\x05\\x04\\xBA\\x20\\x28\\x01\\x00'")
    protected void m01b(
            @ParserContext("out") OutputStream out
    ) throws IOException
    {
        debug("m01b");
        NMEASentence txt = NMEASentence.txt("ST60 Tridata");
        txt.writeTo(out);
    }
    @Rule("'\\x01\\x05\\x70\\x99\\x10\\x28\\x01\\x00'")
    protected void m01c(
            @ParserContext("out") OutputStream out
    ) throws IOException
    {
        debug("m01c");
        NMEASentence txt = NMEASentence.txt("ST60 Log");
        txt.writeTo(out);
    }
    @Rule("'\\x01\\x05\\xF3\\x18\\x00\\x26\\x0F\\x06'")
    protected void m01d(
            @ParserContext("out") OutputStream out
    ) throws IOException
    {
        debug("m01d");
        NMEASentence txt = NMEASentence.txt("ST80 Masterview");
        txt.writeTo(out);
    }
    @Rule("'\\x01\\x05\\xFA\\x03\\x00\\x30\\x07\\x03'")
    protected void m01e(
            @ParserContext("out") OutputStream out
    ) throws IOException
    {
        debug("m01e");
        NMEASentence txt = NMEASentence.txt("ST80 Maxi Display");
        txt.writeTo(out);
    }
    @Rule("'\\x01\\x05\\xFF\\xFF\\xFF\\xD0\\x00\\x00'")
    protected void m01f(
            @ParserContext("out") OutputStream out
    ) throws IOException
    {
        debug("m01f");
        NMEASentence txt = NMEASentence.txt("Smart Controller Remote Control Handset");
        txt.writeTo(out);
    }
    @Rule("'\\x20\\x01' integer")
    protected void m20(
            int xx, 
            @ParserContext("out") OutputStream out
    ) throws IOException
    {
        debug("m20");
        if (!haveBetterVHW)
        {
            float knots = (float)xx/10;
            NMEASentence vhw = NMEASentence.vhw(knots, UnitType.KNOT);
            vhw.writeTo(out);
        }
    }
    @Rule("'\\x23\\x01' b b")
    protected void m23(
            char c, 
            char f, 
            @ParserContext("out") OutputStream out
    ) throws IOException
    {
        debug("m23");
        if (!haveBetterMTW)
        {
            NMEASentence mtw = NMEASentence.mtw(c, UnitType.CELSIUS);
            mtw.writeTo(out);
        }
    }
    @Rule("'\\x24\\x02\\x00\\x00' b")
    protected void m24(
            char displayUnits,
            @ParserContext("out") OutputStream out
    ) throws IOException
    {
        debug("m24");
    }
    @Rule("'\\x26\\x04' integer integer b")
    protected void m26(
            int xx, 
            int yy,
            char de,
            @ParserContext("out") OutputStream out
    ) throws IOException
    {
        debug("m26");
        float knots = (float)xx/100;
        haveBetterVHW = true;
        NMEASentence vhw = NMEASentence.vhw(knots, UnitType.KNOT);
        vhw.writeTo(out);
    }
    @Rule("'\\x27\\x01' integer")
    protected void m27(
            int xx, 
            @ParserContext("out") OutputStream out
    ) throws IOException
    {
        debug("m27");
        haveBetterMTW = true;
        float temp = (float)(xx-100)/10;
        NMEASentence mtw = NMEASentence.mtw(temp, UnitType.CELSIUS);
        mtw.writeTo(out);
    }
    @Rule("'\\x30\\x00' b")
    protected void m30(
            char cc, 
            @ParserContext("out") OutputStream out
    ) throws IOException
    {
        debug("m30");
        char i;
        switch (cc)
        {
            case 0x0:
                i = '0';
                break;
            case 0x4:
                i = '1';
                break;
            case 0x8:
                i = '2';
                break;
            case 0xc:
                i = '3';
                break;
            default:
                i = '?';
                break;
        }
        NMEASentence txt = NMEASentence.txt("Light L"+i);
        txt.writeTo(out);
    }
    @Rule("'\\x60\\x0c' b b b b b b b b b b b b b")
    protected void m60(
            char c1,
            char c2,
            char c3,
            char c4,
            char c5,
            char c6,
            char c7,
            char c8,
            char c9,
            char c10,
            char c11,
            char c12,
            char c13,
            @ParserContext("out") OutputStream out
    ) throws IOException
    {
        debug("m60 %02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X", (short)c1, (short)c2, (short)c3, (short)c4, (short)c5, (short)c6, (short)c7, (short)c8, (short)c9, (short)c10, (short)c11, (short)c12, (short)c13);
    }
    @Rule("'\\x65\\x00\\x02'")
    protected void m65(
            @ParserContext("out") OutputStream out
    ) throws IOException
    {
        debug("m65");
    }
    @Rule("b b")
    protected int integer(char x2, char x1)
    {
        return (x1<<8)+x2;
    }
    @Terminal(expression="[\\x00-\\xff]")
    protected abstract char b(char b);
    
    public static SeaTalk2NMEA newInstance()
    {
        return (SeaTalk2NMEA) GenClassFactory.loadGenInstance(SeaTalk2NMEA.class);
    }
    @ParseMethod(start = "single", size = 128, charSet = "ISO-8859-1" , features = {SingleThread})
    public abstract void parse(
            InputReader input,
            @ParserContext("out") OutputStream out
    ) throws IOException;
    @ParseMethod(start = "statements", features = {SingleThread})
    public abstract void parse(
            CharSequence cs,
            @ParserContext("out") OutputStream out
    ) throws IOException;
    @ParseMethod(start = "statements", size = 256, charSet = "ISO-8859-1" )
    public abstract void parse(
            ScatteringByteChannel channel,
            @ParserContext("out") OutputStream out
    ) throws IOException;
    @RecoverMethod
    public void recover(
            @ParserContext(ParserConstants.INPUTREADER) InputReader reader,
            @ParserContext(ParserConstants.THROWABLE) Throwable thr,
            @ParserContext("out") OutputStream out) throws IOException
    {
        int len = reader.getLength();
        byte[] buf = new byte[len];
        for (int ii=0;ii<len;ii++)
        {
            buf[ii] = (byte) reader.charAt(ii);
        }
        if (thr != null)
        {
            log(SEVERE, thr, "SeaTalk2NMEA.recover: \n%s", HexDump.toHex(buf));
            throw new IOException(thr);
        }
        else
        {
            finest("SeaTalk2NMEA.recover:");
            finest(()->HexDump.toHex(buf));
        }
        reader.clear();
    }

}
