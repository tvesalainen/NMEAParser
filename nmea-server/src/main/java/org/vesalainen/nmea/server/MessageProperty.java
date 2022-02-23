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

import org.json.JSONObject;
import org.json.JSONString;
import org.vesalainen.nmea.server.jaxb.PropertyType;
import org.vesalainen.parsers.nmea.NMEAMessage;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MessageProperty extends ObjectProperty
{
    
    public MessageProperty(CachedScheduledThreadPool executor, PropertyType property)
    {
        super(executor, property, "message");
    }

    @Override
    public <T> void set(String property, long time, T value)
    {
        if (value != null)
        {
            super.set(property, time, new JSONMessage((NMEAMessage) value));
        }
    }
    
    private static class JSONMessage implements JSONString
    {
        private String msg;

        public JSONMessage(NMEAMessage msg)
        {
            this.msg = String.format("{\"id\":%s,\"msg\":%s}", 
                    JSONObject.numberToString(msg.getTextIdentifier()), 
                    JSONObject.quote(msg.getMessage()));
        }
        
        @Override
        public String toJSONString()
        {
            return msg;
        }
        
    }
}
