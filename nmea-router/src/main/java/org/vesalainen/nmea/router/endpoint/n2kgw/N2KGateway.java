/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nmea.router.endpoint.n2kgw;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.*;
import java.util.concurrent.ExecutionException;
import org.vesalainen.can.AbstractCanService;
import org.vesalainen.can.candump.CanDumpService;
import org.vesalainen.can.j1939.AddressManager;
import org.vesalainen.can.j1939.ProductInformation;
import org.vesalainen.nmea.jaxb.router.N2KGatewayType;
import org.vesalainen.nmea.router.Version;
import org.vesalainen.nmea.util.Stoppable;
import static org.vesalainen.parsers.nmea.NMEAPGN.*;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class N2KGateway implements Stoppable
{
    private final AbstractCanService canService;
    private final NMEASender nmeaSender;
    private final AISSender aisSender;
    private final SourceManager sourceManager;

    public N2KGateway(AbstractCanService canService, NMEASender nmeaSender, AISSender aisSender, SourceManager sourceManager)
    {
        this.canService = canService;
        this.nmeaSender = nmeaSender;
        this.aisSender = aisSender;
        this.sourceManager = sourceManager;
    }

    public static N2KGateway getInstance(N2KGatewayType type, WritableByteChannel out, CachedScheduledThreadPool executor) throws IOException
    {
        SourceManager sourceManager = new SourceManager(type);
        NMEASender nmeaSender = new NMEASender(sourceManager, out);
        AISSender aisSender = new AISSender(out);
        AbstractCanService canService = AbstractCanService.openCan2Udp(type.getAddress(), type.getPort(), executor, new N2KMessageFactory(nmeaSender, aisSender, sourceManager));
        canService.addN2K();
        AddressManager addressManager = new AddressManager();
        ProductInformation info = addressManager.getOwnProductInformation();
        info.setManufacturerSModelId("NMEA Router N2K Gateway");
        info.setManufacturerSModelSerialCode(Version.getVersion());
        addressManager.addNameObserver(sourceManager::nameChanged);
        canService.setAddressManager(addressManager);
        return new N2KGateway(canService, nmeaSender, aisSender, sourceManager);
    }

    public static N2KGateway getInstance(String bus, Path in, Path out, CachedScheduledThreadPool executor) throws IOException
    {
        SeekableByteChannel ch = Files.newByteChannel(out, WRITE, CREATE, TRUNCATE_EXISTING);
        SourceManager sourceManager = new SourceManager();
        NMEASender nmeaSender = new NMEASender(sourceManager, ch);
        AISSender aisSender = new AISSender(ch);
        AbstractCanService canService = new CanDumpService(bus, in, executor, new N2KMessageFactory(nmeaSender, aisSender, sourceManager));
        canService.addN2K();
        AddressManager addressManager = new AddressManager();
        addressManager.addNameObserver(sourceManager::nameChanged);
        canService.setAddressManager(addressManager);
        return new N2KGateway(canService, nmeaSender, aisSender, sourceManager);
    }

    public void start()
    {
        canService.start();
    }
    
    @Override
    public void stop()
    {
        canService.stop();
    }

    public void startAndWait() throws InterruptedException, ExecutionException
    {
        canService.startAndWait();
    }
    public static int getPgnFor(String prefix)
    {
        switch (prefix)
        {
            case "RMC":
                return POSITION_RAPID_UPDATE.getPGN();
            case "DBT":
                return WATER_DEPTH.getPGN();
            case "HDT":
                return VESSEL_HEADING.getPGN();
            case "MTW":
                return ENVIRONMENTAL_PARAMETERS.getPGN();
            case "MWV":
                return WIND_DATA.getPGN();
            case "VHW":
                return SPEED_WATER_REFERENCED.getPGN();
            default:
                throw new UnsupportedOperationException(prefix+" not supported");
        }
    }

}
