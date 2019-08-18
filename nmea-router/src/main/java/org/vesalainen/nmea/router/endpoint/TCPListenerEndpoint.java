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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import static java.util.logging.Level.SEVERE;
import org.vesalainen.nio.RingByteBuffer;
import org.vesalainen.nio.channels.ByteBufferPipe;
import org.vesalainen.nio.channels.ByteBufferPipe.Sink;
import org.vesalainen.nio.channels.ByteBufferPipe.Source;
import org.vesalainen.nmea.jaxb.router.TcpEndpointType;
import org.vesalainen.nmea.router.Router;
import static org.vesalainen.nmea.router.RouterManager.POOL;
import org.vesalainen.nmea.router.filter.MessageFilter;
import org.vesalainen.parsers.nmea.ais.AISMonitor;
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
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.setOption(SO_REUSEADDR, true);
            serverSocketChannel.bind(socketAddress);
            while (true)
            {
                SocketChannel socketChannel = serverSocketChannel.accept();
                TCPEndpoint tcpEndpoint = new TCPEndpoint(socketChannel, endpointType, router);
                POOL.submit(tcpEndpoint);
                if (endpointType.isAisFastBoot() != null && endpointType.isAisFastBoot())
                {
                    AISService.fastBoot(socketChannel);
                }
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
        for (TCPEndpoint tcpEndPoint : clients)
        {
            int cnt = tcpEndPoint.write(src, ring);
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

        public TCPEndpoint(SocketChannel socketChannel, TcpEndpointType endpointType, Router router)
        {
            super(endpointType, router, "-"+seq.incrementAndGet());
            this.channel = socketChannel;
            pipe = new ByteBufferPipe(1024, true);
            sink = pipe.sink();
            source = pipe.source();
            sink.setTimeout(0, TimeUnit.MILLISECONDS);
        }
        
        @Override
        public SocketChannel createChannel() throws IOException
        {
            return channel;
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
                while (true)
                {
                    source.writeTo(channel);
                }
            }
            catch (Throwable ex)
            {
                log(SEVERE, ex, "Stopping proxy %s %s", name, ex.getMessage());
            }
        }
        @Override
        public void run()
        {
            try
            {
                clients.add(this);
                config("starting socket connection %s", channel);
                proxyFuture = POOL.submit(this::proxy);
                super.run();
            }
            catch (Throwable ex)
            {
                log(SEVERE, ex, "Stopping %s %s", name, ex.getMessage());
            }
            finally
            {
                proxyFuture.cancel(true);
                clients.remove(this);
            }
        }
        
    }
}
