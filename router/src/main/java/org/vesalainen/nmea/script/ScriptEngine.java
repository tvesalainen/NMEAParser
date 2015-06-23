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
package org.vesalainen.nmea.script;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.ScatteringByteChannel;
import java.util.List;
import java.util.zip.CheckedOutputStream;
import org.vesalainen.parser.GenClassFactory;
import org.vesalainen.parser.ParserConstants;
import static org.vesalainen.parser.ParserFeature.SingleThread;
import static org.vesalainen.parser.ParserFeature.SyntaxOnly;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.ParseMethod;
import org.vesalainen.parser.annotation.ParserContext;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.annotation.Rules;
import org.vesalainen.parser.annotation.Terminal;
import org.vesalainen.parser.util.InputReader;
import org.vesalainen.parsers.nmea.NMEAChecksum;
import org.vesalainen.parsers.seatalk.SeaTalk2NMEA;
import org.vesalainen.regex.Regex;

/**
 *
 * @author tkv
 */
@GenClassname("org.vesalainen.nmea.script.ScriptEngineImpl")
@GrammarDef
public abstract class ScriptEngine<R>
{
    @Rule(left = "statements", value = "statement*")
    protected List<ScriptStatement<R>> statements(List<ScriptStatement<R>> list)
    {
        return list;
    }
    @Rules({
        @Rule("send ';'"),
        @Rule("sleep ';'"),
        @Rule("kill ';'"),
        @Rule("loop ';'")
    })
    protected ScriptStatement<R> statement(ScriptStatement<R> ss)
    {
        return ss;
    }
    @Rule("'loop' '\\(' integer '\\)' '\\{' statements '\\}'")
    protected ScriptStatement<R> loop(Number times, List<ScriptStatement<R>> sList, @ParserContext("factory") ScriptObjectFactory<R> factory) throws IOException
    {
        ScriptStatement<R> looper = factory.createLooper(times.intValue(), sList);
        looper.exec();
        return looper;
    }
    @Rule("'kill' '\\(' identifier '\\)'")
    protected ScriptStatement<R> kill(String target, @ParserContext("factory") ScriptObjectFactory<R> factory) throws IOException
    {
        ScriptStatement<R> killer = factory.createKiller(target);
        killer.exec();
        return killer;
    }
    @Rule("'sleep' '\\(' integer '\\)'")
    protected ScriptStatement<R> sleep(Number millis, @ParserContext("factory") ScriptObjectFactory<R> factory) throws IOException
    {
        ScriptStatement<R> sleeper = factory.createSleeper(millis.longValue());
        sleeper.exec();
        return sleeper;
    }
    @Rule("'send' '\\(' string '\\)'")
    protected ScriptStatement<R> send(String msg, @ParserContext("factory") ScriptObjectFactory<R> factory) throws IOException
    {
        ScriptStatement<R> sender = factory.createSender(msg);
        sender.exec();
        return sender;
    }
    @Rule("'send' '\\(' identifier '\\,' string '\\)'")
    protected ScriptStatement<R> send(String to, String msg, @ParserContext("factory") ScriptObjectFactory<R> factory) throws IOException
    {
        ScriptStatement<R> sender = factory.createSender(to, msg);
        sender.exec();
        return sender;
    }
    public static <R> ScriptEngine<R> newInstance()
    {
        return (ScriptEngine<R>) GenClassFactory.loadGenInstance(ScriptEngine.class);
    }
    @ParseMethod(
            start = "statements", 
            features = {SingleThread},
            whiteSpace = {"whiteSpace", "doubleSlashComment", "hashComment", "cComment"}
    )
    public abstract List<ScriptStatement<R>> exec(String script, @ParserContext("factory") ScriptObjectFactory<R> factory) throws IOException;
    
    @ParseMethod(
            start = "statements", 
            features = {SyntaxOnly, SingleThread},
            whiteSpace = {"whiteSpace", "doubleSlashComment", "hashComment", "cComment"}
    )
    public abstract List<ScriptStatement<R>> check(String script) throws IOException;
    
    @Terminal(expression = "[a-zA-z][a-zA-z0-9_]*")
    protected abstract String identifier(String value);

    @Terminal(expression = "'[^']*'|\"[^\"]*\"|`[^´]´")
    protected String string(String value)
    {
        return value.substring(1, value.length() - 1);
    }

    @Terminal(expression = "[\\+\\-]?[0-9]+")
    protected Number integer(String value)
    {
        return Long.parseLong(value);
    }

    @Terminal(expression = "[\\+\\-]?[0-9]+\\.[0-9]+")
    protected Number decimal(String value)
    {
        return Double.parseDouble(value);
    }

    @Terminal(expression = "[ \t\r\n]+")
    protected abstract void whiteSpace();

    @Terminal(expression = "\\-\\-[^\n]*\n")
    protected void doubleSlashComment(
            @ParserContext(ParserConstants.INPUTREADER) InputReader reader
            )
    {
    }

    @Terminal(expression = "#[^\n]*\n")
    protected void hashComment(
            @ParserContext(ParserConstants.INPUTREADER) InputReader reader
            )
    {
    }

    @Terminal(expression = "/\\*.*\\*/", options =
    {
        Regex.Option.FIXED_ENDER
    })
    protected void cComment(
            @ParserContext(ParserConstants.INPUTREADER) InputReader reader
            )
    {
    }
}
