/*
 * Copyright (C) 2015 tkv
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
package org.vesalainen.nmea.processor;

import java.io.IOException;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.util.logging.Level;
import org.vesalainen.code.PropertySetter;
import org.vesalainen.nmea.jaxb.router.ProcessorType;
import org.vesalainen.nmea.jaxb.router.VariationSourceType;
import org.vesalainen.parsers.nmea.NMEAParser;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author tkv
 */
public class Processor extends JavaLogging implements Runnable
{
    private final ScatteringByteChannel in;
    private final GatheringByteChannel out;
    private final ProcessorType processorType;
    private final NMEADispatcher observer = NMEADispatcher.getInstance(NMEADispatcher.class);

    public Processor(ProcessorType processorType, ScatteringByteChannel in, GatheringByteChannel out) throws IOException
    {
        this.processorType = processorType;
        setLogger(this.getClass());
        this.in = in;
        this.out = out;
    }
    
    public void add(PropertySetter propertySetter)
    {
        observer.addObserver(propertySetter, propertySetter.getPrefixes());
    }
    
    @Override
    public void run()
    {
        try
        {
            for (VariationSourceType vst : processorType.getVariationSource())
            {
                if (vst instanceof VariationSourceType)
                {
                    info("add VariationSource");
                    VariationSource vs = new VariationSource(out, vst);
                    add(vs);
                }
            }
            NMEAParser parser = NMEAParser.newInstance();
            parser.parse(in, observer, null);
        }
        catch (IOException ex)
        {
            log(Level.SEVERE, "", ex);
        }
        log(Level.SEVERE, "Processor dies!!!");
    }

}
