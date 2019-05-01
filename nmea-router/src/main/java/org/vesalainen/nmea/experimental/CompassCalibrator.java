/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nmea.experimental;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.vesalainen.code.AbstractPropertySetter;
import org.vesalainen.math.UnitType;
import static org.vesalainen.math.UnitType.*;
import org.vesalainen.navi.BoatPosition;
import org.vesalainen.navi.Navis;
import org.vesalainen.navi.WayPoint;
import org.vesalainen.nmea.processor.deviation.DeviationBuilder;
import org.vesalainen.parsers.nmea.MessageType;
import org.vesalainen.parsers.nmea.NMEADispatcher;
import org.vesalainen.parsers.nmea.NMEAParser;
import org.vesalainen.parsers.nmea.NMEAService;
import org.vesalainen.parsers.nmea.ais.AISDispatcher;
import org.vesalainen.parsers.nmea.ais.MessageTypes;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;
import org.vesalainen.util.logging.JavaLogging;
import org.vesalainen.util.navi.Location;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CompassCalibrator extends JavaLogging
{
    private static final double DISTANCE_TOLERANCE = UnitType.convert(10, Meter, NM);
    private static final double DEGREE_TOLERANCE = 10;
    private static final long TIME_TOLERANCE = 2000;
    private Path path;
    private CachedScheduledThreadPool executor;
    private Map<Integer,AISTarget> aisTargets = new ConcurrentHashMap<>();
    private Map<Integer,ARPATarget> arpaTargets = new ConcurrentHashMap<>();
    private DeviationBuilder builder;
    private final double variation;
    private NMEAObserverImpl nmeaObserver;
    private AISObserverImpl aisObserver;

    public CompassCalibrator(Path path, double variation)
    {
        this(new DeviationBuilder(path, variation), path, variation, new CachedScheduledThreadPool());
    }
    public CompassCalibrator(DeviationBuilder builder, Path path, double variation, CachedScheduledThreadPool executor)
    {
        super(CompassCalibrator.class);
        this.path = path;
        this.variation = variation;
        this.executor = executor;
        this.builder = builder;
    }
    public void attach(NMEAService svc)
    {
        nmeaObserver = new NMEAObserverImpl();
        aisObserver = new AISObserverImpl();
        svc.addNMEAObserver(nmeaObserver);
        svc.addAISObserver(aisObserver);
    }
    public void detach(NMEAService svc)
    {
        svc.removeNMEAObserver(nmeaObserver);
        svc.removeAISObserver(aisObserver);
    }
    public void run() throws IOException, InterruptedException
    {
        NMEADispatcher nmeaDispatcher = NMEADispatcher.getInstance(NMEADispatcher.class);
        nmeaObserver = new NMEAObserverImpl();
        nmeaDispatcher.addObserver(nmeaObserver, nmeaObserver.getPrefixes());
        AISDispatcher aisDispatcher = AISDispatcher.getInstance(AISDispatcher.class);
        aisObserver = new AISObserverImpl();
        aisDispatcher.addObserver(aisObserver, aisObserver.getPrefixes());
        NMEAParser parser = NMEAParser.newInstance();
        parser.parse(path, false, path::toString, nmeaDispatcher, aisDispatcher);
        executor.awaitTermination(1, TimeUnit.DAYS);
    }
    private void process(AISTarget ais, AISInstance aisIns)
    {
        info("process %s %d", ais, aisIns.time);
        arpaTargets.values().forEach((a)->process(a, ais, aisIns));
    }
    private void process(ARPATarget arpa, AISTarget ais, AISInstance aisIns)
    {
        info("process %s %s", ais, arpa);
        ARPAInstance arpaIns = arpa.getClosest(aisIns.time);
        if (arpaIns != null)
        {
            process(ais, arpaIns, aisIns);
        }
    }
    private void process(AISTarget ais, ARPAInstance arpaIns, AISInstance aisIns)
    {
        double aisLat = ais.getLatitude(aisIns);
        double aisLon = ais.getLongitude(aisIns);
        double distanceToAis = Navis.distance(arpaIns.ownLat, arpaIns.ownLon, aisLat, aisLon);
        if (diff(distanceToAis, arpaIns.targetDistance) > DISTANCE_TOLERANCE)
        {
            info("%s too far %.0fm %.0fm", ais, UnitType.convert(distanceToAis, NM, Meter), UnitType.convert(arpaIns.targetDistance, NM, Meter));
            return;
        }
        double bearingToAis = Navis.bearing(arpaIns.ownLat, arpaIns.ownLon, aisLat, aisLon);
        double angleDiff = Navis.angleDiff(arpaIns.bearingFromOwnShip, bearingToAis);
        if (Math.abs(angleDiff) > DEGREE_TOLERANCE)
        {
            info("angle too big %f", angleDiff);
        }
        else
        {
            info("correct %s %f %f %f", ais, arpaIns.trueHeading, arpaIns.bearingFromOwnShip, bearingToAis);
            builder.correct(arpaIns.trueHeading, arpaIns.bearingFromOwnShip, bearingToAis);
        }
    }
    private double diff(double x, double y)
    {
        return Math.abs(x-y);
    }
    private class ARPATarget
    {
        private int targetNumber;
        private NavigableMap<Long,ARPAInstance> instances = new TreeMap<>();

        public ARPATarget(int targetNumber)
        {
            this.targetNumber = targetNumber;
        }
        
        void add(ARPAInstance instant)
        {
            instances.put(instant.time, instant);
        }
        ARPAInstance getClosest(Long time)
        {
            if (!instances.isEmpty())
            {
                Map.Entry<Long, ARPAInstance> entry = getClosestEntry(time);
                if (entry != null && Math.abs(time - entry.getKey()) <= TIME_TOLERANCE)
                {
                    return entry.getValue();
                }
            }
            return null;
        }
        Map.Entry<Long, ARPAInstance> getClosestEntry(Long time)
        {
            Map.Entry<Long, ARPAInstance> floor = instances.floorEntry(time);
            Map.Entry<Long, ARPAInstance> ceiling = instances.ceilingEntry(time);
            if (floor != null && ceiling != null)
            {
                if (time - floor.getKey() < ceiling.getKey() - time)
                {
                    return floor;
                }
                else
                {
                    return ceiling;
                }
            }
            else
            {
                if (floor != null)
                {
                    return floor;
                }
                else
                {
                    if (ceiling != null)
                    {
                        return ceiling;
                    }
                    else
                    {
                        return null;
                    }
                }
            }
        }

        @Override
        public String toString()
        {
            return "ARPATarget{" + targetNumber + '}';
        }
    }
    private class ARPAInstance
    {
        private long time;
        private float targetDistance;
        private float bearingFromOwnShip;
        private double ownLon;
        private double ownLat;
        private float trueHeading;

        public ARPAInstance(long time, float targetDistance, float bearingFromOwnShip, double longitude, double latitude, float trueHeading)
        {
            this.time = time;
            this.targetDistance = targetDistance;
            this.bearingFromOwnShip = bearingFromOwnShip;
            this.ownLon = longitude;
            this.ownLat = latitude;
            this.trueHeading = trueHeading;
        }
        public double getTargetLatitude()
        {
            return ownLat + Navis.deltaLatitude(targetDistance, bearingFromOwnShip);
        }
        public double getTargetLongitude()
        {
            return ownLon + Navis.deltaLongitude(ownLat, targetDistance, bearingFromOwnShip);
        }

        @Override
        public String toString()
        {
            return "ARPAInstance{" + targetDistance + ", " + bearingFromOwnShip + ", " + trueHeading + '}';
        }
        
    }
    private class NMEAObserverImpl extends AbstractPropertySetter
    {
        private String[] prefixes = new String[]{"clock", "targetCourse", "targetSpeed", "targetDistance", "targetStatus", "targetNumber", "longitude", "latitude", "trueHeading", "bearingFromOwnShip", "messageType"};
        private float targetCourse;
        private float targetSpeed;
        private float targetDistance;
        private char targetStatus;
        private int targetNumber;
        private double longitude;
        private double latitude;
        private float trueHeading;
        private float bearingFromOwnShip;
        private MessageType messageType;
        private Clock clock;

        @Override
        public void commit(String reason)
        {
            if (messageType != null)
            {
                switch (messageType)
                {
                    case RMA:
                    case RMC:
                    case GLL:
                    case GGA:
                        //finest("GPS %s", new Location(latitude, longitude));
                        break;
                    case TTM:
                        ARPATarget target;
                        switch (targetStatus)
                        {
                            case 'Q':
                                target = new ARPATarget(targetNumber);
                                arpaTargets.put(targetNumber, target);
                                break;
                            case 'T':
                                target = arpaTargets.get(targetNumber);
                                if (target == null)
                                {
                                    target = new ARPATarget(targetNumber);
                                    arpaTargets.put(targetNumber, target);
                                }
                                ARPAInstance instance = new ARPAInstance(clock.millis(), targetDistance, bearingFromOwnShip, longitude, latitude, trueHeading);
                                target.add(instance);
                                break;
                            case 'L':
                                arpaTargets.remove(targetNumber);
                                break;
                        }
                        break;
                }
            }
        }

        @Override
        public void rollback(String reason)
        {
        }

        @Override
        public void start(String reason)
        {
        }

        @Override
        public String[] getPrefixes()
        {
            return prefixes;
        }

        @Override
        public void set(String property, Object arg)
        {
            switch (property)
            {
                case "messageType":
                    this.messageType = (MessageType) arg;
                    break;
                case "clock":
                    this.clock = (Clock) arg;
                    break;
            }
        }

        @Override
        public void set(String property, double arg)
        {
            switch (property)
            {
                case "longitude":
                    this.longitude = arg;
                    break;
                case "latitude":
                    this.latitude = arg;
                    break;
            }
        }

        @Override
        public void set(String property, float arg)
        {
            switch (property)
            {
                case "targetCourse":
                    this.targetCourse = arg;
                    break;
                case "targetSpeed":
                    this.targetSpeed = arg;
                    break;
                case "targetDistance":
                    this.targetDistance = arg;
                    break;
                case "bearingFromOwnShip":
                    this.bearingFromOwnShip = arg;
                    break;
                case "trueHeading":
                    this.trueHeading = arg;
                    break;
            }
        }

        @Override
        public void set(String property, int arg)
        {
            switch (property)
            {
                case "targetNumber":
                    this.targetNumber = arg;
                    break;
            }
        }

        @Override
        public void set(String property, char arg)
        {
            switch (property)
            {
                case "targetStatus":
                    this.targetStatus = arg;
                    break;
            }
        }
        
    }
    private class AISTarget
    {
        private int mmsi;
        private String vesselName = "???";
        private BoatPosition pos;

        public AISTarget(int mmsi)
        {
            this.mmsi = mmsi;
        }
        
        public double getLatitude(AISInstance aisIns)
        {
            if (pos != null && aisIns.heading != -1)
            {
                return pos.centerLatitude(aisIns.latitude, aisIns.longitude, aisIns.heading);
            }
            else
            {
                return aisIns.latitude;
            }
        }

        public double getLongitude(AISInstance aisIns)
        {
            if (pos != null && aisIns.heading != -1)
            {
                return pos.centerLongitude(aisIns.latitude, aisIns.longitude, aisIns.heading);
            }
            else
            {
                return aisIns.longitude;
            }
        }

        @Override
        public String toString()
        {
            return "AISTarget{" + mmsi + ", " + vesselName + '}';
        }
        
    }
    private class AISInstance implements WayPoint
    {
        private long time;
        private double latitude;
        private double longitude;
        private double heading;

        public AISInstance(long time, double latitude, double longitude, double heading)
        {
            this.time = time;
            this.latitude = latitude;
            this.longitude = longitude;
            this.heading = heading;
        }

        @Override
        public long getTime()
        {
            return time;
        }

        @Override
        public double getLatitude()
        {
            return latitude;
        }

        @Override
        public double getLongitude()
        {
            return longitude;
        }

        public double getHeading()
        {
            return heading;
        }
        
    }
    private class AISObserverImpl extends AbstractPropertySetter
    {
        private String[] prefixes = new String[]{"clock", "ownMessage", "dimensionToStarboard", "dimensionToPort", "dimensionToStern", "dimensionToBow", "vesselName", "latitude", "longitude", "heading", "messageType", "mmsi"};
        private boolean ownMessage;
        private int dimensionToStarboard;
        private int dimensionToPort;
        private int dimensionToStern;
        private int dimensionToBow;
        private String vesselName;
        private double latitude;
        private double longitude;
        private int heading;
        private Future<?> future;
        private MessageTypes messageType;
        private int mmsi;
        private Clock clock;

        @Override
        public void commit(String reason)
        {
            if (mmsi != 0 && !ownMessage)
            {
                AISTarget trg = aisTargets.get(mmsi);
                if (trg == null)
                {
                    trg = new AISTarget(mmsi);
                    aisTargets.put(mmsi, trg);
                }
                final AISTarget target = trg;
                if (vesselName != null)
                {
                    trg.vesselName = vesselName;
                }
                if (dimensionToStarboard != -1)
                {
                    trg.pos = new BoatPosition(dimensionToStarboard, dimensionToPort, dimensionToBow, dimensionToStern);
                }
                if (clock != null)
                {
                    switch (messageType)
                    {
                        case PositionReportClassA:
                        case PositionReportClassAAssignedSchedule:
                        case PositionReportClassAResponseToInterrogation:
                        case StandardClassBCSPositionReport:
                        case ExtendedClassBEquipmentPositionReport:
                        case AidToNavigationReport:
                            finest("AIS %s", new Location(latitude, longitude));
                            final AISInstance instance = new AISInstance(clock.millis(), latitude, longitude, heading);
                            //process(target, instance);
                            if (future == null || future.isDone())
                            {
                                future = executor.submit(()->process(target, instance));
                            }
                            break;
                    }
                }
            }
        }

        @Override
        public void start(String reason)
        {
            heading = -1;
            dimensionToStarboard = -1;
            dimensionToPort = -1;
            dimensionToStern = -1;
            dimensionToBow = -1;
            vesselName = null;
            ownMessage = false;
        }

        @Override
        public void rollback(String reason)
        {
        }

        @Override
        public String[] getPrefixes()
        {
            return prefixes;
        }

        @Override
        public void set(String property, Object arg)
        {
            switch (property)
            {
                case "clock":
                    this.clock = (Clock) arg;
                    break;
                case "messageType":
                    this.messageType = (MessageTypes) arg;
                    break;
                case "vesselName":
                    this.vesselName = (String) arg;
                    break;
            }
        }

        @Override
        public void set(String property, int arg)
        {
            switch (property)
            {
                case "mmsi":
                    this.mmsi = arg;
                    break;
                case "heading":
                    this.heading = arg;
                    break;
                case "dimensionToStarboard":
                    this.dimensionToStarboard = arg;
                    break;
                case "dimensionToPort":
                    this.dimensionToPort = arg;
                    break;
                case "dimensionToStern":
                    this.dimensionToStern = arg;
                    break;
                case "dimensionToBow":
                    this.dimensionToBow = arg;
                    break;
            }
        }

        @Override
        public void set(String property, boolean arg)
        {
            switch (property)
            {
                case "ownMessage":
                    this.ownMessage = arg;
                    break;
            }
        }

        @Override
        public void set(String property, double arg)
        {
            switch (property)
            {
                case "latitude":
                    this.latitude = arg;
                    break;
                case "longitude":
                    this.longitude = arg;
                    break;
            }
        }

    }
}
