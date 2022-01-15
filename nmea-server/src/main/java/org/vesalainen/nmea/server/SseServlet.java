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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.AsyncContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.JSONObject;
import org.vesalainen.json.SseWriter;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SseServlet extends HttpServlet
{
    private PropertyServer propertyServer;
    private ReentrantLock lock = new ReentrantLock();

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
        ServletContext servletContext = config.getServletContext();
        propertyServer = (PropertyServer) servletContext.getAttribute(PropertyServer.class.getName());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        lock.lock();
        try
        {
            log(req.toString());
            HttpSession session = req.getSession(true);
            SseHandler sseHandler = (SseHandler) session.getAttribute(SseHandler.class.getName());
            if (sseHandler == null)
            {
                throw new ServletException("SSE not started for session");
            }
            Map<String, String[]> parameterMap = req.getParameterMap();
            propertyServer.addSse(parameterMap, sseHandler);
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        lock.lock();
        try
        {
            log(req.toString());
            HttpSession session = req.getSession(true);
            resp.setContentType("text/event-stream");
            resp.setCharacterEncoding("UTF-8");
            resp.flushBuffer();
            log("async started");
            AsyncContext asyncContext = req.startAsync();
            asyncContext.setTimeout(-1);
            SseHandler sseHandler = (SseHandler) session.getAttribute(SseHandler.class.getName());
            if (sseHandler != null)
            {
                sseHandler.close();
            }
            session.setAttribute(SseHandler.class.getName(), new SseHandler(asyncContext));
        }
        finally
        {
            lock.unlock();
        }
    }
    
    public class SseHandler implements Runnable
    {
        private AsyncContext asyncContext;
        private BlockingQueue<SseWriter> queue = new ArrayBlockingQueue<>(16);
        private boolean disconnected = false;

        public SseHandler(AsyncContext asyncContext)
        {
            this.asyncContext = asyncContext;
            asyncContext.start(this);
        }

        public boolean fireEvent(SseWriter event)
        {
            if (!disconnected)
            {
                boolean ok = queue.offer(event);
                if (!ok)
                {
                    disconnected = true;
                }
                return ok;
            }
            else
            {
                return false;
            }
        }

        @Override
        public void run()
        {
            try
            {
                while (true)
                {
                    SseWriter event = queue.take();
                    PrintWriter writer = asyncContext.getResponse().getWriter();
                    event.write(writer);
                    writer.flush();
                }
            }
            catch (IllegalStateException ex)
            {
                log("Sse completed");
            }
            catch (Throwable ex)
            {
                log("Sse", ex);
            }
        }
        
        private void close()
        {
            asyncContext.complete();
        }

    }
}
