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

import org.vesalainen.nmea.util.AbstractSampleConsumer;
import java.io.IOException;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.vesalainen.nmea.jaxb.router.ProcessorType;
import org.vesalainen.nmea.jaxb.router.SntpBroadcasterType;
import org.vesalainen.nmea.jaxb.router.SntpMulticasterType;
import org.vesalainen.nmea.jaxb.router.SntpServerType;
import org.vesalainen.nmea.jaxb.router.TimeSetterType;
import org.vesalainen.nmea.jaxb.router.TrackerType;
import org.vesalainen.nmea.jaxb.router.TrueWindSourceType;
import org.vesalainen.nmea.jaxb.router.VariationSourceType;
import org.vesalainen.nmea.util.NMEASample;
import org.vesalainen.parsers.nmea.NMEAService;

/**
 *
 * @author tkv
 */
public class Processor extends NMEAService implements Runnable, AutoCloseable
{
    private final ScatteringByteChannel in;
    private final GatheringByteChannel out;
    private ProcessorType processorType;
    private List<AbstractSampleConsumer> processes = new ArrayList<>();

    public Processor(ProcessorType processorType, ScatteringByteChannel in, GatheringByteChannel out) throws IOException
    {
        super(in, out);
        this.in = in;
        this.out = out;
        this.processorType = processorType;
    }

    @Override
    public void start()
    {
        super.start();
        try
        {
            for (Object ob : processorType.getVariationSourceOrTrueWindSourceOrTracker())
            {
                AbstractSampleConsumer process = null;
                if (ob instanceof VariationSourceType)
                {
                    VariationSourceType vst = (VariationSourceType) ob;
                    info("add VariationSource");
                    process = new VariationSource(out, vst);
                }
                if (ob instanceof TrueWindSourceType)
                {
                    TrueWindSourceType vst = (TrueWindSourceType) ob;
                    info("add TrueWindSource");
                    process = new TrueWindSource(out, vst);
                }
                if (ob instanceof TrackerType)
                {
                    TrackerType tt = (TrackerType) ob;
                    info("add Tracker");
                    process = new Tracker(tt);
                }
                if (ob instanceof SntpBroadcasterType)
                {
                    SntpBroadcasterType sntpBroadcasterType = (SntpBroadcasterType) ob;
                    warning("not supported SNTPBroadcaster");
                    //SNTPBroadcaster broadcaster = new SNTPBroadcaster(sntpBroadcasterType);
                    //addNMEAObserver(broadcaster);
                }
                if (ob instanceof SntpMulticasterType)
                {
                    SntpMulticasterType sntpMulticasterType = (SntpMulticasterType) ob;
                    warning("not supported SNTPMulticaster");
                    //SNTPMulticaster multicaster = new SNTPMulticaster(sntpMulticasterType);
                    //addNMEAObserver(multicaster);
                }
                if (ob instanceof TimeSetterType)
                {
                    TimeSetterType timeSetterType = (TimeSetterType) ob;
                    warning("not supported TimeSetterType");
                    //TimeSetter timeSetter = new TimeSetter(timeSetterType);
                    //addNMEAObserver(timeSetter);
                }
                if (ob instanceof SntpServerType)
                {
                    SntpServerType sntpServerType = (SntpServerType) ob;
                    warning("not supported SNTPServer");
                    //SNTPServer server = new SNTPServer(sntpServerType);
                    //addNMEAObserver(server);
                }
                if (process == null)
                {
                    throw new UnsupportedOperationException(ob+" not supported");
                }
                process.start(this);
                processes.add(process);
            }
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public void stop()
    {
        processes.stream().forEach((process) ->
        {
            process.stop();
        });
        super.stop();
    }
    
}
