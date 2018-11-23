/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.*;
import java.time.Clock;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vesalainen.code.AbstractPropertySetter;
import static org.vesalainen.math.UnitType.Meter;
import static org.vesalainen.math.UnitType.NM;
import org.vesalainen.navi.Navis;
import org.vesalainen.navi.cpa.Vessel;
import org.vesalainen.nmea.jaxb.router.CompassCorrectorType;
import org.vesalainen.nmea.util.Stoppable;
import org.vesalainen.parsers.nmea.MessageType;
import org.vesalainen.util.logging.AttachedLogger;
import org.vesalainen.util.navi.SimpleStats;

/**
 * @deprecated Experimental
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CompassCorrector extends AbstractPropertySetter implements AttachedLogger, Stoppable
{
    private String[] prefixes = new String[]{"clock", "messageType", "latitude", "longitude", "trueHeading", "speedOverGround", "trackMadeGood", "rateOfTurn", "magneticVariation"};
    private Clock clock = Clock.systemUTC();
    private Path store;
    private boolean furuno;
    private Vessel furunoGPS;
    private Vessel aisGPS;
    private double latitude;
    private double longitude;
    private float trueHeading;
    private float speedOverGround;
    private float trackMadeGood;
    private float rateOfTurn;
    private MessageType messageType;
    private Angle[] corTab;
    private int count;

    public CompassCorrector(CompassCorrectorType type, WritableByteChannel out)
    {
        Path dir = Paths.get(type.getDirectory());
        this.store = dir.resolve("compass-corrector.csv");
        corTab = new Angle[360];
        if (Files.exists(store))
        {
            try
            {
                int a = 0;
                for (String line : Files.readAllLines(store))
                {
                    corTab[a++] = new Angle(line);
                }
            }
            catch (IOException ex)
            {
                log(Level.SEVERE, ex, "%s", store);
            }
        }
        else
        {
            for (int a=0;a<360;a++)
            {
                corTab[a] = new Angle(a);
            }
        }
    }

    @Override
    public void commit(String reason)
    {
        if (messageType != null)
        {
            switch (messageType)
            {
                case RMC:
                    if (furuno)
                    {
                        if (furunoGPS == null)
                        {
                            furunoGPS = new Vessel();
                        }
                        furunoGPS.update(clock.millis(), latitude, longitude, speedOverGround, trackMadeGood, rateOfTurn);
                    }
                    else
                    {
                        if (aisGPS == null)
                        {
                            aisGPS = new Vessel();
                        }
                        aisGPS.update(clock.millis(), latitude, longitude, speedOverGround, trackMadeGood, rateOfTurn);
                    }
                    break;
                case HDT:
                    if (furunoGPS != null && aisGPS != null)
                    {
                        long millis = clock.millis();
                        double elat1 = furunoGPS.estimatedLatitude(millis);
                        double elon1 = furunoGPS.estimatedLongitude(millis);
                        double elat2 = aisGPS.estimatedLatitude(millis);
                        double elon2 = aisGPS.estimatedLongitude(millis);
                        Angle angle = corTab[(int)trueHeading];
                        angle.add(elat1, elon1, elat2, elon2);
                        count++;
                        if (count % 1000 == 0)
                        {
                            store();
                        }
                    }
                    break;
            }
            furuno = false;
        }
    }
    private void store()
    {
        try
        {
            try (BufferedWriter bw = Files.newBufferedWriter(store, CREATE, WRITE, TRUNCATE_EXISTING))
            {
                for (Angle a : corTab)
                {
                    bw.write(a.toString());
                    bw.newLine();
                }
            }
        }
        catch (IOException ex)
        {
            Logger.getLogger(CompassCorrector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void rollback(String reason)
    {
        furuno = false;
    }

    @Override
    public void set(String property, double arg)
    {
        switch (property)
        {
            case "latitude":
                latitude = arg;
                break;
            case "longitude":
                longitude = arg;
                break;
        }
    }

    @Override
    public void set(String property, float arg)
    {
        switch (property)
        {
            case "trueHeading":
                trueHeading = arg;
                break;
            case "speedOverGround":
                speedOverGround = arg;
                break;
            case "trackMadeGood":
                trackMadeGood = arg;
                break;
            case "rateOfTurn":
                rateOfTurn = arg;
                break;
            case "magneticVariation":
                furuno = true;
                break;
        }
    }

    @Override
    public void set(String property, Object arg)
    {
        switch (property)
        {
            case "clock":
                //clock = (Clock) arg;
                break;
            case "messageType":
                messageType = (MessageType) arg;
                break;
        }
    }
    
    @Override
    public void stop()
    {
    }

    @Override
    public String[] getPrefixes()
    {
        return prefixes;
    }

    public static class Angle
    {
        private int angle;
        private double sin;
        private double cos;
        private int cnt;
        private SimpleStats distance = new SimpleStats();

        public Angle(int angle)
        {
            this.angle = angle;
        }

        public Angle(String line)
        {
            String[] split = line.split(",");
            if (split.length < 4)
            {
                throw new IllegalArgumentException(line);
            }
            this.angle = Integer.parseInt(split[0]);
            this.sin = Double.parseDouble(split[1]);
            this.cos = Double.parseDouble(split[2]);
            this.cnt = Integer.parseInt(split[3]);;
        }
        
        public void add(double lat1, double lon1, double lat2, double lon2)
        {
            double radians = Navis.radBearing(lat1, lon1, lat2, lon2);
            sin += Math.sin(radians);
            cos += Math.cos(radians);
            cnt++;
            double dist = Navis.distance(lat1, lon1, lat2, lon2);
            distance.add(NM.convertTo(dist, Meter));
        }
        public double angle()
        {
            double dd = Math.atan2(sin, cos);
            if (dd < 0)
            {
                dd += 2*Math.PI;
            }
            return Math.toDegrees(dd);
        }

        @Override
        public int hashCode()
        {
            int hash = 7;
            hash = 67 * hash + this.angle;
            hash = 67 * hash + (int) (Double.doubleToLongBits(this.sin) ^ (Double.doubleToLongBits(this.sin) >>> 32));
            hash = 67 * hash + (int) (Double.doubleToLongBits(this.cos) ^ (Double.doubleToLongBits(this.cos) >>> 32));
            hash = 67 * hash + this.cnt;
            return hash;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final Angle other = (Angle) obj;
            if (this.angle != other.angle)
            {
                return false;
            }
            if (Double.doubleToLongBits(this.sin) != Double.doubleToLongBits(other.sin))
            {
                return false;
            }
            if (Double.doubleToLongBits(this.cos) != Double.doubleToLongBits(other.cos))
            {
                return false;
            }
            if (this.cnt != other.cnt)
            {
                return false;
            }
            return true;
        }

        @Override
        public String toString()
        {
            return String.format(Locale.US, "%d,%.20g,%.20g,%d,%.1f,%.1f", angle, sin, cos, cnt, angle(), distance.getAverage());
        }
        
    }
}
