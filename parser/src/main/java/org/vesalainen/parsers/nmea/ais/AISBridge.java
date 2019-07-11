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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.vesalainen.util.Transactional;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AISBridge implements Transactional
{
    private final AISParser parser;
    private final AISObserver aisData;
    private final ExecutorService executor;
    private final Map<Integer,MessageHandler> map = new HashMap<>();
    private int current;

    public AISBridge(AISObserver aisData) throws IOException
    {
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
    )
    {
    }

    public void waitAndStopThreads()
    {
        executor.shutdownNow();
    }

    @Override
    public void start(String reason)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void rollback(String reason)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void commit(String reason)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    private class MessageHandler implements Runnable
    {
        private int message;
        private AISPipe channel;

        public void add(CharSequence data, int padding) throws IOException
        {
            channel.add(data, padding);
        }
        
        @Override
        public void run()
        {
            switch (message)
            {
                case 1:
                case 2:
                case 3:
                    parser.parse123Messages(channel, aisData, AISBridge.this);
                    break;
                default:
                    String methodName = "parse"+message+"Messages";
                    try
                    {
                        Method parseMethod = parser.getClass().getMethod(methodName, AISChannel.class, AISObserver.class, AISContext.class);
                        parseMethod.invoke(parser, channel, aisData, AISBridge.this);
                    }
                    catch (NoSuchMethodException  ex)
                    {
                        throw new IllegalArgumentException(methodName+" not implemented", ex);
                    }
                    catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
                    {
                        throw new IllegalArgumentException(ex);
                    }
                    break;
            }
        }
        
    }
}
