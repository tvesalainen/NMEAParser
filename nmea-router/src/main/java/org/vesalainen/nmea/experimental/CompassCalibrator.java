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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.vesalainen.math.UnitType;
import static org.vesalainen.math.UnitType.*;
import org.vesalainen.navi.BoatPosition;
import org.vesalainen.navi.Navis;
import org.vesalainen.navi.WayPoint;
import org.vesalainen.parsers.nmea.AbstractNMEAObserver;
import org.vesalainen.parsers.nmea.MessageType;
import org.vesalainen.parsers.nmea.NMEAParser;
import org.vesalainen.parsers.nmea.ais.AbstractAISObserver;
import org.vesalainen.parsers.nmea.ais.MessageTypes;
import org.vesalainen.parsers.nmea.time.GPSClock;
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
    private static final double DEGREE_TOLERANCE = 25;
    private static final long TIME_TOLERANCE = 2000;
    private Path path;
    private CachedScheduledThreadPool executor;
    private Map<Integer,AISTarget> aisTargets = new ConcurrentHashMap<>();
    private Map<Integer,ARPATarget> arpaTargets = new ConcurrentHashMap<>();
    private Corrector corrector = new Corrector();
    private Clock clock;

    public CompassCalibrator(Path path)
    {
        this(path, new CachedScheduledThreadPool());
    }
    public CompassCalibrator(Path path, CachedScheduledThreadPool executor)
    {
        super(CompassCalibrator.class);
        this.path = path;
        this.executor = executor;
    }

    public void run() throws IOException, InterruptedException
    {
        NMEAParser parser = NMEAParser.newInstance();
        clock = GPSClock.getInstance(false);
        JavaLogging.setClockSupplier(()->clock);
        parser.parse(path, (GPSClock)clock, path::toString, new NMEAObserverImpl(), new AISObserverImpl());
        executor.awaitTermination(1, TimeUnit.DAYS);
    }
    private void process(AISTarget ais, AISInstance aisIns)
    {
        finest("process %s %d", ais, aisIns.time);
        arpaTargets.values().forEach((a)->process(a, ais, aisIns));
    }
    private void process(ARPATarget arpa, AISTarget ais, AISInstance aisIns)
    {
        finest("process %s %s", ais, arpa);
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
            finest("%s too far %.0fm %.0fm", ais, UnitType.convert(distanceToAis, NM, Meter), UnitType.convert(arpaIns.targetDistance, NM, Meter));
            return;
        }
        double bearingToAis = Navis.bearing(arpaIns.ownLat, arpaIns.ownLon, aisLat, aisLon);
        corrector.add(arpaIns.trueHeading, arpaIns.bearingFromOwnShip, bearingToAis);
    }
    private double diff(double x, double y)
    {
        return Math.abs(x-y);
    }
    private class Corrector
    {
        private List<Double> trueBearings = new ArrayList<>();
        private List<Double> radarBearings = new ArrayList<>();
        private List<Double> aisBearings = new ArrayList<>();
        
        public boolean add(double trueBearing, double radarBearing, double aisBearing)
        {
            double angleDiff = Navis.angleDiff(aisBearing, radarBearing);
            if (Math.abs(angleDiff) > DEGREE_TOLERANCE)
            {
                finest("angle too big %f", angleDiff);
                return false;
            }
            else
            {
                info("correct %f %f %f", trueBearing, radarBearing, aisBearing);
                trueBearings.add(trueBearing);
                radarBearings.add(radarBearing);
                aisBearings.add(aisBearing);
                return true;
            }
        }
        public void start()
        {
            trueBearings.clear();
            radarBearings.clear();
            aisBearings.clear();
        }
        public void commit()
        {
            if (!radarBearings.isEmpty())
            {
                System.err.println();
            }
        }
        public void rollback()
        {
        }

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
    private class NMEAObserverImpl extends AbstractNMEAObserver
    {

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
                                ARPAInstance instance = new ARPAInstance(CompassCalibrator.this.clock.millis(), targetDistance, bearingFromOwnShip, longitude, latitude, trueHeading);
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
        public void setMessageType(MessageType messageType)
        {
            this.messageType = messageType;
        }

        @Override
        public void setTargetCourse(float course)
        {
            this.targetCourse = course;
        }

        @Override
        public void setTargetSpeed(float speed)
        {
            this.targetSpeed = speed;
        }

        @Override
        public void setTargetDistance(float distance)
        {
            this.targetDistance = distance;
        }

        @Override
        public void setBearingFromOwnShip(float bearing)
        {
            this.bearingFromOwnShip = bearing;
        }

        @Override
        public void setLongitude(double longitude)
        {
            this.longitude = longitude;
        }

        @Override
        public void setLatitude(double latitude)
        {
            this.latitude = latitude;
        }

        @Override
        public void setTargetStatus(char status)
        {
            this.targetStatus = status;
        }

        @Override
        public void setTargetNumber(int target)
        {
            this.targetNumber = target;
        }

        @Override
        public void setTrueHeading(float degrees)
        {
            this.trueHeading = degrees;
        }

        @Override
        public void setClock(Clock clock)
        {
            CompassCalibrator.this.clock = clock;
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
    private class AISObserverImpl extends AbstractAISObserver
    {
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
                        process(target, instance);
                        /**
                        if (future == null || future.isDone())
                        {
                            future = executor.submit(()->process(target, instance));
                        }
                        */
                        break;
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
        }

        @Override
        public void rollback(String reason)
        {
        }

        @Override
        public void setOwnMessage(boolean ownMessage)
        {
            this.ownMessage = ownMessage;
        }

        @Override
        public void setMessageType(MessageTypes messageType)
        {
            this.messageType = messageType;
        }

        @Override
        public void setDimensionToStarboard(int dimension)
        {
            this.dimensionToStarboard = dimension;
        }

        @Override
        public void setDimensionToPort(int dimension)
        {
            this.dimensionToPort = dimension;
        }

        @Override
        public void setDimensionToStern(int dimension)
        {
            this.dimensionToStern = dimension;
        }

        @Override
        public void setDimensionToBow(int dimension)
        {
            this.dimensionToBow = dimension;
        }

        @Override
        public void setVesselName(String str)
        {
            this.vesselName = str;
        }

        @Override
        public void setLatitude(double degrees)
        {
            this.latitude = degrees;
        }

        @Override
        public void setLongitude(double degrees)
        {
            this.longitude = degrees;
        }

        @Override
        public void setHeading(int heading)
        {
            this.heading = heading;
        }

    }
}
