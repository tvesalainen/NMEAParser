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

import java.util.HashMap;
import java.util.Map;
import org.vesalainen.parser.util.InputReader;

/**
 * @author Timo Vesalainen
 */
public class VesselMonitor extends AbstractAISObserver
{
    private int messageNumber;
    private int nmeaCount;
    private String lastCommit;
    private Map<Integer,Vessel> map = new HashMap<>();
    private Vessel target;

    @Override
    public void setPrefix(int numberOfSentences, int sentenceNumber, int sequentialMessageID, char channel)
    {
        nmeaCount++;
    }
    
    @Override
    public void setMessageType(MessageTypes messageTypes)
    {
        messageNumber = messageTypes.ordinal();
    }

    @Override
    public void setMMSI(int mmsi)
    {
        target = map.get(mmsi);
        if (target == null)
        {
            target = new Vessel(mmsi);
            map.put(mmsi, target);
        }
    }

    @Override
    public void rollback(String reason)
    {
        System.err.println(messageNumber+" failed "+reason+" at "+nmeaCount+" last="+lastCommit);
    }

    @Override
    public void commit(String reason)
    {
        if (sentenceNumber == numberOfSentences)
        {
            
        }
        messageNumber = 0;
        if (nmeaCount == 94)
        {
            System.err.println();
        }
        lastCommit = reason;
    }

    public void setNavigationStatus(NavigationStatus navigationStatus)
    {
        target.setNavigationStatus(navigationStatus);
    }

    public void setRateOfTurn(float degreesPerMinute)
    {
        target.setRateOfTurn(degreesPerMinute);
    }

    public void setSpeed(float knots)
    {
        target.setSpeed(knots);
    }

    public void setLongitude(float degrees)
    {
        target.setLongitude(degrees);
    }

    public void setLatitude(float degrees)
    {
        target.setLatitude(degrees);
    }

    public void setCourse(float cog)
    {
        target.setCourse(cog);
    }

    public void setYear(int year)
    {
        target.setYear(year);
    }

    public void setMonth(int month)
    {
        target.setMonth(month);
    }

    public void setDay(int day)
    {
        target.setDay(day);
    }

    public void setMinute(int minute)
    {
        target.setMinute(minute);
    }

    public void setHour(int hour)
    {
        target.setHour(hour);
    }

    public void setSecond(int second)
    {
        target.setSecond(second);
    }

    @Override
    public void setRAIM(boolean raim)
    {
        target.setRAIM(raim);
    }

    @Override
    public void setRadioStatus(int radio)
    {
        target.setRadioStatus(radio);
    }

    @Override
    public void setCallSign(String classSign)
    {
        target.setCallSign(classSign);
    }

}
