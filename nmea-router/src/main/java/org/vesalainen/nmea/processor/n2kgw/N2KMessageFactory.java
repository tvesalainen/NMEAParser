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
package org.vesalainen.nmea.processor.n2kgw;

import org.vesalainen.nmea.processor.n2kgw.AISSender;
import org.vesalainen.nmea.processor.n2kgw.AISCompiler;
import org.vesalainen.can.AbstractMessageFactory;
import org.vesalainen.can.SignalCompiler;
import org.vesalainen.can.dbc.MessageClass;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class N2KMessageFactory extends AbstractMessageFactory
{   
    private final PlainNMEACompiler nmeaCompiler;
    private final AISCompiler aisCompiler;

    public N2KMessageFactory(NMEASender nmeaSender, AISSender aisSender)
    {
        this.nmeaCompiler = new PlainNMEACompiler(nmeaSender);
        this.aisCompiler = new AISCompiler(aisSender);
    }
    
    @Override
    protected SignalCompiler getCompiler(MessageClass mc)
    {
        switch (mc.getTransmitter())
        {
            case "Ais":
                return aisCompiler;
            default:
                return nmeaCompiler;
        }
    }
        
}
