/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.parsers.nmea.ais;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.*;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import static java.util.Locale.US;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import org.vesalainen.nio.channels.ChannelHelper;
import org.vesalainen.nio.channels.GZIPChannel;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AISLogFile implements Closeable
{
    private static final int MAX_BUF = 400;
    private Formatter formatter;
    private final ByteOut byteOut;
    private final OutputStreamWriter writer;
    private final Path path;
    private CachedScheduledThreadPool executor;
    private long maxLogSize;
    private final ReentrantLock lock = new ReentrantLock();
    private int version=-1;
    private CountDownLatch versionChanging;
    
    public AISLogFile(Path path, long maxLogSize, CachedScheduledThreadPool executor)
    {
        this.path = path;
        this.maxLogSize = maxLogSize;
        this.executor = executor;
        this.byteOut = new ByteOut();
        this.writer = new OutputStreamWriter(byteOut, UTF_8);
        this.formatter = new Formatter(writer);
    }

    public void format(String format, Object... args) throws IOException
    {
        lock.lock();
        try
        {
            ensureSize();
            formatter.format(US, format, args);
            formatter.flush();

        }
        finally
        {
            lock.unlock();
        }
    }
    private int version()
    {
        lock.lock();
        try
        {
            if (versionChanging != null)
            {
                versionChanging.await();
            }
            initVersion();
            return version;
        }
        catch (InterruptedException ex)
        {
            throw new IllegalArgumentException(ex);
        }
        finally
        {
            lock.unlock();
        }
    }
    private void initVersion()
    {
        lock.lock();
        try
        {
            if (version == -1)
            {
                int ver = 1;
                String base = path.getFileName().toString();
                while (true)
                {
                    Path trg = path.resolveSibling(base+"."+ver+".gz");
                    if (!Files.exists(trg))
                    {
                        break;
                    }
                    ver++;
                }
                version = ver-1;
            }
        }
        finally
        {
            lock.unlock();
        }
    }
    public List<Path> getPaths()
    {
        List<Path> list = new ArrayList<>();
        int ver = version();
        String base = path.getFileName().toString();
        for (int ii=1;ii<=ver;ii++)
        {
            list.add(path.resolveSibling(base+"."+ii+".gz"));
        }
        list.add(path);
        return list;
    }
    private Path nextVersion()
    {
        initVersion();
        String base = path.getFileName().toString();
        int ver = version+1;
        return path.resolveSibling(base+"."+ver+".gz");
    }
    private void ensureSize() throws IOException
    {
        flush(MAX_BUF);
        int size = byteOut.size();
        if (
                (Files.exists(path) && Files.size(path) + size>= maxLogSize) ||
                (size >= maxLogSize)
                )
        {
            versionChanging = new CountDownLatch(1);
            flush(0);
            Path tmp = Files.createTempFile(path.getParent(), path.getFileName().toString(), ".log");
            Files.move(path, tmp, REPLACE_EXISTING);
            Compressor compressor = new Compressor(tmp, path);
            executor.submit(compressor);
        }
    }
    public void flush() throws IOException
    {
        flush(0);
    }
    private void flush(int limit) throws IOException
    {
        if (byteOut.size() > limit)
        {
            byteOut.write();
        }
    }

    @Override
    public void close() throws IOException
    {
        flush(0);
    }
    private class ByteOut extends ByteArrayOutputStream
    {

        private ByteBuffer wrap;
        public void write() throws IOException
        {
            lock.lock();
            try (SeekableByteChannel channel = Files.newByteChannel(path, CREATE, APPEND))
            {
                if (wrap == null || wrap.array() != buf)
                {
                    wrap = ByteBuffer.wrap(buf);
                }
                wrap.position(0).limit(count);
                channel.write(wrap);
                reset();
            }
            finally
            {
                lock.unlock();
            }
        }
    }
    private class Compressor extends JavaLogging implements Runnable
    {
        private Path tmp;
        private Path log;

        public Compressor(Path tmp, Path log)
        {
            super(Compressor.class);
            this.tmp = tmp;
            this.log = log;
        }
        
        @Override
        public void run()
        {
            Path trg = nextVersion();
            try (FileChannel in = FileChannel.open(tmp, READ, DELETE_ON_CLOSE);
                    GZIPChannel out = new GZIPChannel(trg, CREATE, WRITE))
            {
                ByteBuffer bb = ByteBuffer.allocate(4096);
                int rc = in.read(bb);
                while (rc > 0)
                {
                    bb.flip();
                    ChannelHelper.writeAll(out, bb);
                    bb.clear();
                    rc = in.read(bb);
                }
            }
            catch (IOException ex)
            {
                log(Level.SEVERE, ex, "compress %s -> %s", tmp, trg);
            }
            finally
            {
                version++;
                versionChanging.countDown();
            }
        }
        
    }
}
