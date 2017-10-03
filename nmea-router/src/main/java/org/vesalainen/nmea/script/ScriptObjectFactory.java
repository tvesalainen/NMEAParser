/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.util.List;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <E>
 */
public interface ScriptObjectFactory<E>
{
    public ScriptStatement<Integer,E> createSender(String msg);

    public ScriptStatement<Integer,E> createSender(String to, String msg);

    public ScriptStatement<Void,E> createSleeper(long millis);

    public ScriptStatement<Boolean,E> createKiller(String target);

    public ScriptStatement<Void,E> createIf(ScriptStatement<Boolean,E> expr, ScriptStatement<?,E> successStat, ScriptStatement<?,E> elseStat);

    public ScriptStatement<Void,E> createWhile(ScriptStatement<Boolean,E> expr, ScriptStatement<?,E> stat);

    public ScriptStatement<Void,E> createBlock(List<ScriptStatement<?,E>> sList);

    public ScriptStatement<Void,E> createLooper(int times, ScriptStatement stat);

    public ScriptStatement<Boolean,E> createWaiter(long millis, String msg);

    public ScriptStatement<Void,E> createRestarter();
}
