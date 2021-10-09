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

import org.vesalainen.can.dbc.MessageClass;
import org.vesalainen.can.j1939.PGN;
import org.vesalainen.parsers.nmea.NMEAPGN;
import org.vesalainen.parsers.nmea.TalkerId;
import static org.vesalainen.parsers.nmea.TalkerId.*;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SourceManager extends JavaLogging
{
    private Source[] map = new Source[256];

    public SourceManager()
    {
        super(SourceManager.class);
    }
    
    public TalkerId getTalkerId(int canId)
    {
        int sa = PGN.sourceAddress(canId);
        Source src = map[sa];
        if (src != null)
        {
            return src.getTalkerId();
        }
        return U0;
    }
    public void add(int canId, MessageClass mc)
    {
        int sa = PGN.sourceAddress(canId);
        Source src = map[sa];
        if (src == null)
        {
            src = new Source();
            map[sa] = src;
        }
        src.add(canId, sa, mc);
    }
    private class Source
    {
        private TalkerId talkerId = U0;

        public void add(int canId, int src, MessageClass mc)
        {
            String category = mc.getTransmitter();
            if ("Ais".equals(category))
            {
                finest("source(%d) %s -> %s", src, talkerId, AI);
                talkerId = AI;
            }
            else
            {
                int pgn = PGN.pgn(canId);
                NMEAPGN nmeaPgn = NMEAPGN.getForPgn(pgn);
                if (nmeaPgn == null)
                {
                    warning("pgn %d unknown", pgn);
                    return;
                }
                switch (talkerId)
                {
                    case U0:
                        switch (nmeaPgn)
                        {
                            case GNSS_POSITION_DATA:
                            case POSITION_RAPID_UPDATE:
                                finest("source(%d) %s -> %s", src, talkerId, GN);
                                talkerId = GN;
                                break;
                            case WATER_DEPTH:
                            case SPEED_WATER_REFERENCED:
                                finest("source(%d) %s -> %s", src, talkerId, SD);
                                talkerId = SD;
                                break;
                            case VESSEL_HEADING:
                                finest("source(%d) %s -> %s", src, talkerId, HC);
                                talkerId = HC;
                                break;
                            case WIND_DATA:
                                finest("source(%d) %s -> %s", src, talkerId, WI);
                                talkerId = WI;
                                break;
                        }
                        break;
                    case GN:
                        switch (nmeaPgn)
                        {
                            case VESSEL_HEADING:
                                finest("source(%d) %s -> %s", src, talkerId, HC);
                                talkerId = HC;
                                break;
                        }
                        break;
                }
            }
        }
        public TalkerId getTalkerId()
        {
            return talkerId;
        }
        
    }
}
