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

import org.vesalainen.parsers.nmea.NMEADispatcher;
import java.io.IOException;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.util.ArrayList;
import java.util.List;
import org.vesalainen.nmea.jaxb.router.ProcessorType;
import org.vesalainen.nmea.jaxb.router.SntpBroadcasterType;
import org.vesalainen.nmea.jaxb.router.SntpMulticasterType;
import org.vesalainen.nmea.jaxb.router.SntpServerType;
import org.vesalainen.nmea.jaxb.router.TimeSetterType;
import org.vesalainen.nmea.jaxb.router.TrackerType;
import org.vesalainen.nmea.jaxb.router.TrueWindSourceType;
import org.vesalainen.nmea.jaxb.router.VariationSourceType;
import org.vesalainen.parsers.nmea.NMEAService;

/**
 *
 * @author tkv
 */
public class Processor extends NMEAService implements Runnable, AutoCloseable
{
    private final ScatteringByteChannel in;
    private final GatheringByteChannel out;
    private final ProcessorType processorType;
    private final NMEADispatcher observer = NMEADispatcher.getInstance(NMEADispatcher.class);
    private final List<AutoCloseable> autoCloseables = new ArrayList<>();
    private Thread thread;

    public Processor(ProcessorType processorType, ScatteringByteChannel in, GatheringByteChannel out) throws IOException
    {
        super(in, out);
        this.processorType = processorType;
        this.in = in;
        this.out = out;
        for (Object ob : processorType.getVariationSourceOrTrueWindSourceOrTracker())
        {
            if (ob instanceof VariationSourceType)
            {
                VariationSourceType vst = (VariationSourceType) ob;
                info("add VariationSource");
                VariationSource vs = new VariationSource(out, vst);
                addNMEAObserver(vs);
            }
            if (ob instanceof TrueWindSourceType)
            {
                TrueWindSourceType vst = (TrueWindSourceType) ob;
                info("add TrueWindSource");
                TrueWindSource vs = new TrueWindSource(out, vst);
                addNMEAObserver(vs);
            }
            if (ob instanceof TrackerType)
            {
                TrackerType tt = (TrackerType) ob;
                info("add Tracker");
                Tracker tracker = new Tracker(tt);
                addNMEAObserver(tracker);
            }
            if (ob instanceof SntpBroadcasterType)
            {
                SntpBroadcasterType sntpBroadcasterType = (SntpBroadcasterType) ob;
                info("add SNTPBroadcaster");
                SNTPBroadcaster broadcaster = new SNTPBroadcaster(sntpBroadcasterType);
                addNMEAObserver(broadcaster);
            }
            if (ob instanceof SntpMulticasterType)
            {
                SntpMulticasterType sntpMulticasterType = (SntpMulticasterType) ob;
                info("add SNTPMulticaster");
                SNTPMulticaster multicaster = new SNTPMulticaster(sntpMulticasterType);
                addNMEAObserver(multicaster);
            }
            if (ob instanceof TimeSetterType)
            {
                TimeSetterType timeSetterType = (TimeSetterType) ob;
                info("add TimeSetterType");
                TimeSetter timeSetter = new TimeSetter(timeSetterType);
                addNMEAObserver(timeSetter);
            }
            if (ob instanceof SntpServerType)
            {
                SntpServerType sntpServerType = (SntpServerType) ob;
                info("add SNTPServer");
                SNTPServer server = new SNTPServer(sntpServerType);
                addNMEAObserver(server);
            }
        }
    }
    
}
