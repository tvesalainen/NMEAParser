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
package org.vesalainen.parsers.nmea;

import org.vesalainen.parser.annotation.ParserContext;
import org.vesalainen.parser.annotation.Rule;

/**
 *
 * @author tkv
 */
public class NMEATalkerIds extends NMEASentences
{

    @Rule(left = "talkerId", value = "'AB'")
    protected void ab(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.AB);
    }

    @Rule(left = "talkerId", value = "'AD'")
    protected void ad(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.AD);
    }

    @Rule(left = "talkerId", value = "'AG'")
    protected void ag(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.AG);
    }

    @Rule(left = "talkerId", value = "'AI'")
    protected void ai(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.AI);
    }

    @Rule(left = "talkerId", value = "'AP'")
    protected void ap(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.AP);
    }

    @Rule(left = "talkerId", value = "'BN'")
    protected void bn(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.BN);
    }

    @Rule(left = "talkerId", value = "'CC'")
    protected void cc(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.CC);
    }

    @Rule(left = "talkerId", value = "'CD'")
    protected void cd(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.CD);
    }

    @Rule(left = "talkerId", value = "'CM'")
    protected void cm(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.CM);
    }

    @Rule(left = "talkerId", value = "'CS'")
    protected void cs(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.CS);
    }

    @Rule(left = "talkerId", value = "'CT'")
    protected void ct(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.CT);
    }

    @Rule(left = "talkerId", value = "'CV'")
    protected void cv(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.CV);
    }

    @Rule(left = "talkerId", value = "'CX'")
    protected void cx(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.CX);
    }

    @Rule(left = "talkerId", value = "'DE'")
    protected void de(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.DE);
    }

    @Rule(left = "talkerId", value = "'DF'")
    protected void df(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.DF);
    }

    @Rule(left = "talkerId", value = "'DU'")
    protected void du(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.DU);
    }

    @Rule(left = "talkerId", value = "'EC'")
    protected void ec(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.EC);
    }

    @Rule(left = "talkerId", value = "'EP'")
    protected void ep(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.EP);
    }

    @Rule(left = "talkerId", value = "'ER'")
    protected void er(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.ER);
    }

    @Rule(left = "talkerId", value = "'GP'")
    protected void gp(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.GP);
    }

    @Rule(left = "talkerId", value = "'HC'")
    protected void hc(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.HC);
    }

    @Rule(left = "talkerId", value = "'HE'")
    protected void he(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.HE);
    }

    @Rule(left = "talkerId", value = "'HN'")
    protected void hn(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.HN);
    }

    @Rule(left = "talkerId", value = "'II'")
    protected void ii(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.II);
    }

    @Rule(left = "talkerId", value = "'IN'")
    protected void in(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.IN);
    }

    @Rule(left = "talkerId", value = "'LA'")
    protected void la(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.LA);
    }

    @Rule(left = "talkerId", value = "'LC'")
    protected void lc(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.LC);
    }

    @Rule(left = "talkerId", value = "'MP'")
    protected void mp(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.MP);
    }

    @Rule(left = "talkerId", value = "'NL'")
    protected void nl(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.NL);
    }

    @Rule(left = "talkerId", value = "'OM'")
    protected void om(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.OM);
    }

    @Rule(left = "talkerId", value = "'OS'")
    protected void os(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.OS);
    }

    @Rule(left = "talkerId", value = "'RA'")
    protected void ra(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.RA);
    }

    @Rule(left = "talkerId", value = "'SD'")
    protected void sd(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.SD);
    }

    @Rule(left = "talkerId", value = "'SN'")
    protected void sn(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.SN);
    }

    @Rule(left = "talkerId", value = "'SS'")
    protected void ss(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.SS);
    }

    @Rule(left = "talkerId", value = "'TI'")
    protected void ti(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.TI);
    }

    @Rule(left = "talkerId", value = "'TR'")
    protected void tr(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.TR);
    }

    @Rule(left = "talkerId", value = "'U0'")
    protected void u0(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.U0);
    }

    @Rule(left = "talkerId", value = "'U1'")
    protected void u1(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.U1);
    }

    @Rule(left = "talkerId", value = "'U2'")
    protected void u2(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.U2);
    }

    @Rule(left = "talkerId", value = "'U3'")
    protected void u3(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.U3);
    }

    @Rule(left = "talkerId", value = "'U4'")
    protected void u4(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.U4);
    }

    @Rule(left = "talkerId", value = "'U5'")
    protected void u5(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.U5);
    }

    @Rule(left = "talkerId", value = "'U6'")
    protected void u6(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.U6);
    }

    @Rule(left = "talkerId", value = "'U7'")
    protected void u7(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.U7);
    }

    @Rule(left = "talkerId", value = "'U8'")
    protected void u8(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.U8);
    }

    @Rule(left = "talkerId", value = "'U9'")
    protected void u9(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.U9);
    }

    @Rule(left = "talkerId", value = "'UP'")
    protected void up(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.UP);
    }

    @Rule(left = "talkerId", value = "'VD'")
    protected void vd(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.VD);
    }

    @Rule(left = "talkerId", value = "'DM'")
    protected void dm(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.DM);
    }

    @Rule(left = "talkerId", value = "'VW'")
    protected void vw(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.VW);
    }

    @Rule(left = "talkerId", value = "'WI'")
    protected void wi(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.WI);
    }

    @Rule(left = "talkerId", value = "'YC'")
    protected void yc(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.YC);
    }

    @Rule(left = "talkerId", value = "'YD'")
    protected void yd(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.YD);
    }

    @Rule(left = "talkerId", value = "'YF'")
    protected void yf(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.YF);
    }

    @Rule(left = "talkerId", value = "'YL'")
    protected void yl(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.YL);
    }

    @Rule(left = "talkerId", value = "'YP'")
    protected void yp(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.YP);
    }

    @Rule(left = "talkerId", value = "'YR'")
    protected void yr(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.YR);
    }

    @Rule(left = "talkerId", value = "'YT'")
    protected void yt(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.YT);
    }

    @Rule(left = "talkerId", value = "'YV'")
    protected void yv(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.YV);
    }

    @Rule(left = "talkerId", value = "'YX'")
    protected void yx(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.YX);
    }

    @Rule(left = "talkerId", value = "'ZA'")
    protected void za(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.ZA);
    }

    @Rule(left = "talkerId", value = "'ZC'")
    protected void zc(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.ZC);
    }

    @Rule(left = "talkerId", value = "'ZQ'")
    protected void zq(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.ZQ);
    }

    @Rule(left = "talkerId", value = "'ZV'")
    protected void zv(@ParserContext("data") NMEAObserver data)
    {
        data.start(null);
        data.setTalkerId(TalkerId.ZV);
    }

}
