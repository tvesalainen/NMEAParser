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
import java.net.InetSocketAddress;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
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
import org.vesalainen.util.Merger;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class GPSCompass
{
    private static final NMEAParser parser = NMEAParser.newInstance();
    private static final int WINDOW = 256;
    private AverageSeeker distanceSeeker;
    private AngleAverageSeeker diffSeeker;
    private AngleAverageSeeker[] angleSeeker;
    private int[] angleCount;
    private final ThreadPoolExecutor executor;

    public GPSCompass()
    {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        executor = new ThreadPoolExecutor(2, 2+availableProcessors, 1, TimeUnit.MINUTES, new SynchronousQueue(), new ThreadPoolExecutor.CallerRunsPolicy());
        this.distanceSeeker = new AverageSeeker(WINDOW);
        this.diffSeeker = new AngleAverageSeeker(WINDOW);
        this.angleSeeker = new AngleAverageSeeker[360];
        this.angleCount = new int[360];
        for (int ii=0;ii<360;ii++)
        {
            angleSeeker[ii] = new AngleAverageSeeker(WINDOW);
        }
    }
    
    public void start() throws IOException, InterruptedException, ExecutionException
    {
        BlockingQueue queueGPS = new ArrayBlockingQueue(64);
        BlockingQueue queueAIS = new ArrayBlockingQueue(64);
        Path dir = Paths.get("C:\\Users\\tkv\\share");
        //GPS gps = new GPS("pi2", 10111, queueGPS);
        //GPS ais = new GPS("pi2", 10112, queueAIS);
        GPS gps = new GPS(dir.resolve("gps.nmea"), queueGPS);
        GPS ais = new GPS(dir.resolve("ais.nmea"), queueAIS);
        Future<?> aisFuture = executor.submit(ais);
        Future<?> gpsFuture = executor.submit(gps);
        Iterator<WP> aisIt = Merger.iterator(queueAIS);
        Iterator<WP> gpsIt = Merger.iterator(queueGPS);
        Iterator<WP> merge = Merger.merge(aisIt, gpsIt);
        
        Pairer pairer = new Pairer(Merger.iterable(merge));
        executor.execute(pairer);
        aisFuture.get();
        gpsFuture.get();
        System.err.printf("distance %s\n", distanceSeeker);
        System.err.printf("fiff %s\n", diffSeeker);
        for (int ii=0;ii<360;ii++)
        {
            System.err.printf("%3d: %5d %s\n", ii, angleCount[ii], angleSeeker[ii]);
        }
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
            Logger.getLogger(GPSCompass.class.getName()).log(Level.SEVERE, null, ex);
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
            for (WP current : it)
            {
                if (prev != null && current.getTime() == prev.getTime())
                {
                    executor.execute(new Calculator(prev, current));
                }
                prev = current;
            }
        }
        
    }
    private class Calculator implements Runnable
    {
        private WP wp1;
        private WP wp2;

        public Calculator(WP wp1, WP wp2)
        {
            this.wp1 = wp1;
            this.wp2 = wp2;
        }
        
        @Override
        public void run()
        {
            double heading = wp1.heading == -1 ? wp2.heading : wp1.heading;
            double bearing = Navis.bearing(wp1, wp2);
            double distance = Navis.distance(wp1, wp2)*1852;
            double weight = 1.0/(wp1.hdopSq() * wp2.hdopSq());
            double angleDiff = Navis.angleDiff(heading, bearing);
            distanceSeeker.add(distance, weight);
            diffSeeker.add(angleDiff, weight);
            angleSeeker[(int)heading].add(bearing, weight);
            angleCount[(int)heading]++;
            //System.err.printf("run %.1f %.1f %.1f %.2f\n", heading, bearing, distance, weight);
        }
        
    }
    private class GPS extends AbstractNMEAObserver implements Runnable
    {
        private GatheringByteChannel channel;
        private BlockingQueue<WP> queue;
        private MessageType messageType;
        private double longitude;
        private double latitude;
        private float hdop = Float.MAX_VALUE;
        private float heading = -1;

        public GPS(Path path, BlockingQueue<WP> queue) throws IOException
        {
            this.channel = (GatheringByteChannel) Files.newByteChannel(path);
            this.queue = queue;
        }
        public GPS(String hostname, int port, BlockingQueue<WP> queue) throws IOException
        {
            InetSocketAddress address = new InetSocketAddress(hostname, port);
            this.channel = SocketChannel.open(address);
            this.queue = queue;
        }
        
        @Override
        public void run()
        {
            try
            {
                parser.parse(channel, false, channel::toString, this, null);
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }

        @Override
        public void commit(String reason)
        {
            if (Objects.equals(messageType, MessageType.RMC))
            {
                WP wp = new WP(longitude, latitude, hdop, clock.millis(), heading);
                try
                {
                    queue.put(wp);
                }
                catch (InterruptedException ex)
                {
                    Logger.getLogger(GPSCompass.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        @Override
        public void setMessageType(MessageType messageType)
        {
            this.messageType = messageType;
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
        public void setHorizontalDilutionOfPrecision(float horizontalDilutionOfPrecision)
        {
            this.hdop = horizontalDilutionOfPrecision;
        }

        @Override
        public void setTrueHeading(float degrees)
        {
            this.heading = degrees;
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
