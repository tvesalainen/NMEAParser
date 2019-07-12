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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;
import org.vesalainen.util.Transactional;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AISBridge extends JavaLogging implements Transactional
{
    private final AISParser parser;
    private final AISObserver aisData;
    private final ExecutorService executor;
    private final MessageHandler[] handlers = new MessageHandler[27];
    private MessageHandler current;
    private Semaphore semaphore = new Semaphore(1);

    public AISBridge(AISObserver aisData) throws IOException
    {
        super(AISBridge.class);
        this.parser = AISParser.newInstance();
        this.aisData = aisData;
        executor = Executors.newCachedThreadPool();

    }

    public void newSentence(
            boolean ownMessage, 
            int numberOfSentences, 
            int sentenceNumber, 
            int sequentialMessageID, 
            char channel, 
            CharSequence payload, 
            int padding, 
            int checksum, 
            long value
    ) throws IOException
    {
        if (checksum != value)
        {
            warning("Message parsing was terminated because of checksum fail");
            if (current != null)
            {
                current.pipe.rollback();
            }
            current = null;
            return;
        }
        if (sentenceNumber == 1)    // first
        {
            if (current != null)
            {
                current.pipe.rollback();
            }
            int messageNumber = payload.charAt(0)-'0';
            current = getMessageHandler(messageNumber);
            current.pipe.start(ownMessage, (byte) channel);
        }
        if (current != null)
        {
            current.pipe.add(payload, padding);
            if (sentenceNumber == numberOfSentences)    // last
            {
                current.pipe.commit();
            }
        }
    }
    private MessageHandler getMessageHandler(int messageNumber) throws IOException
    {
        MessageHandler handler = handlers[messageNumber-1];
        if (handler == null || handler.future.isDone())
        {
            if (handler != null && handler.future.isDone())
            {
                warning("Message %d parsing was terminated", messageNumber);
            }
            handler = new MessageHandler(messageNumber);
            handlers[messageNumber-1] = handler;
        }
        return handler;
    }
    public void waitAndStopThreads() throws IOException
    {
        for (MessageHandler handler : handlers)
        {
            if (handler != null && !handler.future.isDone())
            {
                handler.pipe.exit();
            }
        }
        executor.shutdown();
        try
        {
            executor.awaitTermination(1, TimeUnit.MINUTES);
            executor.shutdownNow();
        }
        catch (InterruptedException ex)
        {
            log(SEVERE, ex, "%s", ex.getMessage());
        }
    }

    @Override
    public void start(String reason)
    {
        try
        {
            semaphore.acquire();
        }
        catch (InterruptedException ex)
        {
            log(SEVERE, ex, "%s", ex.getMessage());
        }
    }

    @Override
    public void rollback(String reason)
    {
        semaphore.release();
    }

    @Override
    public void commit(String reason)
    {
        semaphore.release();
    }
    private class MessageHandler implements Runnable
    {
        private int message;
        private AISPipe pipe;
        private final Future<?> future;

        public MessageHandler(int message) throws IOException
        {
            this.message = message;
            this.pipe = new AISPipe();
            future = executor.submit(this);
        }

        @Override
        public void run()
        {
            switch (message)
            {
                case 1:
                case 2:
                case 3:
                    parser.parse123Messages(pipe, aisData, AISBridge.this);
                    break;
                default:
                    String methodName = "parse"+message+"Messages";
                    try
                    {
                        Method parseMethod = parser.getClass().getMethod(methodName, ReadableByteChannel.class, AISObserver.class, AISBridge.class);
                        parseMethod.invoke(parser, pipe, aisData, AISBridge.this);
                    }
                    catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
                    {
                        log(SEVERE, ex, "%s", ex.getMessage());
                    }
                    break;
            }
            System.err.println();
        }

    }
}
