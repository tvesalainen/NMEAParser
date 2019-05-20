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
package org.vesalainen.nmea.processor;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import static java.util.logging.Level.*;
import org.vesalainen.code.AbstractPropertySetter;
import org.vesalainen.math.UnitType;
import static org.vesalainen.math.UnitType.*;
import org.vesalainen.navi.BoatPosition;
import org.vesalainen.navi.Navis;
import org.vesalainen.navi.WayPoint;
import org.vesalainen.nmea.processor.deviation.DeviationManager;
import org.vesalainen.parsers.nmea.MessageType;
import org.vesalainen.parsers.nmea.NMEADispatcher;
import org.vesalainen.parsers.nmea.NMEAParser;
import org.vesalainen.parsers.nmea.NMEAService;
import org.vesalainen.parsers.nmea.ais.AISDispatcher;
import org.vesalainen.parsers.nmea.ais.MessageTypes;
import org.vesalainen.util.LongRingBufferMap;
import org.vesalainen.util.TimeToLiveMap;
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
    private static final double DEGREE_TOLERANCE = 6;
    private static final long TIME_TOLERANCE = 2000;
    private Path path;
    private CachedScheduledThreadPool executor;
    private Map<Integer,AISTarget> aisTargets = new ConcurrentHashMap<>();
    private Map<Integer,ARPATarget> arpaTargets = new TimeToLiveMap<>(10, TimeUnit.MINUTES);
    private DeviationManager devMgr;
    private final double variation;
    private NMEAObserverImpl nmeaObserver;
    private AISObserverImpl aisObserver;
    private List<Correction> corrections = new ArrayList<>();

    public CompassCalibrator(Path path, double variation) throws IOException
    {
        this(new DeviationManager(path, variation), path, variation, new CachedScheduledThreadPool());
    }
    public CompassCalibrator(DeviationManager devMgr, Path path, double variation, CachedScheduledThreadPool executor)
    {
        super(CompassCalibrator.class);
        this.path = path;
        this.variation = variation;
        this.executor = executor;
        this.devMgr = devMgr;
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
        try
        {
            finest("process %s %d", ais, aisIns.time);
            arpaTargets.values().forEach((a)->process(a, ais, aisIns));
            commit(ais);
        }
        catch (Exception ex)
        {
            log(SEVERE, ex, "%s", ex.getMessage());
        }
    }
    private void process(ARPATarget arpa, AISTarget ais, AISInstance aisIns)
    {
        finest("process %s %s", ais, arpa);
        ARPAInstance arpaIns = arpa.getClosest(aisIns.time);
        if (arpaIns != null)
        {
            process(arpa, ais, arpaIns, aisIns);
        }
    }
    private void process(ARPATarget arpa, AISTarget ais, ARPAInstance arpaIns, AISInstance aisIns)
    {
        double aisLat = ais.getLatitude(aisIns);
        double aisLon = ais.getLongitude(aisIns);
        double maxLength = ais.getMaxLength(aisIns);
        double distanceToAis = Navis.distance(arpaIns.ownLat, arpaIns.ownLon, aisLat, aisLon);
        double distanceToAisMeters = UnitType.convert(distanceToAis, NM, Meter);
        double maxAngle = 2*Math.toDegrees(Math.atan2(maxLength/2, distanceToAisMeters));
        if (maxAngle > DEGREE_TOLERANCE)
        {
            fine("too close too big");
            return;
        }
        if (diff(distanceToAis, arpaIns.targetDistance) > DISTANCE_TOLERANCE)
        {
            fine("%s too far %.0fm %.0fm", ais, UnitType.convert(distanceToAis, NM, Meter), UnitType.convert(arpaIns.targetDistance, NM, Meter));
            return;
        }
        double bearingToAis = Navis.bearing(arpaIns.ownLat, arpaIns.ownLon, aisLat, aisLon);
        double angleDiff = Navis.angleDiff(arpaIns.bearingFromOwnShip, bearingToAis);
        if (Math.abs(angleDiff) > DEGREE_TOLERANCE)
        {
            fine("%s angle too big %f", ais, angleDiff);
        }
        else
        {
            Correction correction = new Correction(arpa, ais, arpaIns.magneticHeading, arpaIns.bearingFromOwnShip, bearingToAis, 1/maxAngle);
            corrections.add(correction);
            fine("queue correction %s %.1f", correction, maxAngle);
        }
    }
    private void commit(AISTarget ais)
    {
        try
        {
            if (corrections.size() == 1)
            {
                Correction c = corrections.get(0);
                fine("correct %s", c);
                devMgr.correct(c.magneticHeading, c.radarHeading, c.aisHeading, c.weight);
            }
            else
            {
                for (Correction c : corrections)
                {
                    info("undo %s", c);
                }
            }
        }
        finally
        {
            corrections.clear();
        }
    }
    private double diff(double x, double y)
    {
        return Math.abs(x-y);
    }
    private class Correction
    {
        private ARPATarget arpa;
        private AISTarget ais;
        private double magneticHeading;
        private double radarHeading;
        private double aisHeading;
        private double weight;

        public Correction(ARPATarget arpa, AISTarget ais, double magneticHeading, double radarHeading, double aisHeading, double weight)
        {
            this.arpa = arpa;
            this.ais = ais;
            this.magneticHeading = magneticHeading;
            this.radarHeading = radarHeading;
            this.aisHeading = aisHeading;
            this.weight = weight;
        }

        @Override
        public String toString()
        {
            return "Correction{" + arpa + ", " + ais + ", " + magneticHeading + ", " + radarHeading + ", " + aisHeading + '}';
        }

        
    }
    private class ARPATarget
    {
        private int targetNumber;
        private LongRingBufferMap<ARPAInstance> instances = new LongRingBufferMap<>(20);

        public ARPATarget(int targetNumber)
        {
            this.targetNumber = targetNumber;
        }
        
        void add(ARPAInstance instant)
        {
            instances.put(instant.time, instant);
        }
        ARPAInstance getClosest(long time)
        {
            return instances.getClosest(time, TIME_TOLERANCE);
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
        private float magneticHeading;

        public ARPAInstance(long time, float targetDistance, float bearingFromOwnShip, double longitude, double latitude, float magneticHeading)
        {
            this.time = time;
            this.targetDistance = targetDistance;
            this.bearingFromOwnShip = bearingFromOwnShip;
            this.ownLon = longitude;
            this.ownLat = latitude;
            this.magneticHeading = magneticHeading;
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
            return "ARPAInstance{" + targetDistance + ", " + bearingFromOwnShip + ", " + magneticHeading + '}';
        }
        
    }
    private class NMEAObserverImpl extends AbstractPropertySetter
    {
        private String[] prefixes = new String[]{"clock", "targetCourse", "targetSpeed", "targetDistance", "targetStatus", "targetNumber", "longitude", "latitude", "magneticHeading", "bearingFromOwnShip", "messageType"};
        private float targetCourse;
        private float targetSpeed;
        private float targetDistance;
        private char targetStatus;
        private int targetNumber;
        private double longitude;
        private double latitude;
        private float magneticHeading;
        private float bearingFromOwnShip;
        private MessageType messageType;
        private Clock clock;
        private int lastMagHea;

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
                                ARPAInstance instance = new ARPAInstance(clock.millis(), targetDistance, bearingFromOwnShip, longitude, latitude, magneticHeading);
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
                case "magneticHeading":
                    this.magneticHeading = arg;
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
        private String vesselName = "";
        private BoatPosition pos;

        public AISTarget(int mmsi)
        {
            this.mmsi = mmsi;
        }
        public double getMaxLength(AISInstance aisIns)
        {
            if (pos != null)
            {
                if (pos.length() > 4)
                {
                    return pos.length();
                }
            }   
            return 500;
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
        private String[] prefixes = new String[]{"clock", "second", "ownMessage", "dimensionToStarboard", "dimensionToPort", "dimensionToStern", "dimensionToBow", "vesselName", "latitude", "longitude", "heading", "messageType", "mmsi"};
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
        private int second;
        private long lastMillis;

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
                            /* second is always in future
                            ZonedDateTime timestamp = ZonedDateTime.now(clock);
                            if (second != -1)
                            {
                                timestamp = (ZonedDateTime) TimestampSupport.adjustIntoSecond(timestamp, second);
                            }
                            long millis = timestamp.toInstant().toEpochMilli(); // this is synced with second
                            info("%s %s", target, timestamp);
                            */
                            fine("%s %s", target, clock);
                            long millis = clock.millis();
                            final AISInstance instance = new AISInstance(millis, latitude, longitude, heading);
                            if ((future == null || future.isDone()) && millis > lastMillis)
                            {
                                future = executor.submit(()->process(target, instance));
                            }
                            lastMillis = millis + TIME_TOLERANCE;   // so that next ais measure has corrected deviation in effect
                            break;
                    }
                }
            }
        }

        @Override
        public void start(String reason)
        {
            second = -1;
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
                case "second":
                    this.second = arg;
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
