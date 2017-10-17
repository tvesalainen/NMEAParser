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
import java.util.zip.CheckedOutputStream;
import java.nio.channels.ScatteringByteChannel;
import static java.util.logging.Level.*;
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
import org.vesalainen.parsers.nmea.NMEAChecksum;
import org.vesalainen.parsers.nmea.NMEAGen;
import org.vesalainen.util.HexDump;
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
public abstract class SeaTalk2NMEA extends JavaLogging
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
            @ParserContext("out") CheckedOutputStream out
    ) throws IOException
    {
        debug("m00");
        int y = yz >> 4;
        int z = yz & 0xf;
        boolean anchorAlarm = (y & 8) == 8;
        boolean metric = (y & 4) == 4;
        boolean defect = (z & 4) == 4;
        boolean deepAlarm = (z & 2) == 2;
        boolean shallowAlarm = (z & 1) == 1;
        if (yz != 0x60)
        {
            warning("%x defect %x", yz, xx);
        }
        if (!defect)
        {
            NMEAGen.dbt(out, (float)xx/10F);
        }
    }
    @Rule("'\\x01\\x05\\x00\\x00\\x00\\x60\\x01\\x00'")
    protected void m01a(
            @ParserContext("out") CheckedOutputStream out
    ) throws IOException
    {
        debug("m01a");
        NMEAGen.txt(out, "Course Computer 400G");
    }
    @Rule("'\\x01\\x05\\x04\\xBA\\x20\\x28\\x01\\x00'")
    protected void m01b(
            @ParserContext("out") CheckedOutputStream out
    ) throws IOException
    {
        debug("m01b");
        NMEAGen.txt(out, "ST60 Tridata");
    }
    @Rule("'\\x01\\x05\\x70\\x99\\x10\\x28\\x01\\x00'")
    protected void m01c(
            @ParserContext("out") CheckedOutputStream out
    ) throws IOException
    {
        debug("m01c");
        NMEAGen.txt(out, "ST60 Log");
    }
    @Rule("'\\x01\\x05\\xF3\\x18\\x00\\x26\\x0F\\x06'")
    protected void m01d(
            @ParserContext("out") CheckedOutputStream out
    ) throws IOException
    {
        debug("m01d");
        NMEAGen.txt(out, "ST80 Masterview");
    }
    @Rule("'\\x01\\x05\\xFA\\x03\\x00\\x30\\x07\\x03'")
    protected void m01e(
            @ParserContext("out") CheckedOutputStream out
    ) throws IOException
    {
        debug("m01e");
        NMEAGen.txt(out, "ST80 Maxi Display");
    }
    @Rule("'\\x01\\x05\\xFF\\xFF\\xFF\\xD0\\x00\\x00'")
    protected void m01f(
            @ParserContext("out") CheckedOutputStream out
    ) throws IOException
    {
        debug("m01f");
        NMEAGen.txt(out, "Smart Controller Remote Control Handset");
    }
    @Rule("'\\x20\\x01' integer")
    protected void m20(
            int xx, 
            @ParserContext("out") CheckedOutputStream out
    ) throws IOException
    {
        debug("m20");
        if (!haveBetterVHW)
        {
            float knots = (float)xx/10;
            NMEAGen.vhw(out, knots);
        }
    }
    @Rule("'\\x23\\x01' b b")
    protected void m23(
            char c, 
            char f, 
            @ParserContext("out") CheckedOutputStream out
    ) throws IOException
    {
        debug("m23");
        if (!haveBetterMTW)
        {
            NMEAGen.mtw(out, c);
        }
    }
    @Rule("'\\x24\\x02\\x00\\x00' b")
    protected void m24(
            char displayUnits,
            @ParserContext("out") CheckedOutputStream out
    ) throws IOException
    {
        debug("m24");
    }
    @Rule("'\\x26\\x04' integer integer b")
    protected void m26(
            int xx, 
            int yy,
            char de,
            @ParserContext("out") CheckedOutputStream out
    ) throws IOException
    {
        debug("m26");
        float knots = (float)xx/100;
        haveBetterVHW = true;
        NMEAGen.vhw(out, knots);
    }
    @Rule("'\\x27\\x01' integer")
    protected void m27(
            int xx, 
            @ParserContext("out") CheckedOutputStream out
    ) throws IOException
    {
        debug("m27");
        haveBetterMTW = true;
        float temp = (float)(xx-100)/10;
        NMEAGen.mtw(out, temp);
    }
    @Rule("'\\x30\\x00' b")
    protected void m30(
            char cc, 
            @ParserContext("out") CheckedOutputStream out
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
        NMEAGen.txt(out, "Light L"+i);
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
            @ParserContext("out") CheckedOutputStream out
    ) throws IOException
    {
        debug("m60 %02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X", (short)c1, (short)c2, (short)c3, (short)c4, (short)c5, (short)c6, (short)c7, (short)c8, (short)c9, (short)c10, (short)c11, (short)c12, (short)c13);
    }
    @Rule("'\\x65\\x00\\x02'")
    protected void m65(
            @ParserContext("out") CheckedOutputStream out
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
    public void parse(CharSequence cs, OutputStream out) throws IOException
    {
        CheckedOutputStream cos = new CheckedOutputStream(out, new NMEAChecksum());
        parse(cs, cos);
    }
    public void parse(ScatteringByteChannel channel, OutputStream out) throws IOException
    {
        CheckedOutputStream cos = new CheckedOutputStream(out, new NMEAChecksum());
        parse(channel, cos);
    }
    @ParseMethod(start = "single", size = 128, charSet = "ISO-8859-1" , features = {SingleThread})
    public abstract void parse(
            InputReader input,
            @ParserContext("out") CheckedOutputStream out
    ) throws IOException;
    @ParseMethod(start = "statements", features = {SingleThread})
    protected abstract void parse(
            CharSequence cs,
            @ParserContext("out") CheckedOutputStream out
    ) throws IOException;
    @ParseMethod(start = "statements", size = 256, charSet = "ISO-8859-1" )
    protected abstract void parse(
            ScatteringByteChannel channel,
            @ParserContext("out") CheckedOutputStream out
    ) throws IOException;
    @RecoverMethod
    public void recover(
            @ParserContext(ParserConstants.INPUTREADER) InputReader reader,
            @ParserContext(ParserConstants.THROWABLE) Throwable thr,
            @ParserContext("out") CheckedOutputStream out) throws IOException
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
            warning("SeaTalk2NMEA.recover:");
            warning(HexDump.toHex(buf));
        }
        reader.clear();
    }

}
