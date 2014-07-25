/*
 * Copyright (C) 2013 Timo Vesalainen
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
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import org.vesalainen.parser.util.InputReader;
import org.vesalainen.util.concurrent.SimpleWorkflow;

/**
 * @author Timo Vesalainen
 */
public class AISContext extends SimpleWorkflow<Integer>
{
    private final AISObserver aisData;
    private final AISParser aisParser;
    private int last;
    private int numberOfSentences;
    private InputReader input;

    public AISContext(AISObserver aisData) throws IOException
    {
        super(-1, Runtime.getRuntime().availableProcessors()+1, 5, TimeUnit.MINUTES);
        this.aisData = aisData;
        aisParser = AISParser.newInstance();
    }

    public void setInput(InputReader input)
    {
        this.input = input;
    }
    
    public AISObserver getAisData()
    {
        return aisData;
    }

    public void setNumberOfMessages(int numberOfMessages)
    {
        this.numberOfSentences = numberOfMessages;
    }

    boolean isLastMessage()
    {
        return --numberOfSentences == 0;
    }

    public int getLast()
    {
        return last;
    }

    public void setLast(int last)
    {
        this.last = last;
    }

    @Override
    protected Runnable create(Integer key)
    {
        System.err.println("Message "+key);
        return new AISThread(key, this);
    }
    class AISThread implements Runnable
    {
        private final int message;
        private final AISContext context;
        private final AISChannel channel;

        public AISThread(int message, AISContext context)
        {
            this.message = message;
            this.context = context;
            this.channel = new AISChannel(input, context);
        }
        
        @Override
        public void run()
        {
            switch (message)
            {
                case 0:
                    aisParser.parse(channel, aisData, context);
                    break;
                case 1:
                case 2:
                case 3:
                    aisParser.parse123Messages(channel, aisData, context);
                    break;
                default:
                    String methodName = "parse"+message+"Messages";
                    try
                    {
                        Method parser = aisParser.getClass().getMethod(methodName, AISChannel.class, AISObserver.class, AISContext.class);
                        parser.invoke(aisParser, channel, aisData, context);
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
