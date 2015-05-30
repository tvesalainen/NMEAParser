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
package org.vesalainen.nmea.router;

import org.vesalainen.comm.channel.SerialChannel;
import org.vesalainen.util.SimpleMatcher;

/**
 *
 * @author tkv
 */
public class SeaTalkMatcher extends SimpleMatcher
{
    private int index=-1;
    private int end=-1;
    public SeaTalkMatcher()
    {
        super(SerialChannel.getErrorReplacement());
    }

    @Override
    public Status match(int cc)
    {
        if (index >= 0)
        {
            switch (index)
            {
                case 0:
                    break;
                case 1:
                    end = (cc&0xf)+2;
                    break;
                default:
                    if (index == end)
                    {
                        index = -1;
                        return Status.Match;
                    }
                    break;
            }
            index++;
            return Status.Ok;
        }
        else
        {
            Status status = super.match(cc);
            if (status == Status.Match)
            {
                index = 0;
                end = -1;
                return Status.Ok;
            }
            else
            {
                return status;
            }
        }
    }
    
}
