/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.io.IOException;
import java.time.Instant;
import java.util.function.Consumer;
import org.vesalainen.parser.GenClassFactory;
import org.vesalainen.parser.ParserConstants;
import org.vesalainen.parser.ParserInfo;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.ParseMethod;
import org.vesalainen.parser.annotation.ParserContext;
import org.vesalainen.parser.annotation.RecoverMethod;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.annotation.Terminal;
import org.vesalainen.parser.util.AbstractParser;
import org.vesalainen.parser.util.InputReader;
import org.vesalainen.regex.SyntaxErrorException;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
@GenClassname()
@GrammarDef()
public abstract class AISTargetDynamicParser extends AbstractParser implements ParserInfo
{
    public static final AISTargetDynamicParser PARSER = getInstance();
    
    private static AISTargetDynamicParser getInstance()
    {
        return (AISTargetDynamicParser) GenClassFactory.loadGenInstance(AISTargetDynamicParser.class);
    }
    
    @ParseMethod(start = "line", whiteSpace="whiteSpace")
    public abstract AISTargetDynamic parse(String line);
    
    @ParseMethod(start = "lines", whiteSpace="whiteSpace")
    public abstract <I> void parse(I input, @ParserContext("consumer") Consumer<AISTargetDynamic> consumer);
    
    @RecoverMethod
    public void recover(
            @ParserContext(ParserConstants.InputReader) InputReader reader,
            @ParserContext(ParserConstants.ExpectedDescription) String expected,
            @ParserContext(ParserConstants.LastToken) String got,
            @ParserContext(ParserConstants.Exception) Throwable thr
            ) throws IOException
    {
        if (thr != null && !(thr instanceof SyntaxErrorException))
        {
            throw new IOException(thr);
        }
        int cc = reader.read();
        while (cc != -1 && cc != '\n')
        {
            cc = reader.read();
        }
        if (cc == -1)
        {
            throw new SyntaxErrorException(reader.toString());
        }
        warning("skipped %s", reader);
        reader.clear();
    }
    @Rule(left="lines", value="line")
    protected void lines1(AISTargetDynamic line, @ParserContext("consumer") Consumer<AISTargetDynamic> consumer)
    {
        if (line != null)
        {
            consumer.accept(line);
        }
    }
    @Rule(left="lines", value="lines line")
    protected void lines2(AISTargetDynamic line, @ParserContext("consumer") Consumer<AISTargetDynamic> consumer)
    {
        if (line != null)
        {
            consumer.accept(line);
        }
    }
    @Rule(left="line", value="hashComment")
    protected AISTargetDynamic comment()
    {
        return null;
    }
    @Rule(left="line", value="message123 instant coordinate coordinate '\\,'? float float int float navigationStatus maneuver char")
    protected AISTargetDynamic line123(MessageTypes type, Instant instant, double latitude, double longitude, float course, float speed, int heading, float rateOfTurn, NavigationStatus navigationStatus, ManeuverIndicator maneuver, char channel)
    {
        return new AISTargetDynamic()
                .setMessageType(type)
                .setTimestamp(instant)
                .setLatitude(latitude)
                .setLongitude(longitude)
                .setCourse(course)
                .setSpeed(speed)
                .setHeading(heading)
                .setRateOfTurn(rateOfTurn)
                .setNavigationStatus(navigationStatus)
                .setManeuver(maneuver)
                .setChannel(channel);
    }
    @Rule(left="line", value="message5 instant old")
    protected AISTargetDynamic line5(MessageTypes type, Instant instant)
    {
        return null;
    }
    @Rule(left="line", value="message5 instant quote int int int int epfdFixTypes")
    protected AISTargetDynamic line5(MessageTypes type, Instant instant, String destination, int etaMonth, int etaDay, int etaHour, int etaMinute, EPFDFixTypes epfdFixTypes)
    {
        return null;
    }
    @Rule(left="line", value="message5 instant coordinate coordinate '\\,'? float float int char boolean")
    protected AISTargetDynamic line9(MessageTypes type, Instant instant, double latitude, double longitude, float course, float speed, int altitude, char channel, boolean assignedMode)
    {
        return new AISTargetDynamic()
                .setMessageType(type)
                .setTimestamp(instant)
                .setLatitude(latitude)
                .setLongitude(longitude)
                .setCourse(course)
                .setSpeed(speed)
                .setAltitude(altitude)
                .setChannel(channel)
                .setAssignedMode(assignedMode);
    }
    @Rule(left="line", value="message189 instant coordinate coordinate '\\,'? float float int char boolean")
    protected AISTargetDynamic line189(MessageTypes type, Instant instant, double latitude, double longitude, float course, float speed, int heading, char channel, boolean assignedMode)
    {
        return new AISTargetDynamic()
                .setMessageType(type)
                .setTimestamp(instant)
                .setLatitude(latitude)
                .setLongitude(longitude)
                .setCourse(course)
                .setSpeed(speed)
                .setHeading(heading)
                .setChannel(channel)
                .setAssignedMode(assignedMode);
    }
    
    @Rule(left="line", value="message24 instant old")
    protected AISTargetDynamic line24(MessageTypes type, Instant instant)
    {
        return null;
    }
    @Terminal(expression = "Msg[123]")
    protected MessageTypes message123(String msg)
    {
        return MessageTypes.values()[msg.charAt(3)-'0'];
    }
    @Terminal(expression = "Msg1[89]")
    protected MessageTypes message189(String msg)
    {
        return MessageTypes.values()[Integer.parseInt(msg.substring(3))];
    }
    @Terminal(expression = "Msg5")
    protected MessageTypes message5()
    {
        return MessageTypes.StaticAndVoyageRelatedData;
    }
    @Terminal(expression = "Msg24")
    protected MessageTypes message24()
    {
        return MessageTypes.StaticDataReport;
    }

    @Terminal(expression = "Msg9")
    protected MessageTypes message9()
    {
        return MessageTypes.StandardSARAircraftPositionReport;
    }
    @Terminal(expression = "[a-zA-z][a-zA-z0-9_]*")
    protected NavigationStatus navigationStatus(String status)
    {
        return NavigationStatus.valueOf(status);
    }
    @Terminal(expression = "[a-zA-z][a-zA-z0-9_]*")
    protected ManeuverIndicator maneuver(String maneuver)
    {
        return ManeuverIndicator.valueOf(maneuver);
    }
    @Terminal(expression = "[a-zA-z][a-zA-z0-9_]*")
    protected EPFDFixTypes epfdFixTypes(String epfdFixTypes)
    {
        return EPFDFixTypes.valueOf(epfdFixTypes);
    }
    @Terminal(expression = "\\{[^\\}]+\\}[\n]?")
    protected abstract void old();


    
}
