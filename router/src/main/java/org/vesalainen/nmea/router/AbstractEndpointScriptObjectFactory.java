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
import org.vesalainen.parsers.nmea.NMEAChecksum;

/**
 *
 * @author tkv
 */
public abstract class AbstractEndpointScriptObjectFactory<E> extends AbstractScriptObjectFactory<E>
{
    private final Router router;
    private final Endpoint endpoint;

    public AbstractEndpointScriptObjectFactory(Router router, Endpoint endpoint)
    {
        this.router = router;
        this.endpoint = endpoint;
    }

    @Override
    public ScriptStatement<Void,E> createRestarter()
    {
        return new Restarter();
    }
    
    @Override
    public ScriptStatement<Integer,E> createSender(String msg)
    {
        return new Sender(endpoint.name, msg);
    }

    @Override
    public ScriptStatement<Integer,E> createSender(String to, String msg)
    {
        return new Sender(to, msg);
    }

    @Override
    public ScriptStatement<Boolean,E> createKiller(String target)
    {
        return new Killer(target);
    }

    protected static ByteBuffer createMessage(String msg)
    {
        byte[] bytes = msg.getBytes();
        if (bytes.length < 5)
        {
            throw new IllegalArgumentException(msg+" is too short");
        }
        NMEAChecksum cs = new NMEAChecksum();
        cs.update(bytes, 0, bytes.length);
        ByteBuffer bb = ByteBuffer.allocate(bytes.length+5);
        bb.put(bytes, 0, bytes.length);
        cs.fillSuffix(bytes, 0, 5);
        bb.put(bytes, 0, 5);
        return bb;
    }

    private static class Restarter<E> implements ScriptStatement<Void,E>
    {

        public Restarter()
        {
        }

        @Override
        public Void exec(E engine) throws IOException, InterruptedException
        {
            throw new RestartException("restarted by script");
        }
        
        @Override
        public String toString()
        {
            return "restart()";
        }

    }

    private class Killer<E> implements ScriptStatement<Boolean,E>
    {
        private final String target;
        public Killer(String target)
        {
            this.target = target;
        }

        @Override
        public Boolean exec(E engine) throws IOException
        {
            return router.kill(target);
        }
        
        @Override
        public String toString()
        {
            return "kill(" + target + ')';
        }

    }

    private class Sender<E> implements ScriptStatement<Integer,E>
    {
        private final String to;
        private final ByteBuffer bb;
        private Sender(String to, String msg)
        {
            this.to = to;
            this.bb = createMessage(msg);
        }

        @Override
        public Integer exec(E engine) throws IOException
        {
            bb.clear();
            return router.send(to, bb);
        }

        @Override
        public String toString()
        {
            String m = new String(bb.array());
            return "send(" + to + ", " + m + ')';
        }

    }
    
}
