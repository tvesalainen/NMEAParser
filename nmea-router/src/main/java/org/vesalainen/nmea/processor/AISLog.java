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
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.*;
import java.time.Clock;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vesalainen.code.AbstractPropertySetter;
import org.vesalainen.nio.channels.ChannelHelper;
import org.vesalainen.nio.channels.GZIPChannel;
import org.vesalainen.nmea.jaxb.router.AisLogType;
import org.vesalainen.nmea.util.Stoppable;
import org.vesalainen.parsers.nmea.ais.AISProperties;
import org.vesalainen.parsers.nmea.ais.ManeuverIndicator;
import org.vesalainen.parsers.nmea.ais.MessageTypes;
import org.vesalainen.parsers.nmea.ais.NavigationStatus;
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
    private boolean msg22;
    private boolean dsc;
    private boolean csUnit;
    private boolean display;
    private boolean assignedMode;
    private boolean band;
    
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
            case "repeatIndicator":
                repeat = arg;
                break;
            case "second":
                second = arg;
                break;
            case "radioStatus":
                radio = arg;
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
            case "msg22":
                msg22 = arg;
                break;
            case "dsc":
                dsc = arg;
                break;
            case "csUnit":
                csUnit = arg;
                break;
            case "display":
                display = arg;
                break;
            case "assignedMode":
                assignedMode = arg;
                break;
            case "band":
                band = arg;
                break;
            default:
                super.set(property, arg);
                break;
        }
    }

    @Override
    public void commit(String reason)
    {
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
                try (BufferedWriter w = Files.newBufferedWriter(log, CREATE, APPEND);
                        PrintWriter pw = new PrintWriter(w))
                {
                    switch (type)
                    {
                        case PositionReportClassA:
                        case PositionReportClassAAssignedSchedule:
                        case PositionReportClassAResponseToInterrogation:
                            pw.format(Locale.US, "Msg%d %s %s, %.1f %.1f %.1f %.1f\n",
                                    type.ordinal(),
                                    clock,
                                    new Location(lat, lon),
                                    course,
                                    speed,
                                    heading,
                                    turn
                                    );
                            break;
                        case StandardClassBCSPositionReport:
                        case ExtendedClassBEquipmentPositionReport:
                            pw.format(Locale.US, "Msg%d %s %s, %.1f %.1f\n",
                                    type.ordinal(),
                                    clock,
                                    new Location(lat, lon),
                                    course,
                                    speed
                                    );
                            break;
                        default:
                            pw.format(Locale.US, "Msg%d %s %s\n",
                                    type.ordinal(),
                                    clock,
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
            else
            {
                warning("mmsi missing %s", properties);
            }
        }
        catch (IOException ex)
        {
            log(Level.SEVERE, ex, "");
        }        
        finally
        {
            properties.clear();
        }
    }

    @Override
    public void rollback(String reason)
    {
        properties.clear();
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

    private boolean update(Properties fps, Properties nps)
    {
        boolean changed = false;
        for (String property : nps.stringPropertyNames())
        {
            String np = nps.getProperty(property);
            String fp = fps.getProperty(property);
            if (!np.equals(fp))
            {
                fps.setProperty(property, np);
                changed = true;
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
