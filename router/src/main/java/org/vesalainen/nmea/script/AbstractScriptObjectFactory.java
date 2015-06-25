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
 * along with this program.  If not, see <http://www.gnu.org/licenses/,E>.
 */
package org.vesalainen.nmea.script;

import java.io.IOException;
import java.util.List;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author tkv
 * @param <E> */
public abstract class AbstractScriptObjectFactory<E> implements ScriptObjectFactory<E>
{

    @Override
    public ScriptStatement<Void,E> createIf(ScriptStatement<Boolean,E> expr, ScriptStatement<?,E> successStat, ScriptStatement<?,E> elseStat)
    {
        return new If(expr, successStat, elseStat);
    }

    @Override
    public ScriptStatement<Void, E> createWhile(ScriptStatement<Boolean, E> expr, ScriptStatement<?, E> stat)
    {
        return new While(expr, stat);
    }

    @Override
    public ScriptStatement<Void,E> createBlock(List<ScriptStatement<?,E>> sList)
    {
        return new Block(sList);
    }

    @Override
    public ScriptStatement<Void,E> createSleeper(long millis)
    {
        return new Sleeper(millis);
    }

    @Override
    public ScriptStatement<Void,E> createLooper(int times, ScriptStatement stat)
    {
        return new Looper(times, stat);
    }

    private static class Sleeper<E> implements ScriptStatement<Void,E>
    {
        private final long millis;
        public Sleeper(long millis)
        {
            this.millis = millis;
        }

        @Override
        public synchronized Void exec(E engine) throws InterruptedException
        {
            wait(millis);
            return null;
        }

        @Override
        public String toString()
        {
            return "sleep(" + millis + ");";
        }
        
    }

    private static class Looper<E> extends JavaLogging implements ScriptStatement<Void,E>
    {
        private final int times;
        private final ScriptStatement statement;

        public Looper(int times, ScriptStatement statement)
        {
            this.times = times;
            this.statement = statement;
            setLogger(this.getClass());
        }
        @Override
        public Void exec(E engine) throws IOException, InterruptedException
        {
            config("loop: %d", times);
            for (int ii=0;ii<times;ii++)
            {
                statement.exec(engine);
            }
            return null;
        }

        @Override
        public String toString()
        {
            return "loop(" + times + ");";
        }
        
    }

    private static class Block<E> implements ScriptStatement<Void,E>
    {
        private final List<ScriptStatement<?,E>> sList;

        public Block(List<ScriptStatement<?,E>> sList)
        {
            this.sList = sList;
        }

        @Override
        public Void exec(E engine) throws IOException, InterruptedException
        {
            for (ScriptStatement<?,E> s : sList)
            {
                s.exec(engine);
            }
            return null;
        }
    }

    private static class If<E> implements ScriptStatement<Void,E>
    {
        private final ScriptStatement<Boolean,E> expr;
        private final ScriptStatement<?,E> successStat;
        private final ScriptStatement<?,E> elseStat;

        public If(ScriptStatement<Boolean,E> expr, ScriptStatement<?,E> successStat, ScriptStatement<?,E> elseStat)
        {
            this.expr = expr;
            this.successStat = successStat;
            this.elseStat = elseStat;
        }

        @Override
        public Void exec(E engine) throws IOException, InterruptedException
        {
            Boolean success = expr.exec(engine);
            if (success)
            {
                successStat.exec(engine);
            }
            else
            {
                if (elseStat != null)
                {
                    elseStat.exec(engine);
                }
            }
            return null;
        }
    }

    private static class While<E> implements ScriptStatement<Void, E>
    {
        private final ScriptStatement<Boolean,E> expr;
        private final ScriptStatement<?,E> stat;

        public While(ScriptStatement<Boolean, E> expr, ScriptStatement<?, E> stat)
        {
            this.expr = expr;
            this.stat = stat;
        }

        @Override
        public Void exec(E engine) throws IOException, InterruptedException
        {
            while (expr.exec(engine))
            {
                stat.exec(engine);
            }
            return null;
        }

    }
    
}
