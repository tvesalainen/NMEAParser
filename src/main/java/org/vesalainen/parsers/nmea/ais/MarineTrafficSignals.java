/*
 * Copyright (C) 2013 Timo Vesalainen
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

/**
 *
 * @author Timo Vesalainen
 */
public enum MarineTrafficSignals
{

    /**
     * N/A (default)
     */
    NADefault("N/A (default)"),
    /**
     * IALA port traffic signal 1: Serious emergency ἓ all vessels to stop
     */
    IALAPortTrafficSignal1SeriousEmergencyAllVesselsToStop("IALA port traffic signal 1: Serious emergency - all vessels to stop"),
    /**
     * IALA port traffic signal 2: Vessels shall not proceed.
     */
    IALAPortTrafficSignal2VesselsShallNotProceed("IALA port traffic signal 2: Vessels shall not proceed."),
    /**
     * IALA port traffic signal 3: Vessels may proceed. One way traffic.
     */
    IALAPortTrafficSignal3VesselsMayProceedOneWayTraffic("IALA port traffic signal 3: Vessels may proceed. One way traffic."),
    /**
     * IALA port traffic signal 4: Vessels may proceed. Two way traffic.
     */
    IALAPortTrafficSignal4VesselsMayProceedTwoWayTraffic("IALA port traffic signal 4: Vessels may proceed. Two way traffic."),
    /**
     * IALA port traffic signal 5: A vessel may proceed only when it has
     */
    IALAPortTrafficSignal5AVesselMayProceedOnlyWhenItHas("IALA port traffic signal 5: A vessel may proceed only when it has"),
    /**
     * IALA port traffic signal 2a: Vessels shall not proceed, except that
     */
    IALAPortTrafficSignal2aVesselsShallNotProceedExceptThat("IALA port traffic signal 2a: Vessels shall not proceed, except that"),
    /**
     * IALA port traffic signal 5a: A vessel may proceed only when it has
     */
    IALAPortTrafficSignal5aAVesselMayProceedOnlyWhenItHas("IALA port traffic signal 5a: A vessel may proceed only when it has"),
    /**
     * Japan Traffic Signal - I = "in-bound" only acceptable.
     */
    JapanTrafficSignalIInBoundOnlyAcceptable("Japan Traffic Signal - I = \"in-bound\" only acceptable."),
    /**
     * Japan Traffic Signal - O = "out-bound" only acceptable.
     */
    JapanTrafficSignalOOutBoundOnlyAcceptable("Japan Traffic Signal - O = \"out-bound\" only acceptable."),
    /**
     * Japan Traffic Signal - F = both "in- and out-bound" acceptable.
     */
    JapanTrafficSignalFBothInAndOutBoundAcceptable("Japan Traffic Signal - F = both \"in- and out-bound\" acceptable."),
    /**
     * Japan Traffic Signal - XI = Code will shift to "I" in due time.
     */
    JapanTrafficSignalXICodeWillShiftToIInDueTime("Japan Traffic Signal - XI = Code will shift to \"I\" in due time."),
    /**
     * Japan Traffic Signal - XO = Code will shift to "O" in due time.
     */
    JapanTrafficSignalXOCodeWillShiftToOInDueTime("Japan Traffic Signal - XO = Code will shift to \"O\" in due time."),
    /**
     * Japan Traffic Signal - X = Vessels shall not proceed, except a vessel
     */
    JapanTrafficSignalXVesselsShallNotProceedExceptAVessel("Japan Traffic Signal - X = Vessels shall not proceed, except a vessel"),
    /**
     * Reserved
     */
    Reserved("Reserved"),
    /**
     * Reserved
     */
    Reserved15("Reserved"),
    /**
     * Reserved
     */
    Reserved16("Reserved"),
    /**
     * Reserved
     */
    Reserved17("Reserved"),
    /**
     * Reserved
     */
    Reserved18("Reserved"),
    /**
     * Reserved
     */
    Reserved19("Reserved"),
    /**
     * Reserved
     */
    Reserved20("Reserved"),
    /**
     * Reserved
     */
    Reserved21("Reserved"),
    /**
     * Reserved
     */
    Reserved22("Reserved"),
    /**
     * Reserved
     */
    Reserved23("Reserved"),
    /**
     * Reserved
     */
    Reserved24("Reserved"),
    /**
     * Reserved
     */
    Reserved25("Reserved"),
    /**
     * Reserved
     */
    Reserved26("Reserved"),
    /**
     * Reserved
     */
    Reserved27("Reserved"),
    /**
     * Reserved
     */
    Reserved28("Reserved"),
    /**
     * Reserved
     */
    Reserved29("Reserved"),
    /**
     * Reserved
     */
    Reserved30("Reserved"),
    /**
     * Reserved
     */
    Reserved31("Reserved");
    private String description;

    MarineTrafficSignals(String description)
    {
        this.description = description;
    }

    @Override
    public String toString()
    {
        return description;
    }
}
