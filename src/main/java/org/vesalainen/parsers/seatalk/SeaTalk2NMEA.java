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
package org.vesalainen.parsers.seatalk;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.WritableByteChannel;
import org.vesalainen.parser.GenClassFactory;
import org.vesalainen.parser.ParserConstants;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.ParseMethod;
import org.vesalainen.parser.annotation.ParserContext;
import org.vesalainen.parser.annotation.RecoverMethod;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.annotation.Rules;
import org.vesalainen.parser.annotation.Terminal;
import org.vesalainen.parser.util.InputReader;
import org.vesalainen.parsers.nmea.LocalNMEAChecksum;
import org.vesalainen.parsers.nmea.NMEAGen;

/**
 * Note! This class is not thread safe.
 * @author tkv
 * @see <a href="http://www.thomasknauf.de/seatalk.htm">SeaTalk Technical Reference</a>
 */
@GenClassname("org.vesalainen.parsers.seatalk.SeaTalk2NMEAImpl")
@GrammarDef()
@Rules(
{
    @Rule(left = "statements", value = "(prefix statement)*"),
    @Rule(left = "prefix", value = "linuxPrefix"),
    @Rule(left = "prefix", value = "winPrefix"),
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
public abstract class SeaTalk2NMEA
{
    private static final LocalNMEAChecksum localChecksum = new LocalNMEAChecksum();
    private static final String talkerId = "ST";
    private boolean haveBetterMTW;
    private boolean haveBetterVHW;
    
    @Rule("'\\xff\\xff'")
    protected void winPrefix()
    {
        //System.err.println("win");
    }
    @Rule("'\\xff\\x00'")
    protected void linuxPrefix()
    {
        //System.err.println("linux");
    }
    @Rule("'\\x00\\x02' b integer")
    protected void m00(
            char yz, 
            int xx, 
            @ParserContext("bb") ByteBuffer bb,
            @ParserContext("target") WritableByteChannel target
    ) throws IOException
    {
        int y = yz >> 4;
        int z = yz & 0xf;
        boolean anchorAlarm = (y & 8) == 8;
        boolean metric = (y & 4) == 4;
        boolean defect = (z & 4) == 4;
        boolean deepAlarm = (z & 2) == 2;
        boolean shallowAlarm = (z & 1) == 1;
        bb.flip();
        target.write(bb);
        bb.clear();
        NMEAGen.dbt(talkerId, bb, (float)xx/10F);
    }
    @Rule("'\\x01\\x05\\x00\\x00\\x00\\x60\\x01\\x00'")
    protected void m01a(
            @ParserContext("bb") ByteBuffer bb,
            @ParserContext("target") WritableByteChannel target
    ) throws IOException
    {
        send(bb, target);
        NMEAGen.txt(talkerId, bb, "Course Computer 400G");
    }
    @Rule("'\\x01\\x05\\x04\\xBA\\x20\\x28\\x01\\x00'")
    protected void m01b(
            @ParserContext("bb") ByteBuffer bb,
            @ParserContext("target") WritableByteChannel target
    ) throws IOException
    {
        send(bb, target);
        NMEAGen.txt(talkerId, bb, "ST60 Tridata");
    }
    @Rule("'\\x01\\x05\\x70\\x99\\x10\\x28\\x01\\x00'")
    protected void m01c(
            @ParserContext("bb") ByteBuffer bb,
            @ParserContext("target") WritableByteChannel target
    ) throws IOException
    {
        send(bb, target);
        NMEAGen.txt(talkerId, bb, "ST60 Log");
    }
    @Rule("'\\x01\\x05\\xF3\\x18\\x00\\x26\\x0F\\x06'")
    protected void m01d(
            @ParserContext("bb") ByteBuffer bb,
            @ParserContext("target") WritableByteChannel target
    ) throws IOException
    {
        send(bb, target);
        NMEAGen.txt(talkerId, bb, "ST80 Masterview");
    }
    @Rule("'\\x01\\x05\\xFA\\x03\\x00\\x30\\x07\\x03'")
    protected void m01e(
            @ParserContext("bb") ByteBuffer bb,
            @ParserContext("target") WritableByteChannel target
    ) throws IOException
    {
        send(bb, target);
        NMEAGen.txt(talkerId, bb, "ST80 Maxi Display");
    }
    @Rule("'\\x01\\x05\\xFF\\xFF\\xFF\\xD0\\x00\\x00'")
    protected void m01f(
            @ParserContext("bb") ByteBuffer bb,
            @ParserContext("target") WritableByteChannel target
    ) throws IOException
    {
        send(bb, target);
        NMEAGen.txt(talkerId, bb, "Smart Controller Remote Control Handset");
    }
    @Rule("'\\x20\\x01' integer")
    protected void m20(
            int xx, 
            @ParserContext("bb") ByteBuffer bb,
            @ParserContext("target") WritableByteChannel target
    ) throws IOException
    {
        send(bb, target);
        if (!haveBetterVHW)
        {
            float knots = (float)xx/10;
            NMEAGen.vhw(talkerId, bb, knots);
        }
    }
    @Rule("'\\x23\\x01' b b")
    protected void m23(
            char c, 
            char f, 
            @ParserContext("bb") ByteBuffer bb,
            @ParserContext("target") WritableByteChannel target
    ) throws IOException
    {
        send(bb, target);
        if (!haveBetterMTW)
        {
            NMEAGen.mtw(talkerId, bb, c);
        }
    }
    @Rule("'\\x24\\x02\\x00\\x00' b")
    protected void m24(
            char displayUnits,
            @ParserContext("bb") ByteBuffer bb,
            @ParserContext("target") WritableByteChannel target
    ) throws IOException
    {
    }
    @Rule("'\\x26\\x04' integer integer b")
    protected void m26(
            int xx, 
            int yy,
            char de,
            @ParserContext("bb") ByteBuffer bb,
            @ParserContext("target") WritableByteChannel target
    ) throws IOException
    {
        float knots = (float)xx/100;
        send(bb, target);
        haveBetterVHW = true;
        NMEAGen.vhw(talkerId, bb, knots);
    }
    @Rule("'\\x27\\x01' integer")
    protected void m27(
            int xx, 
            @ParserContext("bb") ByteBuffer bb,
            @ParserContext("target") WritableByteChannel target
    ) throws IOException
    {
        send(bb, target);
        haveBetterMTW = true;
        float temp = (float)(xx-100)/10;
        NMEAGen.mtw(talkerId, bb, temp);
    }
    @Rule("'\\x30\\x00' b")
    protected void m30(
            char cc, 
            @ParserContext("bb") ByteBuffer bb,
            @ParserContext("target") WritableByteChannel target
    ) throws IOException
    {
        send(bb, target);
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
        NMEAGen.txt(talkerId, bb, "Light L"+i);
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
            @ParserContext("bb") ByteBuffer bb,
            @ParserContext("target") WritableByteChannel target
    ) throws IOException
    {
        System.err.println(String.format("unknown 0x600x0c %02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X", c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c13));
    }
    @Rule("'\\x65\\x00\\x02'")
    protected void m65(
            @ParserContext("bb") ByteBuffer bb,
            @ParserContext("target") WritableByteChannel target
    ) throws IOException
    {
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
    public void parse(
            ScatteringByteChannel channel,
            @ParserContext("target") WritableByteChannel target
    ) throws IOException
    {
        ByteBuffer bb = ByteBuffer.allocate(80);
        parse(channel, bb, target);
    }
    @ParseMethod(start = "statements", size = 256, charSet = "US-ASCII" )
    protected abstract void parse(
            ScatteringByteChannel channel,
            @ParserContext("bb") ByteBuffer bb,
            @ParserContext("target") WritableByteChannel target
    ) throws IOException;
    @RecoverMethod
    public void recover(
            @ParserContext(ParserConstants.INPUTREADER) InputReader reader,
            @ParserContext(ParserConstants.THROWABLE) Throwable thr,
            @ParserContext("bb") ByteBuffer bb) throws IOException
    {
        int columnNumber = reader.getColumnNumber();
        int length = reader.getLength();
        String input = reader.getInput();
        int cc = input.charAt(0);
        System.err.println(Integer.toHexString(cc));
        reader.clear();
        bb.clear();
    }

    private void send(ByteBuffer bb, WritableByteChannel target) throws IOException
    {
        bb.flip();
        target.write(bb);
        bb.clear();
    }
}
