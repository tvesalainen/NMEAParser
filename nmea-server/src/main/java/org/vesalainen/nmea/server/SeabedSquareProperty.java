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
import org.vesalainen.nmea.server.anchor.AnchorManager;
import org.vesalainen.nmea.server.anchor.SeabedSurveyor.Square;
import org.vesalainen.nmea.server.jaxb.PropertyType;
import org.vesalainen.parsers.nmea.ais.AISService;
import org.vesalainen.parsers.nmea.ais.AISTarget;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SeabedSquareProperty extends ObjectProperty
{
    private final AnchorManager anchorManager;
    private static Map<Square,JSONBuilder.Obj> squareMap = new HashMap<>();
    
    public SeabedSquareProperty(CachedScheduledThreadPool executor, PropertyType property)
    {
        super(executor, property);
        this.anchorManager = AnchorManager.getInstance();
    }

    @Override
    public <T> void set(String property, long time, T arg)
    {
        if (arg instanceof Square)
        {
            Square square = (Square) arg;
            super.set(property, time, getObj(square));
        }
    }

    @Override
    protected void advertise(Observer observer)
    {
        observer.fireEvent(
            JSONBuilder
                .object()
                .value("name", ()->"anchorManager")
                .number("chainDiameter", anchorManager::getChainDiameter)
                .number("anchorWeight", anchorManager::getAnchorWeight)
                .number("maxChainLength", anchorManager::getMaxChainLength)
                .number("maxFairleadTension", anchorManager::getMaxFairleadTension)
                .number("boatLength", anchorManager::getBoatLength)
                .number("boatBeam", anchorManager::getBoatBeam)
            );
        anchorManager.forEachSquare((s)->observer.fireEvent(
                                JSONBuilder
                                    .object()
                                    .value("name", this::getName)
                                    .object("value", getObj(s))));
    }
    
    private static JSONBuilder.Obj getObj(Square square)
    {
        JSONBuilder.Obj obj = squareMap.get(square);
        if (obj == null)
        {
            obj = createObj(square);
            squareMap.put(square, obj);
        }
        return obj;
    }
    private static JSONBuilder.Obj createObj(Square target)
    {
        return 
            JSONBuilder
                .object()
                    .object("value")
                        .number("id", target::getId)
                        .number("lon", target::getLongitude)
                        .number("lat", target::getLatitude)
                        .number("depth", target::getDepth)
                        .number("standardDepth", target::getStandardDepth)
        ;
    }
    
}
