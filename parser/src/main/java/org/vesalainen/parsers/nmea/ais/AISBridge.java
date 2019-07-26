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
import static java.util.logging.Level.SEVERE;
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
    private boolean ownMessage;
    private int numberOfSentences;
    private int nextSentenceNumber;
    private int sequentialMessageID;
    private char channel;

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
        fine("AIS(%d, %d, %s)", numberOfSentences, sentenceNumber, payload);
        if (checksum != value)
        {
            warning("Message parsing was terminated because of checksum fail");
            if (current != null)
            {
                current.pipe.rollback();
                fine("AIS rollback checksum failed");
            }
            current = null;
            return;
        }
        if (sentenceNumber == 1)    // first
        {
            if (current != null)
            {
                current.pipe.rollback();
                fine("AIS rollback rest of message missing");
            }
            int messageNumber = payload.charAt(0)-'0';
            messageNumber = messageNumber < 4 ? 1 : messageNumber;
            current = getMessageHandler(messageNumber);
            current.pipe.start(ownMessage, (byte) channel);
            fine("AIS sent start");
            setExpected(ownMessage, numberOfSentences, sequentialMessageID, channel);
        }
        else
        {
            if (current != null)
            {
                if (!expected(ownMessage, numberOfSentences, sentenceNumber, sequentialMessageID, channel))
                {
                    current.pipe.rollback();
                    fine("AIS rollback sequence error");
                    current = null;
                    return;
                }
            }
        }
        if (current != null)
        {
            current.pipe.add(payload, padding);
            fine("AIS sent payload");
            if (sentenceNumber == numberOfSentences)    // last
            {
                current.pipe.commit();
                current = null;
                fine("AIS commit");
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
            config("AIS created new message handler %d", messageNumber);
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
                fine("AIS terminated %d", handler.message);
            }
        }
        executor.shutdown();
        try
        {
            executor.awaitTermination(10, TimeUnit.MINUTES);
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
            finest("AIS waiting for permit");
            semaphore.acquire();
            finest("AIS got permit");
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
        finest("AIS released permit (rollback)");
    }

    @Override
    public void commit(String reason)
    {
        semaphore.release();
        finest("AIS released permit (commit)");
    }

    private boolean expected(
            boolean ownMessage, 
            int numberOfSentences, 
            int sentenceNumber, 
            int sequentialMessageID, 
            char channel
    )
    {
        if (
            sentenceNumber != nextSentenceNumber ||
            ownMessage != this.ownMessage ||
            numberOfSentences != this.numberOfSentences ||
            sequentialMessageID != this.sequentialMessageID ||
            channel != this.channel
                )
        {
            return false;
        }
        else
        {
            nextSentenceNumber++;
            return true;
        }
    }

    private void setExpected(boolean ownMessage, int numberOfSentences, int sequentialMessageID, char channel)
    {
        this.nextSentenceNumber = 2;
        this.ownMessage = ownMessage;
        this.numberOfSentences = numberOfSentences;
        this.sequentialMessageID = sequentialMessageID;
        this.channel = channel;
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
            fine("AIS exit %d", message);
        }

    }
}
