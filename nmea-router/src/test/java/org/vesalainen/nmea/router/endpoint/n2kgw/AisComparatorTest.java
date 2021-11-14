/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nmea.router.endpoint.n2kgw;

import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.can.j1939.PGN;
import org.vesalainen.parsers.nmea.ais.AISContentHelper;
import org.vesalainen.util.HexDump;
import org.vesalainen.util.HexUtil;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AisComparatorTest
{
    
    public AisComparatorTest()
    {
    }

    @Test
    public void test()
    {
        String h79 = "153F6B503BCFA22869CEBB9FE93DFFFFFFFFFFFFFFFF410E00011C01415354524F4C41424520524620324E4D204558434C5553494F4E4F4E4E";
        String h80 = "15406B503BFA8521698C6F9BE9C9FFFFFFFFFFFFFFFF410E00001E014252455749532053484F414C20312E354E4D204558434C5553494F4E4E";
        String h81 = "15416B503BCF931E69097599E90DFFFFFFFFFFFFFFFF410E00011E014F4B4150415255205245454620312E354E4D204558434C5553494F4E4E";
        String h82 = "15426B503B9ABF1A69D636AEE985FFFFFFFFFFFFFFFF410E00011F0150454E4755494E2053484F414C20312E304E4D204558434C5553494F4E";
        String h83 = "15436B503B3FD51769C2DDA2E9E9FFFFFFFFFFFFFFFF410E000016015055444E455920524F434B40404040404040404058434C5553494F4E4E";
        String h84 = "15446B503BDF661369148DC3E90DFFFFFFFFFFFFFFFF410E00001601545548554120524545464040404040404040404058434C5553494F4E4E";
        String h85 = "15456B503B3F7194691674A9E905FFFFFFFFFFFFFFFF410E00011F01564F4C4B4E455220524F434B5320322E304E4D204558434C5553494F4E";
        String h86 = "55466B503B05AB0269218396E991FFFFFFFFFFFFFFFF0A0E000016014120424541434F4E40404040404040404040404058434C5553494F4E4E";
        String h04 = "15586B503BEAD43769F27495E99DFFFFFFFFFFFFFFFF410E000116015343484F4F4E455220524F434B5340404040404058434C5553494F4E4E";
        byte[] fromString = HexUtil.fromString("55466B503B05AB0269218396E981FFFFFFFFFFFFFFFF0A0E000116014120424541434F4E40404040404040404040404058434C5553494F4E4E");
        String fromHex = HexDump.toHex(fromString);
        System.err.println(fromHex);
        int canId = PGN.canId(129041);
        AISContentHelper h1 = new AISContentHelper("!AIVDM,1,1,,A,18I>TH1000dV>aMbRgE1hbF62d0`,0*3A");
        AISContentHelper h2 = new AISContentHelper("!AIVDM,1,1,,A,18I>TH1wh0dV>aMbRgE1hbF62d0`,0*25");
        int i1 = h1.getInt(42, 49);
        int i2 = h2.getInt(42, 49);
    }
    
}
