/*
 * Copyright (C) 2014 Timo Vesalainen
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

package org.vesalainen.parsers.mmsi;

import org.vesalainen.parser.GenClassFactory;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.ParseMethod;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.annotation.Rules;
import org.vesalainen.parser.annotation.Terminal;

/**
 *
 * @author Timo Vesalainen
 * @see <a href="http://en.wikipedia.org/wiki/Maritime_Mobile_Service_Identity">Maritime_Mobile_Service_Identity</a>
 * @see <a href="doc-files/MMSIParser-mmsi.html#BNF">BNF Syntax for MMSI</a>
 */
@GenClassname("org.vesalainen.parsers.mmsi.MMSIParserImpl")
@GrammarDef()
public abstract class MMSIParser
{
    /**
     * Returns MMSIParser instance
     * @return 
     */
    public static final MMSIParser getInstance()
    {
        return (MMSIParser) GenClassFactory.loadGenInstance(MMSIParser.class);
    }
    /**
     * Parses MMSI number
     * @param mmsi
     * @return 
     */
    public MMSIEntry parse(int mmsi)
    {
        String mmsiString = String.format("%09d", mmsi);
        return parse(mmsiString);
    }
    /**
     * Parses MMSI number
     * @param mmsi
     * @return 
     */
    @ParseMethod(start = "mmsi")
    public abstract MMSIEntry parse(String mmsi);
    
    @Rules({
    @Rule("shipStation"),
    @Rule("groupShipStation"),
    @Rule("coastStation"),
    @Rule("sarAircraft"),
    @Rule("handheldVHF"),
    @Rule("searchAndRescueTransponder"),
    @Rule("mobDevice"),
    @Rule("epirb"),
    @Rule("craftAssociatedWithParentShip"),
    @Rule("navigationalAid")
    })
    protected abstract MMSIEntry mmsi(MMSIEntry e);
    @Rule("mid xxxxxx")
    protected MMSIEntry shipStation(MIDEntry midEntry, int xxxxxx)
    {
        return new MMSIEntry(MMSIType.ShipStation, midEntry, xxxxxx);
    }
    @Rule("'0' mid xxxxx")
    protected MMSIEntry groupShipStation(MIDEntry midEntry, int xxxxx)
    {
        return new MMSIEntry(MMSIType.GroupShipStation, midEntry, xxxxx);
    }
    @Rule("'00' mid xxxx")
    protected MMSIEntry coastStation(MIDEntry midEntry, int xxxx)
    {
        return new MMSIEntry(MMSIType.CoastStation, midEntry, xxxx);
    }
    @Rule("'111' mid x xx")
    protected MMSIEntry sarAircraft(MIDEntry midEntry, int a, int xx)
    {
        return new MMSIEntry(MMSIType.SarAircraft, midEntry, a, xx);
    }
    @Rule("'8' xxxxxxxx")
    protected MMSIEntry handheldVHF(int xxxxxxxx)
    {
        return new MMSIEntry(MMSIType.HandheldVHF, xxxxxxxx);
    }
    @Rule("'970' xx xxxx")
    protected MMSIEntry searchAndRescueTransponder(int yy, int xxxx)
    {
        return new MMSIEntry(MMSIType.SearchAndRescueTransponder, yy, xxxx);
    }
    @Rule("'972' xx xxxx")
    protected MMSIEntry mobDevice(int yy, int xxxx)
    {
        return new MMSIEntry(MMSIType.MobDevice, yy, xxxx);
    }
    @Rule("'974' xx xxxx")
    protected MMSIEntry epirb(int yy, int xxxx)
    {
        return new MMSIEntry(MMSIType.EPIRB, yy, xxxx);
    }
    @Rule("'98' mid xxxx")
    protected MMSIEntry craftAssociatedWithParentShip(MIDEntry midEntry, int xxxx)
    {
        return new MMSIEntry(MMSIType.CraftAssociatedWithParentShip, midEntry, xxxx);
    }
    @Rule("'99' mid x xxx")
    protected MMSIEntry navigationalAid(MIDEntry midEntry, int a, int xxx)
    {
        return new MMSIEntry(MMSIType.NavigationalAid, midEntry, a, xxx);
    }
    @Terminal(expression="[0-9]{8}")
    protected abstract int xxxxxxxx(int x);
    @Terminal(expression="[0-9]{6}")
    protected abstract int xxxxxx(int x);
    @Terminal(expression="[0-9]{5}")
    protected abstract int xxxxx(int x);
    @Terminal(expression="[0-9]{4}")
    protected abstract int xxxx(int x);
    @Terminal(expression="[0-9]{3}")
    protected abstract int xxx(int x);
    @Terminal(expression="[0-9]")
    protected abstract int x(int x);
    @Terminal(expression="[0-9]{2}")
    protected abstract int xx(int x);
    @Terminal(expression="[2-7][0-9]{2}")
    protected MIDEntry mid(int mid)
    {
        return MID.get(mid);
    }
}
