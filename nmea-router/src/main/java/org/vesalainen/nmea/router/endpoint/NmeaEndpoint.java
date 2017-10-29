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

import org.vesalainen.comm.channel.SerialChannel.Configuration;
import static org.vesalainen.comm.channel.SerialChannel.Speed.B4800;
import org.vesalainen.nmea.jaxb.router.Nmea0183Type;
import org.vesalainen.nmea.router.Router;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class NmeaEndpoint extends SerialChannelEndpoint<Nmea0183Type>
{

    public NmeaEndpoint(Nmea0183Type nmea0183Type, Router router)
    {
        super(nmea0183Type, router);
    }

    @Override
    protected Configuration createConfig()
    {
        Configuration configure = super.createConfig();
        configure.setSpeed(B4800);
        return configure;
    }

}
