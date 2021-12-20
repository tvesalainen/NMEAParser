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

import java.awt.Color;
import static java.awt.Color.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vesalainen.math.AngleAverageSeeker;
import org.vesalainen.math.AverageSeeker;
import org.vesalainen.navi.Navis;
import org.vesalainen.navi.SimpleWayPoint;
import org.vesalainen.parsers.nmea.AbstractNMEAObserver;
import org.vesalainen.parsers.nmea.MessageType;
import org.vesalainen.parsers.nmea.NMEAParser;
import org.vesalainen.parsers.nmea.time.GPSClock;
import org.vesalainen.ui.Plotter;
import org.vesalainen.util.Merger;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class GPSCompass
{
    private static final NMEAParser parser = NMEAParser.newInstance();
    private static final int SAMPLES = 36;
    private static final int SAMPLEDIV = 360/SAMPLES;
    private static final int WINDOW = 256;
    private static final double DIFF = 70.63;
    private AverageSeeker distanceSeeker;
    private AngleAverageSeeker diffSeeker;
    private AngleAverageSeeker[] angleSeeker;
    private int[] angleCount;
    private final ThreadPoolExecutor executor;
    private CountDownLatch latch;
    private float variation;

    public GPSCompass()
    {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        executor = new ThreadPoolExecutor(2, 2+availableProcessors, 1, TimeUnit.MINUTES, new SynchronousQueue(), new ThreadPoolExecutor.CallerRunsPolicy());
        latch = new CountDownLatch(SAMPLES);
        this.distanceSeeker = new AverageSeeker(WINDOW);
        this.diffSeeker = new AngleAverageSeeker(WINDOW);
        this.angleSeeker = new AngleAverageSeeker[SAMPLES];
        this.angleCount = new int[SAMPLES];
        for (int ii=0;ii<SAMPLES;ii++)
        {
            final int ind = ii;
            angleSeeker[ii] = new AngleAverageSeeker(WINDOW, 1, ()->{latch.countDown();System.err.printf("%d ready\n", ind);});
        }
    }
    
    public void start() throws IOException, InterruptedException, ExecutionException
    {
        BlockingQueue queueGPS = new ArrayBlockingQueue(1024);
        BlockingQueue queueAIS = new ArrayBlockingQueue(1024);
        Path dir = Paths.get("C:\\Users\\tkv\\share");
        //GPS gps = new GPS("pi2", 10111, queueGPS);
        //GPS ais = new GPS("pi2", 10112, queueAIS);
        GPS gps = new GPS(dir.resolve("gps.nmea"), queueGPS);
        GPS ais = new GPS(dir.resolve("ais.nmea"), queueAIS);
        Future<?> aisFuture = executor.submit(ais);
        Future<?> gpsFuture = executor.submit(gps);
        Iterator<WP> aisIt = Merger.iterator(queueAIS, 20, TimeUnit.SECONDS);
        Iterator<WP> gpsIt = Merger.iterator(queueGPS, 20, TimeUnit.SECONDS);
        Iterator<WP> merge = Merger.merge(aisIt, gpsIt);
        
        Pairer pairer = new Pairer(Merger.iterable(merge));
        executor.execute(pairer);
        gpsFuture.get();
        aisFuture.get();
        Filter filter = Filter.averageFilter(6);
        Plotter plotter = new Plotter(1000, 1000, Color.WHITE, false);
        plotter.setFont("ariel", 0, 6);
        Plotter.Polyline filtered = plotter.polyline(BLUE);
        System.err.printf("distance %s\n", distanceSeeker);
        System.err.printf("diff %s\n", diffSeeker);
        int sum = 0;
        for (int ii=0;ii<SAMPLES;ii++)
        {
            double average = angleSeeker[ii].average();
            double tolerance = angleSeeker[ii].deviation();
            double diff = Navis.angleDiff(ii+DIFF, average);
            System.err.printf("%3d %5d %.1f %.1f %.1f\n", 
                    ii, 
                    angleCount[ii], 
                    average,
                    diff,
                    tolerance
            );
            plotter.setColor(RED).drawPoint(ii, diff);
            filtered.lineTo(ii, filter.filter(diff));
            sum += angleCount[ii];
        }
        System.err.printf("sum=%d\n", sum);
        plotter.drawPolyline(filtered);
        plotter.setColor(new Color(0, 0, 0, 128)).drawCoordinates();
        plotter.plot("c:\\temp\\deviation", "png");
    }
    public static void main(String... args)
    {
        try
        {
            GPSCompass compass = new GPSCompass();
            compass.start();
        }
        catch (IOException | InterruptedException | ExecutionException ex)
        {
            Logger.getLogger(GPSCompass.class.getName()).log(Level.SEVERE, "%s", ex);
        }
    }
    private class Pairer implements Runnable
    {
        private Iterable<WP> it;

        public Pairer(Iterable<WP> it)
        {
            this.it = it;
        }
        
        @Override
        public void run()
        {
            WP prev = null;
            long p = 0;
            for (WP current : it)
            {
                if (prev != null && current.getTime() == prev.getTime())
                {
                    if (p!=0&&current.getTime()>p+1000)
                    {
                        System.err.printf("%s >> %s\n", prev, Instant.ofEpochMilli(p));
                    }
                    executor.execute(new Calculator(prev, current));
                    p = current.getTime();
                }
                prev = current;
            }
        }
        
    }
    private class Calculator implements Runnable
    {
        private WP gps;
        private WP ais;

        public Calculator(WP wp1, WP wp2)
        {
            if (wp1.heading != -1)
            {
                this.gps = wp1;
                this.ais = wp2;
            }
            else
            {
                this.gps = wp2;
                this.ais = wp1;
            }
        }
        
        @Override
        public void run()
        {
            double heading = gps.heading;
            double bearing = Navis.bearing(gps, ais);
            double distance = Navis.distance(gps, ais)*1852;
            double weight = 1.0/(gps.hdopSq() * ais.hdopSq());
            double angleDiff = Navis.angleDiff(heading, bearing);
            distanceSeeker.add(distance, weight);
            diffSeeker.add(angleDiff, weight);
            int deg = index((float) heading);
            angleSeeker[deg].add(bearing, weight);
            angleCount[deg]++;
            //System.err.printf("run %.1f %.1f %.1f %.2f\n", heading, bearing, distance, weight);
        }
        private int index(float heading)
        {
            return Math.round(heading/SAMPLEDIV) % SAMPLES;
        }
        
    }
    private class GPS<T> extends AbstractNMEAObserver implements Runnable
    {
        private T input;
        private BlockingQueue<WP> queue;
        private MessageType messageType;
        private double lon;
        private double lat;
        private float hdop = Float.MAX_VALUE;
        private float heading = -1;
        private double longitude;
        private double latitude;
        private long time;

        public GPS(T input, BlockingQueue<WP> queue) throws IOException
        {
            this.input = (T) input;
            this.queue = queue;
        }
        
        @Override
        public void run()
        {
            try
            {
                parser.parse(input, false, input::toString, this, null);
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }

        @Override
        public void commit(String reason)
        {
            if (time != 0 && clock.millis() > time)
            {
                WP wp = new WP(longitude, latitude, hdop, time, heading);
                try
                {
                    queue.put(wp);
                }
                catch (InterruptedException ex)
                {
                    Logger.getLogger(GPSCompass.class.getName()).log(Level.SEVERE, "%s", ex);
                }
            }
            latitude = lat;
            longitude = lon;
            time = clock.millis();
        }

        @Override
        public void setClock(Clock clock)
        {
            super.setClock(clock);
            ((GPSClock)clock).setPartialUpdate(true);
        }

        @Override
        public void setMessageType(MessageType messageType)
        {
            this.messageType = messageType;
        }

        @Override
        public void setLongitude(double lon)
        {
            this.lon = lon;
        }

        @Override
        public void setLatitude(double lat)
        {
            this.lat = lat;
        }

        @Override
        public void setHorizontalDilutionOfPrecision(float horizontalDilutionOfPrecision)
        {
            this.hdop = horizontalDilutionOfPrecision;
        }

        @Override
        public void setTrueHeading(float degrees)
        {
            this.heading = (float) Navis.normalizeAngle(degrees - variation);
        }

        @Override
        public void setMagneticVariation(float magneticVariation)
        {
            variation = magneticVariation;
        }

    }
    private static class WP extends SimpleWayPoint implements Comparable<WP>
    {
        private float hdop;
        private final float heading;

        public WP(double longitude, double latitude, float hdop, long time, float heading)
        {
            super(time, latitude, longitude);
            this.hdop = hdop;
            this.heading = heading;
        }

        public double hdopSq()
        {
            return hdop*hdop;
        }
        @Override
        public int compareTo(WP o)
        {
            return Long.compare(time, o.time);
        }

    }
}
