/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import org.vesalainen.util.CharSequences;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class NMEA
{
    /**
     * Returns start of message before first ',' or null if not valid message.
     * @param seq
     * @return 
     */
    public static final CharSequence getPrefix(CharSequence seq)
    {
        if (!isNMEAOrAIS(seq))
        {
            return null;
        }
        int idx = CharSequences.indexOf(seq, ',');
        if (idx == -1)
        {
            return null;
        }
        return seq.subSequence(0, idx);
    }
    /**
     * Finds first substring that starts with $/! and ends with \r\n. Returns
     * null otherwise. Doesn't check correctness of message.
     * @param seq
     * @return 
     */
    public static final CharSequence findMessage(CharSequence seq)
    {
        int begin = CharSequences.indexOf(seq, (c)->c=='$'||c=='!');
        if (begin == -1)
        {
            return null;
        }
        int end = CharSequences.indexOf(seq, "\r\n", begin+1);
        if (end != -1)
        {
            return seq.subSequence(begin, end+2);
        }
        return null;
    }
    /**
     * Return talker id of message or null if message is proprietary or invalid.
     * @param seq
     * @return 
     */
    public static final TalkerId getTalkerId(CharSequence seq)
    {
        if (!isNMEAOrAIS(seq))
        {
            return null;
        }
        if (CharSequences.startsWith(seq, "$P"))
        {
            return null;
        }
        return TalkerId.valueOf(seq.subSequence(1, 3).toString());
    }
    /**
     * Return message type of message or null if message is proprietary or invalid.
     * @param seq
     * @return 
     */
    public static final MessageType getMessageType(CharSequence seq)
    {
        if (!isNMEAOrAIS(seq))
        {
            return null;
        }
        if (CharSequences.startsWith(seq, "$P"))
        {
            return null;
        }
        return MessageType.valueOf(seq.subSequence(3, 6).toString());
    }
    /**
     * Returns true if message is valid and starts with '$P'
     * @param seq
     * @return 
     */
    public static final boolean isProprietory(CharSequence seq)
    {
        return isNMEAOrAIS(seq) && CharSequences.startsWith(seq, "$P");
    }
    /**
     * Returns true if message is valid NMEA message
     * @param seq
     * @return 
     */
    public static final boolean isNMEA(CharSequence seq)
    {
        if (seq.charAt(0) != '$')
        {
            return false;
        }
        return isNMEAOrAIS(seq);
    }
    /**
     * Returns true if message is valid AIS message
     * @param seq
     * @return 
     */
    public static final boolean isAIS(CharSequence seq)
    {
        if (seq.charAt(0) != '!')
        {
            return false;
        }
        return isNMEAOrAIS(seq);
    }
    /**
     * Returns true if message is valid NMEA or AIS message
     * @param seq
     * @return 
     */
    public static final boolean isNMEAOrAIS(CharSequence seq)
    {
        if (seq.charAt(0) != '$' && seq.charAt(0) != '!')
        {
            return false;
        }
        NMEAChecksum checkSum = new NMEAChecksum();
        checkSum.update(seq);
        String suffix = checkSum.getSuffix();
        return CharSequences.endsWith(seq, suffix);
    }
}
