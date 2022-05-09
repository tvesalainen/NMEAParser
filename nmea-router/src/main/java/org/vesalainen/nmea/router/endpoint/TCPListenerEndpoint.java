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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.vesalainen.nio.RingByteBuffer;
import org.vesalainen.nio.channels.ByteBufferPipe;
import org.vesalainen.nio.channels.ByteBufferPipe.Sink;
import org.vesalainen.nio.channels.ByteBufferPipe.Source;
import org.vesalainen.nmea.jaxb.router.TcpEndpointType;
import org.vesalainen.nmea.router.Router;
import static org.vesalainen.nmea.router.RouterManager.POOL;
import org.vesalainen.nmea.router.filter.MessageFilter;
import org.vesalainen.parsers.nmea.ais.AISService;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TCPListenerEndpoint extends Endpoint<TcpEndpointType,SocketChannel>
{
    private List<TCPEndpoint> clients = new CopyOnWriteArrayList<>();
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
            try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open())
            {
                serverSocketChannel.setOption(SO_REUSEADDR, true);
                serverSocketChannel.bind(socketAddress);
                while (true)
                {
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    TCPEndpoint tcpEndpoint = new TCPEndpoint(socketChannel, endpointType, router);
                    POOL.submit(tcpEndpoint);
                    if (endpointType.isAisFastBoot() != null && endpointType.isAisFastBoot())
                    {
                        if (!AISService.fastBoot(socketChannel))
                        {
                            warning("AISService.fastBoot not called");
                        }
                    }
                }
            }
        }
        catch (Throwable ex)
        {
            warnBrokenConnection(ex, "%s stopped because of %s", name, ex);
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
        for (TCPEndpoint tcpEndPoint : clients)
        {
            int cnt = tcpEndPoint.write(bb);
            count += cnt;
        }
        return count;
    }

    @Override
    public int write(Endpoint src, RingByteBuffer ring) throws IOException
    {
        int count = 0;
        Iterator<TCPEndpoint> iterator = clients.iterator();
        while (iterator.hasNext())
        {
            TCPEndpoint tcpEndPoint = iterator.next();
            int cnt = tcpEndPoint.write(src, ring);
            if (cnt == 0)
            {
                iterator.remove();
                warning("listener removed");
            }
            count += cnt;
        }
        return count;
    }

    private class TCPEndpoint extends Endpoint<TcpEndpointType,SocketChannel>
    {
        private final ByteBufferPipe pipe;
        private final Sink sink;
        private final Source source;
        private Future<?> proxyFuture;
        private final SocketChannel socketChannel;

        public TCPEndpoint(SocketChannel socketChannel, TcpEndpointType endpointType, Router router)
        {
            super(endpointType, router, "-"+seq.incrementAndGet());
            this.socketChannel = socketChannel;
            pipe = new ByteBufferPipe(1024, true);
            sink = pipe.sink();
            source = pipe.source();
            sink.setTimeout(0, TimeUnit.MILLISECONDS);
        }
        
        @Override
        public SocketChannel createChannel() throws IOException
        {
            return socketChannel;
        }

        @Override
        public int write(Endpoint src, RingByteBuffer ring) throws IOException
        {
            lastWrite = System.currentTimeMillis();
            int cnt = 0;
            if (channel != null)
            {
                if (filterList != null)
                {
                    for (MessageFilter filter : filterList)
                    {
                        if (!filter.accept(ring))
                        {
                            return 0;
                        }
                    }
                }
                cnt = ring.writeTo(sink);
                if (cnt == 0)
                {
                    warning("%s writeTo()=0", name);
                    proxyFuture.cancel(true);
                }
                finest("wrote rc=%d %s", cnt, pipe);
                writeCount++;
                writeBytes += cnt;
            }
            return cnt;
        }

        private void proxy()
        {
            try
            {
                started.await();
                while (true)
                {
                    source.writeTo(channel);
                }
            }
            catch (Throwable ex)
            {
                warning("Stopping proxy %s %s", name, ex.getMessage());
                warnBrokenConnection(ex, "Stopping proxy %s %s", name, ex.getMessage());
            }
        }
        @Override
        public void run()
        {
            try
            {
                config("starting socket connection %s", socketChannel);
                proxyFuture = POOL.submit(this::proxy);
                clients.add(this);
                super.run();
            }
            catch (Throwable ex)
            {
                warnBrokenConnection(ex, "Stopping %s %s", name, ex.getMessage());
            }
            finally
            {
                clients.remove(this);
                proxyFuture.cancel(true);
            }
        }
        
    }
}
