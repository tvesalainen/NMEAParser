/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nmea.router.endpoint;

import org.vesalainen.comm.channel.SerialChannel;
import static org.vesalainen.comm.channel.SerialChannel.Speed.B38400;
import org.vesalainen.nmea.jaxb.router.Nmea0183HsType;
import org.vesalainen.nmea.router.Router;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class NmeaHsEndpoint extends SerialChannelEndpoint<Nmea0183HsType>
{

    public NmeaHsEndpoint(Nmea0183HsType nmea0183HsType, Router router)
    {
        super(nmea0183HsType, router);
    }
    
    @Override
    protected SerialChannel.Configuration createConfig()
    {
        SerialChannel.Configuration configure = super.createConfig();
        configure.setSpeed(B38400);
        return configure;
    }

}
