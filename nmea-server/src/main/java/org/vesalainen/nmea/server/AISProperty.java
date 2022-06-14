/*
 * Copyright (C) 2022 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nmea.server;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.vesalainen.json.JSONBuilder;
import org.vesalainen.nmea.server.jaxb.PropertyType;
import org.vesalainen.parsers.nmea.ais.AISService;
import org.vesalainen.parsers.nmea.ais.AISTarget;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AISProperty extends ObjectProperty
{
    private static Map<AISTarget,JSONBuilder.Obj> targetMap = new HashMap<>();
    
    public AISProperty(CachedScheduledThreadPool executor, PropertyType property)
    {
        super(executor, property);
    }

    @Override
    public <T> void set(String property, long time, T arg)
    {
        if (arg instanceof AISTarget)
        {
            AISTarget target = (AISTarget) arg;
            super.set(property, time, getObj(target));
        }
    }

    @Override
    protected void advertise(Observer observer)
    {
        AISService aisService = AISService.getInstance();
        Collection<AISTarget> liveSet = aisService.getLiveSet();
        liveSet.forEach((t)->observer.fireEvent(getObj(t)));
    }
    
    public static void remove(AISTarget target)
    {
        targetMap.remove(target);
    }
    private static JSONBuilder.Obj getObj(AISTarget target)
    {
        JSONBuilder.Obj obj = targetMap.get(target);
        if (obj == null)
        {
            obj = createObj(target);
            targetMap.put(target, obj);
        }
        return obj;
    }
    private static JSONBuilder.Obj createObj(AISTarget target)
    {
        return 
            JSONBuilder
                .object()
                    .object("value")
                        .number("mmsi", target::getMmsi)
                        .value("name", target::getVesselName)
                        .value("call", target::getCallSign)
                        .number("imo", target::getImoNumber)
                        .value("country", target::getCountry)
                        .number("length", target::getLength)
                        .number("beam", target::getBeam)
                        .number("draught", target::getDraught)
                        .bool("classA", target::isClassA)
                        .value("vendor", target::getVendorId)
                        .number("serial", target::getSerialNumber)
                        .value("mmsiType", target::getMMSIType)
                        .value("shipType", target::getShipType)
                        .value("maneuver", target::getManeuver)
                        .value("status", target::getNavigationStatus)
                        .number("cpaDistance", target::getCPADistance)
                        .number("cpaMinutes", target::getCPATime)
                        .value("lat", target::getLatitudeString)
                        .value("lon", target::getLongitudeString)
                        .number("alt", target::getAltitude)
                        .number("speed", target::getSpeed)
                        .number("cog", target::getCourse)
                        .number("hdg", target::getHeading)
                        .number("rot", target::getRateOfTurn)
                        .number("time", target::getTime)
                        .value("destination", target::getDestination)
                        .number("distance", target::getDistance)
        ;
    }
    
}
