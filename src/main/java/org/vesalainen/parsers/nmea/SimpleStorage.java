/*
 * Copyright (C) 2014 Timo Vesalainen
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

package org.vesalainen.parsers.nmea;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple class for storing observed values.
 * 
 * <p>Note that because of auto boxing there might be performance problems
 * with critical applications.
 * 
 * @author Timo Vesalainen
 */
public class SimpleStorage implements InvocationHandler
{
    private final Map<String,Object> map = new HashMap<>();
    private final Map<String,Object> trmap = new HashMap<>();
    private String commitReason;
    private String rollbackReason;
    
    protected <T extends Transactional> T getStorage(Class<T>... intf)
    {
        return (T) Proxy.newProxyInstance(
                intf[0].getClassLoader(), 
                intf, 
                this);
    }

    public Object getProperty(String property)
    {
        return map.get(property);
    }

    public String getCommitReason()
    {
        return commitReason;
    }

    public String getRollbackReason()
    {
        return rollbackReason;
    }
    
    @Override
    public synchronized Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        String name = method.getName();
        switch (name)
        {
            case "commit":
                map.putAll(trmap);
                trmap.clear();
                commitReason = (String) args[0];
                rollbackReason = null;
                break;
            case "rollback":
                trmap.clear();
                commitReason = null;
                rollbackReason = (String) args[0];
                break;
            default:
                if (name.startsWith("set") && name.length() > 3 && args.length == 1)
                {
                    String property = Character.toLowerCase(name.charAt(3))+name.substring(4);
                    trmap.put(property, args[0]);
                }
                break;
        }
        return null;
    }
}
