/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nmea.router.endpoint;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import static java.util.logging.Level.SEVERE;
import org.vesalainen.nio.RingByteBuffer;
import org.vesalainen.nmea.jaxb.router.TcpEndpointType;
import org.vesalainen.nmea.router.Router;
import static org.vesalainen.nmea.router.ThreadPool.POOL;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TCPListenerEndpoint extends Endpoint<TcpEndpointType,SocketChannel>
{
    private Set<TCPEndpoint> remotes = Collections.synchronizedSet(new HashSet<>());
    
    public TCPListenerEndpoint(TcpEndpointType tcpEndpointType, Router router)
    {
        super(tcpEndpointType, router);
    }

    @Override
    public SocketChannel createChannel() throws IOException
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void run()
    {
        try
        {
            int port = endpointType.getPort();
            InetSocketAddress socketAddress = new InetSocketAddress(port);
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(socketAddress);
            while (true)
            {
                SocketChannel socketChannel = serverSocketChannel.accept();
                TCPEndpoint tcpEndpoint = new TCPEndpoint(socketChannel, endpointType, router, bufferSize);
                POOL.submit(tcpEndpoint);
            }
        }
        catch (Throwable ex)
        {
            log(SEVERE, ex, "%s stopped because of %s", name, ex);
        }
    }

    @Override
    public int write(ByteBuffer bb) throws IOException
    {
        int count = 0;
        for (TCPEndpoint tcpEndpoint : remotes)
        {
            int cnt = tcpEndpoint.write(bb);
            count += cnt;
        }
        return count;
    }

    @Override
    public int write(RingByteBuffer ring) throws IOException
    {
        int count = 0;
        for (TCPEndpoint tcpEndpoint : remotes)
        {
            int cnt = tcpEndpoint.write(ring);
            count += cnt;
        }
        return count;
    }

    private class TCPEndpoint extends Endpoint<TcpEndpointType,SocketChannel>
    {
        private SocketChannel socket;

        public TCPEndpoint(SocketChannel socket, TcpEndpointType endpointType, Router router, int bufferSize)
        {
            super(endpointType, router);
            this.socket = socket;
        }
        
        @Override
        public SocketChannel createChannel() throws IOException
        {
            return socket;
        }

        @Override
        public void run()
        {
            try
            {
                remotes.add(this);
                super.run();
            }
            finally
            {
                remotes.remove(this);
            }
        }
        
    }
}
