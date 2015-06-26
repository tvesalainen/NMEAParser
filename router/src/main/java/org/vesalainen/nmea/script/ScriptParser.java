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
import java.util.List;
import org.vesalainen.parser.GenClassFactory;
import org.vesalainen.parser.ParserConstants;
import static org.vesalainen.parser.ParserFeature.SingleThread;
import static org.vesalainen.parser.ParserFeature.SyntaxOnly;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.ParseMethod;
import org.vesalainen.parser.annotation.ParserContext;
import org.vesalainen.parser.annotation.ReservedWords;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.annotation.Rules;
import org.vesalainen.parser.annotation.Terminal;
import org.vesalainen.parser.util.InputReader;
import org.vesalainen.regex.Regex;

/**
 *
 * @author tkv
 * @param 
 */
@GenClassname("org.vesalainen.nmea.script.ScriptParserImpl")
@GrammarDef
public abstract class ScriptParser<E>
{
    @ReservedWords({"if", "else"})
    protected void reservedWords()
    {
        
    }
    @Rule(left = "statements", value = "statement*")
    protected List<ScriptStatement<?,E>> statements(List<ScriptStatement<?,E>> list)
    {
        return list;
    }
    @Rules({
        @Rule("ifst"),
        @Rule("whilest"),
        @Rule("block"),
        @Rule("send"),
        @Rule("sleep"),
        @Rule("kill"),
        @Rule("restart"),
        @Rule("waitfor"),
        @Rule("loop")
    })
    protected ScriptStatement<?,E> statement(ScriptStatement<?,E> ss)
    {
        return ss;
    }
    @Rule("'if' '\\(' statement '\\)' block ( 'else' block )?")
    protected ScriptStatement<Void,E> ifst(
            ScriptStatement<?,E> expr, 
            ScriptStatement<?,E> successStat, 
            ScriptStatement<?,E> elseStat, 
            @ParserContext("factory") ScriptObjectFactory factory
    ) throws IOException, InterruptedException
    {
        ScriptStatement<Void,E> ifst = factory.createIf(ensureType(Boolean.class, expr), successStat, elseStat);
        return ifst;
    }
    @Rule("'while' '\\(' statement '\\)' block ")
    protected ScriptStatement<Void,E> whilest(
            ScriptStatement<?,E> expr, 
            ScriptStatement<?,E> stat, 
            @ParserContext("factory") ScriptObjectFactory factory
    ) throws IOException, InterruptedException
    {
        ScriptStatement<Void,E> whilest = factory.createWhile(ensureType(Boolean.class, expr), stat);
        return whilest;
    }
    @Rule("'\\{' statements '\\}'")
    protected ScriptStatement<Void,E> block(List<ScriptStatement<?,E>> sList, @ParserContext("factory") ScriptObjectFactory factory) throws IOException, InterruptedException
    {
        ScriptStatement<Void,E> block = factory.createBlock(sList);
        return block;
    }
    @Rule("'loop' '\\(' integer '\\)' block")
    protected ScriptStatement<Void,E> loop(Number times, ScriptStatement stat, @ParserContext("factory") ScriptObjectFactory factory) throws IOException, InterruptedException
    {
        ScriptStatement looper = factory.createLooper(times.intValue(), stat);
        return looper;
    }
    @Rule("'kill' '\\(' identifier '\\)'")
    protected ScriptStatement<Boolean,E> kill(String target, @ParserContext("factory") ScriptObjectFactory factory) throws IOException, InterruptedException
    {
        ScriptStatement<Boolean,E> killer = factory.createKiller(target);
        return killer;
    }
    @Rule("'restart' '\\(' '\\)'")
    protected ScriptStatement<Void,E> restart(@ParserContext("factory") ScriptObjectFactory factory) throws IOException, InterruptedException
    {
        ScriptStatement<Void,E> restarter = factory.createRestarter();
        return restarter;
    }
    @Rule("'sleep' '\\(' integer '\\)'")
    protected ScriptStatement<Void,E> sleep(Number millis, @ParserContext("factory") ScriptObjectFactory factory) throws IOException, InterruptedException
    {
        ScriptStatement<Void,E> sleeper = factory.createSleeper(millis.longValue());
        return sleeper;
    }
    @Rule("'waitfor' '\\(' integer '\\,' string '\\)'")
    protected ScriptStatement<Boolean,E> waitfor(Number millis, String msg, @ParserContext("factory") ScriptObjectFactory factory) throws IOException, InterruptedException
    {
        ScriptStatement<Boolean,E> waiter = factory.createWaiter(millis.longValue(), msg);
        return waiter;
    }
    @Rule("'send' '\\(' string '\\)'")
    protected ScriptStatement<Integer,E> send(String msg, @ParserContext("factory") ScriptObjectFactory factory) throws IOException, InterruptedException
    {
        ScriptStatement<Integer,E> sender = factory.createSender(msg);
        return sender;
    }
    @Rule("'send' '\\(' identifier '\\,' string '\\)'")
    protected ScriptStatement<Integer,E> send(String to, String msg, @ParserContext("factory") ScriptObjectFactory factory) throws IOException, InterruptedException
    {
        ScriptStatement<Integer,E> sender = factory.createSender(to, msg);
        return sender;
    }
    public static  ScriptParser newInstance()
    {
        return (ScriptParser) GenClassFactory.loadGenInstance(ScriptParser.class);
    }
    @ParseMethod(
            start = "statements", 
            features = {SingleThread},
            whiteSpace = {"whiteSpace", "hashComment", "cComment"}
    )
    public abstract List<ScriptStatement> exec(String script, @ParserContext("factory") ScriptObjectFactory factory);
    
    @ParseMethod(
            start = "statements", 
            features = {SyntaxOnly, SingleThread},
            whiteSpace = {"whiteSpace", "hashComment", "cComment"}
    )
    public abstract List<ScriptStatement> check(String script);
    
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
    private <T> ScriptStatement<T,E> ensureType(Class<T> expected, Object checked)
    {
        try
        {
            return (ScriptStatement<T,E>) checked;
        }
        catch (ClassCastException ex)
        {
            throw new IllegalArgumentException(checked+" is not ScriptStatement<"+expected+">");
        }
    }
}
