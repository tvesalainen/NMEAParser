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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import org.vesalainen.parser.util.InputReader;
import static org.vesalainen.parsers.nmea.ais.ThreadMessage.*;
import org.vesalainen.util.concurrent.SimpleWorkflow;

/**
 * @author Timo Vesalainen
 */
public class AISContext extends SimpleWorkflow<Integer,ThreadMessage,Void>
{
    
    private final AISObserver aisData;
    private final AISParser aisParser;
    private int current;
    private int numberOfSentences;
    private int sentenceNumber;
    private InputReader input;
    private boolean aisMessage;

    public AISContext(AISObserver aisData) throws IOException
    {
        super(-1, null, 1, 100, TimeUnit.MINUTES);  // one nmea and one ais thread in parallel!
        this.aisData = aisData;
        aisParser = AISParser.newInstance();
    }

    public void startOfSentence(
            InputReader input, 
            int numberOfSentences, 
            int sentenceNumber,
            int sequentialMessageID,
            char channel
            ) throws IOException
    {
        this.input = input;
        this.aisMessage = true;

        if (sentenceNumber == 1)
        {
            if (this.numberOfSentences != 0)
            {
                // new message without ending the last
                switchTo(current, Rollback);    // kill it
            }
            this.numberOfSentences = numberOfSentences;
            this.sentenceNumber = 1;
            aisData.start(null);
            aisData.setChannel(channel);
            switchTo(0, Go);
        }
        else
        {
            this.sentenceNumber++;
            if (this.sentenceNumber != sentenceNumber)
            {
                if (this.numberOfSentences != 0)
                {
                    switchTo(current, Rollback);    // this will kill ais thread
                }
                this.numberOfSentences = 0;
                this.sentenceNumber = 0;
            }
            else
            {
                switchTo(current, Go);
            }
        }
    }
    public void setMessageType(int messageType)
    {
        this.current = messageType;
        switchTo(messageType, Go);
    }

    public void afterChecksum(boolean committed, String reason)
    {
        if (this.numberOfSentences != 0)
        {
            if (committed)
            {
                if (this.numberOfSentences == this.sentenceNumber) // commit to ais
                {
                    this.numberOfSentences = 0;
                    this.sentenceNumber = 0;
                    switchTo(current, Commit);
                } // else more sentencies to come
            }
            else
            {
                this.numberOfSentences = 0;
                this.sentenceNumber = 0;
                switchTo(current, Rollback);
            }
        }
    }

    public void setOwnMessage(boolean b)
    {
        aisData.setOwnMessage(b);
    }

    public boolean isAisMessage()
    {
        return aisMessage;
    }

    public void setAisMessage(boolean aisMessage)
    {
        this.aisMessage = aisMessage;
    }

    @Override
    protected Runnable create(Integer key)
    {
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
                    //aisParser.parse(channel, aisData, context);
                    break;
                case 1:
                case 2:
                case 3:
                    //aisParser.parse123Messages(channel, aisData, context);
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
