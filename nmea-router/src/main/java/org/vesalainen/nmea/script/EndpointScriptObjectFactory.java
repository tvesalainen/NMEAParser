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

import java.io.IOException;
import org.vesalainen.nmea.router.Endpoint;
import org.vesalainen.nmea.script.RouterEngine;
import org.vesalainen.nmea.script.ScriptStatement;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class EndpointScriptObjectFactory extends AbstractEndpointScriptObjectFactory<EndpointScriptEngine>
{

    public EndpointScriptObjectFactory(RouterEngine router, Endpoint endpoint)
    {
        super(router, endpoint);
    }
    
    @Override
    public ScriptStatement<Boolean,EndpointScriptEngine> createWaiter(long millis, String msg)
    {
        return new Waiter(millis, msg);
    }

    private static class Waiter implements ScriptStatement<Boolean,EndpointScriptEngine>
    {
        private final long millis;
        private final String msg;

        public Waiter(long millis, String msg)
        {
            this.millis = millis;
            this.msg = msg;
        }

        @Override
        public Boolean exec(EndpointScriptEngine engine) throws IOException, InterruptedException
        {
            return engine.startWait(millis, msg);
        }
    }

}
