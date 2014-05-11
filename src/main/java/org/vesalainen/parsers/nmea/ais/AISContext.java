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
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.vesalainen.util.concurrent.UnparallelWorkflow;

/**
 * @author Timo Vesalainen
 */
public class AISContext extends UnparallelWorkflow<Integer>
{
    private final AISObserver aisData;
    private final AISParser aisParser;
    private final InputStream in;
    private final SwitchingInputStream switchingInputStream;
    private final AISInputStream aisInputStream;
    private int last;

    public AISContext(InputStream in, AISObserver aisData) throws IOException
    {
        super(-1);
        this.aisData = aisData;
        this.in = in;
        switchingInputStream = new SwitchingInputStream(in, this);
        aisInputStream = new AISInputStream(switchingInputStream);
        aisParser = AISParser.newInstance();
    }
    
    public AISObserver getAisData()
    {
        return aisData;
    }

    public void setNumberOfMessages(int numberOfMessages)
    {
        switchingInputStream.setNumberOfSentences(numberOfMessages);
    }

    @Override
    public void switchTo(Integer to)
    {
        super.switchTo(to);
    }

    public int getLast()
    {
        return last;
    }

    public void setLast(int last)
    {
        this.last = last;
    }

    public void stopThreads()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected Runnable create(Integer key)
    {
        return new AISThread(key, this);
    }
    public class AISThread implements Runnable
    {
        private final int message;
        private final AISContext context;

        public AISThread(int message, AISContext context)
        {
            this.message = message;
            this.context = context;
        }
        
        @Override
        public void run()
        {
            switch (message)
            {
                case 0:
                    aisParser.parse(aisInputStream, aisData, context);
                    break;
                case 1:
                case 2:
                case 3:
                    aisParser.parse123Messages(aisInputStream, aisData);
                    break;
                default:
                    String methodName = "parse"+message+"Messages";
                    try
                    {
                        Method parser = aisParser.getClass().getMethod(methodName, InputStream.class, AISObserver.class);
                        parser.invoke(aisParser, aisInputStream, aisData);
                    }
                    catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
                    {
                        throw new IllegalArgumentException(methodName+" not implemented", ex);
                    }
                    break;
            }
        }

    }
}
