/*
 * Copyright (C) 2022 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nmea.processor.n2kgw;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import org.vesalainen.can.AbstractCanService;
import org.vesalainen.can.dbc.DBC;
import org.vesalainen.can.j1939.AddressManager;
import org.vesalainen.nmea.router.endpoint.n2kgw.AISCompiler;
import org.vesalainen.nmea.router.endpoint.n2kgw.AISSender;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CompilerT
{
    @Test
    public void test() throws IOException, InterruptedException, ExecutionException
    {
        AbstractCanService canSvc = AbstractCanService.openCan2Udp("224.0.0.3", 11111, new AISCompiler(new AISSender(null)));
        DBC.addN2K();
        canSvc.compilePgn(368772383, DBC.getPgnMessage(129797));
        canSvc.startAndWait();
    }
    
}
