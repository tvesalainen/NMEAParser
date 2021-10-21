/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nmea.router;

import java.util.ArrayList;
import java.util.List;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.vesalainen.management.AbstractDynamicMBean;
import org.vesalainen.nmea.jaxb.router.EndpointType;
import org.vesalainen.nmea.jaxb.router.NmeaType;
import org.vesalainen.nmea.jaxb.router.RouteType;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class JmxRouterConfig
{
    private final RouterConfig routerConfig;
    private final RouterConfigMBean config;
    private final List<EndPointMBean> endPoints = new ArrayList<>();
    private final List<String> targets = new ArrayList<>();

    public JmxRouterConfig(RouterConfig config)
    {
        this.routerConfig = config;
        NmeaType nmeaType = config.getNmeaType();
        this.config = new RouterConfigMBean("Router Config Management", nmeaType);
        for (EndpointType endPoint : nmeaType.getLogEndpointOrTcpEndpointOrProcessor())
        {
            targets.add(endPoint.getName());
        }
        for (EndpointType endPoint : nmeaType.getLogEndpointOrTcpEndpointOrProcessor())
        {
            endPoints.add(new EndPointMBean(endPoint));
        }
    }

    void start()
    {
        config.register();
        endPoints.forEach((e)->e.register());
    }
    
    void stop()
    {
        config.unregister();
        endPoints.forEach((e)->e.unregister());
    }

    private class RouterConfigMBean extends AbstractDynamicMBean
    {

        public RouterConfigMBean(String description, NmeaType nmeaType)
        {
            super(description, nmeaType);
            addOperation(routerConfig, "store");
        }

        @Override
        protected ObjectName createObjectName()
        {
            try
            {
                return ObjectName.getInstance(RouterConfig.class.getName(), "Type", "Common");
            }
            catch (MalformedObjectNameException ex)
            {
                throw new RuntimeException(ex);
            }
        }
        
    }
    private class EndPointMBean extends AbstractDynamicMBean
    {
        private final String name;

        private EndPointMBean(EndpointType endPoint)
        {
            super(endPoint.getName(), endPoint);
            this.name = endPoint.getName();
            for (RouteType route : endPoint.getRoute())
            {
                String prefix = route.getPrefix();
                List<String> target = route.getTarget();
                for (String t : targets)
                {
                    if (!name.equals(t))
                    {
                        addAttribute(
                                prefix+"->"+t, 
                                boolean.class, 
                                ()->target.contains(t), 
                                (Boolean b)->
                                {
                                    if (b) 
                                    {
                                        if (!target.contains(t))
                                        {
                                            target.add(t);
                                        }
                                    }
                                    else 
                                    {
                                        target.remove(t);
                                    }}
                                );
                    }
                }
            }
        }
        
        @Override
        protected ObjectName createObjectName()
        {
            try
            {
                return ObjectName.getInstance(RouterConfig.class.getName(), "Type", name);
            }
            catch (MalformedObjectNameException ex)
            {
                throw new RuntimeException(ex);
            }
        }
        
    }
}
