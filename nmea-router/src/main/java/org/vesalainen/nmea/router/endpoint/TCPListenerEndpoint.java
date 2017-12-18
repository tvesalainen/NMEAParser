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
import static java.net.StandardSocketOptions.SO_REUSEADDR;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;
import org.vesalainen.nio.RingByteBuffer;
import org.vesalainen.nio.SynchronizedRingByteBuffer;
import org.vesalainen.nmea.jaxb.router.TcpEndpointType;
import org.vesalainen.nmea.router.Router;
import static org.vesalainen.nmea.router.RouterManager.POOL;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TCPListenerEndpoint extends Endpoint<TcpEndpointType,SocketChannel>
{
    private AtomicInteger listenerCount = new AtomicInteger();
    private AtomicInteger seq = new AtomicInteger();
    
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
            Endpoint.endpointMap.put(name, this);
            int port = endpointType.getPort();
            InetSocketAddress socketAddress = new InetSocketAddress(port);
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.setOption(SO_REUSEADDR, true);
            serverSocketChannel.bind(socketAddress);
            while (true)
            {
                SocketChannel socketChannel = serverSocketChannel.accept();
                TCPEndpoint tcpEndpoint = new TCPEndpoint(socketChannel, endpointType, router);
                POOL.submit(tcpEndpoint);
            }
        }
        catch (Throwable ex)
        {
            log(SEVERE, ex, "%s stopped because of %s", name, ex);
        }
        finally
        {
            Endpoint.endpointMap.remove(name);
        }
    }

    @Override
    public int write(ByteBuffer bb) throws IOException
    {
        int count = 0;
        if (listenerCount.get() > 0)
        {
            for (Entry<String, Endpoint> entry : endpointMap.entrySet())
            {
                String key = entry.getKey();
                if (key.startsWith(name) && !key.equals(name))
                {
                    int cnt = entry.getValue().write(bb);
                    count += cnt;
                }
            }
        }
        return count;
    }

    @Override
    public int write(Endpoint src, RingByteBuffer ring) throws IOException
    {
        int count = 0;
        if (listenerCount.get() > 0)
        {
            for (Entry<String, Endpoint> entry : endpointMap.entrySet())
            {
                String key = entry.getKey();
                if (key.startsWith(name) && !key.equals(name))
                {
                    int cnt = entry.getValue().write(src, ring);
                    count += cnt;
                }
            }
        }
        return count;
    }

    private class TCPEndpoint extends Endpoint<TcpEndpointType,SocketChannel>
    {
        private SocketChannel socketChannel;
        private SynchronizedRingByteBuffer syncBuffer = new SynchronizedRingByteBuffer(1024, true);
        private Future<?> proxyFuture;

        public TCPEndpoint(SocketChannel socketChannel, TcpEndpointType endpointType, Router router)
        {
            super(endpointType, router, "-"+seq.incrementAndGet());
            this.socketChannel = socketChannel;
        }
        
        @Override
        public SocketChannel createChannel() throws IOException
        {
            return socketChannel;
        }

        @Override
        public int write(Endpoint src, RingByteBuffer ring) throws IOException
        {
            try
            {
                System.err.println("write="+ring.getString());
                int rc = syncBuffer.tryFillAll(ring);
                System.err.println("W="+rc+" "+syncBuffer);
                return rc;
            }
            catch (InterruptedException ex)
            {
                throw new IOException(ex);
            }
        }

        private void proxy()
        {
            try
            {
                while (true)
                {
                    syncBuffer.waitRemaining();
                    syncBuffer.getAll(false);
                    System.err.println("P "+syncBuffer);
                    super.write(this, syncBuffer);
                    syncBuffer.discard();
                }
            }
            catch (InterruptedException ex)
            {
                System.err.println("interrupted");
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
                log(SEVERE, ex, "%s %s", name, ex.getMessage());
            }
        }
        @Override
        public void run()
        {
            try
            {
                listenerCount.incrementAndGet();
                config("starting socket connection %s", socketChannel);
                proxyFuture = POOL.submit(this::proxy);
                super.run();
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
            finally
            {
                proxyFuture.cancel(true);
                listenerCount.decrementAndGet();
            }
        }
        
    }
}
