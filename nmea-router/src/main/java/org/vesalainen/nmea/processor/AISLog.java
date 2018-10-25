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
import java.io.PrintWriter;
import java.io.Reader;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.*;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import org.vesalainen.code.AbstractPropertySetter;
import org.vesalainen.nio.channels.ChannelHelper;
import org.vesalainen.nio.channels.GZIPChannel;
import org.vesalainen.nmea.jaxb.router.AisLogType;
import org.vesalainen.nmea.util.Stoppable;
import org.vesalainen.parsers.nmea.ais.AISProperties;
import org.vesalainen.parsers.nmea.ais.ManeuverIndicator;
import org.vesalainen.parsers.nmea.ais.MessageTypes;
import org.vesalainen.parsers.nmea.ais.NavigationStatus;
import org.vesalainen.parsers.nmea.ais.TimestampSupport;
import org.vesalainen.util.CollectionHelp;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;
import org.vesalainen.util.logging.AttachedLogger;
import org.vesalainen.util.navi.Location;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AISLog extends AbstractPropertySetter implements AttachedLogger, Stoppable
{
    private CachedScheduledThreadPool executor;
    private Path dir;
    private long maxLogSize = 1024*1024;
    private Properties properties = new Properties();
    private String[] prefixes;
    private Clock clock;
    private NavigationStatus status;
    private ManeuverIndicator maneuver;
    private MessageTypes type;
    private int alt;
    private int repeat;
    private int second;
    private int radio;
    private float lat;
    private float lon;
    private float turn;
    private float speed;
    private float course;
    private float heading;
    private boolean accuracy;
    private boolean raim;
    private boolean assignedMode;
    private char channel;
    
    AISLog(AisLogType type, CachedScheduledThreadPool executor)
    {
        this.executor = executor;
        String dirName = type.getDirectory();
        Objects.requireNonNull(dirName, "ais-log directory");
        dir = Paths.get(dirName);
        prefixes = CollectionHelp.toArray(AISProperties.getInstance().getAllProperties(), String.class);
        BigInteger mls = type.getMaxLogSize();
        if (mls != null)
        {
            maxLogSize = mls.longValueExact();
        }
        reset();
    }

    @Override
    public void set(String property, Object arg)
    {
        switch (property)
        {
            case "clock":
                clock = (Clock) arg;
                break;
            case "messageType":
                type = (MessageTypes) arg;
                break;
            case "status":
                status = (NavigationStatus) arg;
                break;
            case "maneuver":
                maneuver = (ManeuverIndicator) arg;
                break;
            default:
                super.set(property, arg);
                break;
        }
    }

    @Override
    public void set(String property, float arg)
    {
        switch (property)
        {
            case "latitude":
                lat = arg;
                break;
            case "longitude":
                lon = arg;
                break;
            case "turn":
                turn = arg;
                break;
            case "speed":
                speed = arg;
                break;
            case "course":
                course = arg;
                break;
            case "heading":
                heading = arg;
                break;
            default:
                super.set(property, arg);
                break;
        }
    }

    @Override
    public void set(String property, int arg)
    {
        switch (property)
        {
            case "altitude":
                alt = arg;
                break;
            case "repeatIndicator":
                repeat = arg;
                break;
            case "second":
                second = arg;
                break;
            case "radioStatus":
                radio = arg;
                break;
            case "partNumber":
                // ignore
                break;
            default:
                super.set(property, arg);
                break;
        }
    }

    @Override
    public void set(String property, boolean arg)
    {
        switch (property)
        {
            case "positionAccuracy":
                accuracy = arg;
                break;
            case "raim":
                raim = arg;
                break;
            case "assignedMode":
                assignedMode = arg;
                break;
            default:
                super.set(property, arg);
                break;
        }
    }

    @Override
    public void set(String property, char arg)
    {
        switch (property)
        {
            case "channel":
                channel = arg;
                break;
            default:
                super.set(property, arg);
                break;
        }
    }

    @Override
    public void commit(String reason)
    {
        if (channel == 0)
        {
            return; // record only transmitted own messages
        }
        try
        {
            String mmsi = properties.getProperty("mmsi");
            if (mmsi != null)
            {
                Path dat = dir.resolve(mmsi+".dat");
                Path log = dir.resolve(mmsi+".log");
                if (Files.exists(log) && Files.size(log) >= maxLogSize)
                {
                    Path tmp = Files.createTempFile(dir, mmsi, ".log");
                    Files.move(log, tmp, REPLACE_EXISTING);
                    Compressor compressor = new Compressor(tmp, log);
                    executor.submit(compressor);
                }
                Properties props = new Properties();
                if (Files.exists(dat))
                {
                    try (Reader r = Files.newBufferedReader(dat))
                    {
                        props.load(r);
                    }
                    catch (IOException ex)
                    {
                        log(Level.SEVERE, ex, "loading %s", dat);
                        return;
                    }
                }
                if (update(props, properties))
                {
                    try (BufferedWriter w = Files.newBufferedWriter(dat))
                    {
                        props.store(w, clock.toString());
                    }
                    catch (IOException ex)
                    {
                        log(Level.SEVERE, ex, "storing %s", dat);
                        return;
                    }
                }
                boolean logExists = Files.exists(log);
                ZonedDateTime timestamp = ZonedDateTime.now(clock);
                try (BufferedWriter w = Files.newBufferedWriter(log, CREATE, APPEND);
                        PrintWriter pw = new PrintWriter(w))
                {
                    switch (type)
                    {
                        case PositionReportClassA:
                        case PositionReportClassAAssignedSchedule:
                        case PositionReportClassAResponseToInterrogation:
                            if (!logExists)
                            {
                                pw.print("#Msg  Timestamp Location Course Speed Heading Turn Status Maneuver Channel\r\n");
                            }
                            pw.format(Locale.US, "Msg%d %s %s, %.1f %.1f %.1f %.1f %s %s %c\r\n",
                                    type.ordinal(),
                                    TimestampSupport.adjustIntoSecond(timestamp, second),
                                    new Location(lat, lon),
                                    course,
                                    speed,
                                    heading,
                                    turn,
                                    status,
                                    maneuver,
                                    channel
                                    );
                            break;
                        case StandardSARAircraftPositionReport:
                            if (!logExists)
                            {
                                pw.print("#Msg  Timestamp Location Course Speed Altitude Channel Assigned\r\n");
                            }
                            pw.format(Locale.US, "Msg%d %s %s, %.1f %.1f %d %c %b\r\n",
                                    type.ordinal(),
                                    TimestampSupport.adjustIntoSecond(timestamp, second),
                                    new Location(lat, lon),
                                    course,
                                    speed,
                                    alt,
                                    channel,
                                    assignedMode
                                    );
                            break;
                        case StandardClassBCSPositionReport:
                        case ExtendedClassBEquipmentPositionReport:
                            if (!logExists)
                            {
                                pw.print("#Msg  Timestamp Location Course Speed Channel Assigned\r\n");
                            }
                            pw.format(Locale.US, "Msg%d %s %s, %.1f %.1f %c %b\r\n",
                                    type.ordinal(),
                                    TimestampSupport.adjustIntoSecond(timestamp, second),
                                    new Location(lat, lon),
                                    course,
                                    speed,
                                    channel,
                                    assignedMode
                                    );
                            break;
                        default:
                            pw.format(Locale.US, "Msg%d %s %s\r\n",
                                    type.ordinal(),
                                    timestamp,
                                    properties
                                    );
                            break;
                    }
                }
                catch (IOException ex)
                {
                    log(Level.SEVERE, ex, "storing %s", log);
                    return;
                }
            }
        }
        catch (Exception ex)
        {
            log(Level.SEVERE, ex, "%s", properties);
        }        
        finally
        {
            reset();
        }
    }

    @Override
    public void rollback(String reason)
    {
        reset();
    }

    private void reset()
    {
        properties.clear();
        status = null;
        maneuver = null;
        type = null;
        alt = -1;
        repeat = -1;
        second = 60;
        radio = 0;
        lat = 0;
        lon = 0;
        turn = 0;
        speed = -1;
        course = -1;
        heading = -1;
        accuracy = false;
        raim = false;
        assignedMode = false;
        channel = 0;
    }
    @Override
    protected void setProperty(String property, Object arg)
    {
        properties.setProperty(property, arg.toString());
    }

    @Override
    public String[] getPrefixes()
    {
        return prefixes;
    }

    @Override
    public void stop()
    {
    }

    private boolean update(Properties stored, Properties received)
    {
        boolean changed = false;
        for (String property : received.stringPropertyNames())
        {
            String fresh = received.getProperty(property);
            String db = stored.getProperty(property);
            if (!fresh.equals(db))
            {
                stored.setProperty(property, fresh);
                changed = true;
                if (db != null)
                {
                    warning("AIS: %s replaced %s -> %s", property, db, fresh);
                }
            }
        }
        return changed;
    }

    private class Compressor implements Runnable
    {
        private Path tmp;
        private Path log;

        public Compressor(Path tmp, Path log)
        {
            this.tmp = tmp;
            this.log = log;
        }
        
        @Override
        public void run()
        {
            Path trg = null;
            int ver = 1;
            String base = log.getFileName().toString();
            while (true)
            {
                trg = log.resolveSibling(base+"."+ver+".gz");
                if (!Files.exists(trg))
                {
                    break;
                }
                ver++;
            }
            try (FileChannel in = FileChannel.open(tmp, READ, DELETE_ON_CLOSE);
                    GZIPChannel out = new GZIPChannel(trg, CREATE, WRITE))
            {
                ByteBuffer bb = ByteBuffer.allocate(4096);
                int rc = in.read(bb);
                while (rc > 0)
                {
                    bb.flip();
                    ChannelHelper.writeAll(out, bb);
                    bb.clear();
                    rc = in.read(bb);
                }
            }
            catch (IOException ex)
            {
                log(Level.SEVERE, ex, "compress %s -> %s", tmp, trg);
            }
        }
        
    }
}
