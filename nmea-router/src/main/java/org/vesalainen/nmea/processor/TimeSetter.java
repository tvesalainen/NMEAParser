/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import org.vesalainen.code.PropertySetter;
import org.vesalainen.nmea.jaxb.router.TimeSetterType;
import org.vesalainen.parsers.nmea.NMEAClock;
import org.vesalainen.util.Transactional;
import org.vesalainen.util.logging.JavaLogging;

/**
 * A class for setting system time using configurable command
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TimeSetter extends TimerTask implements PropertySetter, Transactional
{
    private static final String[] Prefixes = new String[]{
        "clock"
            };
    private GregorianCalendar calendar;
    private long period = 4000;
    private Timer timer;
    private JavaLogging log = new JavaLogging();
    private NMEAClock clock;
    private SimpleDateFormat format;
    private long maxDelta = 1000;   // max time difference

    public TimeSetter(TimeSetterType timeSetterType) throws IOException
    {
        log.setLogger(this.getClass());
        String cmd = timeSetterType.getCmd();
        if (cmd == null)
        {
            throw new IOException("cmd attribute not set");
        }
        format = new SimpleDateFormat(cmd);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        log.config("time setting command = %s", cmd);
        int pollInterval = 6;
        Integer interval = timeSetterType.getPollInterval();
        if (interval != null)
        {
            pollInterval = interval;
        }
        period = (long) Math.pow(2, pollInterval)*1000;
        Long md = timeSetterType.getMaxDelta();
        if (md != null)
        {
            maxDelta = md;
        }
    }
    
    @Override
    public String[] getPrefixes()
    {
        return Prefixes;
    }

    @Override
    public void start(String reason)
    {
    }

    @Override
    public void rollback(String reason)
    {
        log.warning("rollback(%s)", reason);
    }

    @Override
    public void commit(String reason)
    {
        if (timer == null)
        {
            log.config("TimeSetter started period=%d", period);
            timer = new Timer();
            timer.scheduleAtFixedRate(this, 0, period);
        }
    }
    
    @Override
    public void run()
    {
        try
        {
            long delta = Math.abs(clock.offset());
            if (delta > maxDelta)
            {
                Date time = calendar.getTime();
                String cmd = format.format(time);
                log.info("cmd=%s %s", cmd, time);
                Process proc = Runtime.getRuntime().exec(cmd);
                int exitValue = proc.waitFor();
                if (exitValue != 0)
                {
                    String err = getString(proc.getErrorStream());
                    log.warning("cmd %s returned %d\n%s", cmd, exitValue, err);
                }
            }
            else
            {
                log.fine("Time not updated. Delta = %dms", delta);
            }
        }
        catch (Exception ex)
        {
            log.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    private String getString(InputStream is) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        int cc = is.read();
        while (cc != -1)
        {
            sb.append((char)cc);
            cc = is.read();
        }
        return sb.toString();
    }
    @Override
    public void set(String property, boolean arg)
    {
        switch (property)
        {
            
        }
    }

    @Override
    public void set(String property, byte arg)
    {
        switch (property)
        {
            
        }
    }

    @Override
    public void set(String property, char arg)
    {
        switch (property)
        {
            
        }
    }

    @Override
    public void set(String property, short arg)
    {
        switch (property)
        {
            
        }
    }

    @Override
    public void set(String property, int arg)
    {
        switch (property)
        {
            
        }
    }

    @Override
    public void set(String property, long arg)
    {
        switch (property)
        {
            
        }
    }

    @Override
    public void set(String property, float arg)
    {
        switch (property)
        {
        }
    }

    @Override
    public void set(String property, double arg)
    {
        switch (property)
        {
            
        }
    }

    @Override
    public void set(String property, Object arg)
    {
        switch (property)
        {
            case "clock":
                clock = (NMEAClock) arg;
                break;
        }
    }

}
