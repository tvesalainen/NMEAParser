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
package org.vesalainen.nmea.server;

import java.nio.file.Path;
import java.nio.file.Paths;
import javax.servlet.http.HttpServletRequest;
import org.vesalainen.web.servlet.AbstractJarServlet;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ResourceServlet extends AbstractJarServlet
{

    public ResourceServlet()
    {
        super(Paths.get("src/main/resources"));
    }

    @Override
    protected String getPage(HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        if ("/".equals(requestURI))
        {
            requestURI = "/index.html";
        }
        return "/web"+requestURI;
    }
    
}
