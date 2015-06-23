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

import java.io.IOException;
import java.nio.ByteBuffer;
import org.vesalainen.nmea.router.Router.Endpoint;
import org.vesalainen.nmea.script.AbstractScriptObjectFactory;
import org.vesalainen.nmea.script.ScriptStatement;

/**
 *
 * @author tkv
 */
public class EndpointScriptObjectFactory extends AbstractScriptObjectFactory<Boolean>
{
    private final Router router;
    private final Endpoint endpoint;

    public EndpointScriptObjectFactory(Router router, Endpoint endpoint)
    {
        this.router = router;
        this.endpoint = endpoint;
    }
    
    @Override
    public ScriptStatement<Boolean> createSender(String msg)
    {
        return new Sender(endpoint.name, msg);
    }

    @Override
    public ScriptStatement<Boolean> createSender(String to, String msg)
    {
        return new Sender(to, msg);
    }

    @Override
    public ScriptStatement<Boolean> createKiller(String target)
    {
        return new Killer(target);
    }

    private static class Killer implements ScriptStatement<Boolean>
    {
        private String target;
        public Killer(String target)
        {
            this.target = target;
        }

        @Override
        public Boolean exec() throws IOException
        {
            return router.kill(target);
        }
    }

    private class Sender implements ScriptStatement<Boolean>
    {
        private final String to;
        private final ByteBuffer bb;
        private Sender(String to, String msg)
        {
            this.to = to;
            this.bb = ByteBuffer.wrap(msg.getBytes());
        }

        @Override
        public Boolean exec() throws IOException
        {
            bb.clear();
            return router.send(to, bb);
        }
    }
    
}
