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
package org.vesalainen.nmea.sender;

import java.io.IOException;
import java.util.logging.Level;
import org.vesalainen.code.PropertySetter;
import org.vesalainen.nio.channels.UnconnectedDatagramChannel;
import org.vesalainen.nmea.jaxb.router.SenderType;
import org.vesalainen.nmea.jaxb.router.VariationSourceType;
import org.vesalainen.parsers.nmea.NMEAParser;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author tkv
 */
public class Sender extends JavaLogging implements Runnable
{
    private UnconnectedDatagramChannel channel;
    private final SenderType senderType;
    private final NMEADispatcher observer = NMEADispatcher.getInstance(NMEADispatcher.class);
    private final String address;
    private int port = 10110;

    public Sender(SenderType senderType) throws IOException
    {
        this.senderType = senderType;
        setLogger(this.getClass());
        address = senderType.getAddress();
        Integer p = senderType.getPort();
        if (p != null)
        {
            port = p;
        }
    }
    
    public void add(PropertySetter propertySetter)
    {
        observer.addObserver(propertySetter, propertySetter.getPrefixes());
    }
    @Override
    public void run()
    {
        config("open sender channel %s %d", address, port);
        try (UnconnectedDatagramChannel ch = UnconnectedDatagramChannel.open(address, port, 100, true, true))
        {
            channel = ch;
            VariationSourceType vst = senderType.getVariationSource();
            if (vst != null)
            {
                info("add VariationSource");
                VariationSource vs = new VariationSource(channel, vst);
                add(vs);
            }
            NMEAParser parser = NMEAParser.newInstance();
            parser.parse(channel, observer, null);
        }
        catch (IOException ex)
        {
            log(Level.SEVERE, "", ex);
        }
        log(Level.SEVERE, "Sender dies!!!");
    }

}
