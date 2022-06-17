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
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.AsyncContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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
            //log(req.toString());
            HttpSession session = req.getSession(true);
            SseReference sseReference = (SseReference) session.getAttribute(SseHandler.class.getName());
            if (sseReference == null)
            {
                throw new ServletException("SSE not started for session");
            }
            Map<String, String[]> parameterMap = req.getParameterMap();
            propertyServer.addSse(parameterMap, sseReference, req.getLocale());
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
            //log(req.toString());
            HttpSession session = req.getSession(true);
            resp.setContentType("text/event-stream");
            resp.setCharacterEncoding("UTF-8");
            resp.flushBuffer();
            log("async started");
            AsyncContext asyncContext = req.startAsync();
            asyncContext.setTimeout(-1);
            SseReference sseReference = (SseReference) session.getAttribute(SseHandler.class.getName());
            if (sseReference != null)
            {
                SseHandler handler = sseReference.getHandler();
                if (handler != null)
                {
                    handler.close();
                }
            }
            SseHandler sseHandler = new SseHandler(asyncContext);
            session.setAttribute(SseHandler.class.getName(), sseHandler.getReference());
        }
        finally
        {
            lock.unlock();
        }
    }
    
    public class SseHandler implements Runnable
    {
        private AsyncContext asyncContext;
        private BlockingQueue<SseWriter> queue = new ArrayBlockingQueue<>(1024);
        private volatile boolean disconnected = true;
        private SseReference reference;
        private final Locale locale;

        public SseHandler(AsyncContext asyncContext)
        {
            this.reference = new SseReference(this);
            this.asyncContext = asyncContext;
            this.locale = asyncContext.getRequest().getLocale();
            asyncContext.start(this);
        }

        public SseReference getReference()
        {
            return reference;
        }

        public void fireEvent(SseWriter event)
        {
            if (!disconnected)
            {
                boolean ok = queue.offer(event);
                if (!ok)
                {
                    log("queue is full");
                    close();
                }
            }
        }

        @Override
        public void run()
        {
            try
            {
                log("Sse started");
                disconnected = false;
                PrintWriter writer = asyncContext.getResponse().getWriter();
                while (true)
                {
                    SseWriter event = queue.take();
                    event.write(locale, writer);
                    if (queue.isEmpty())
                    {
                        writer.flush();
                    }
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
            log("close sse handler");
            reference.clear();
            disconnected = true;
            asyncContext.complete();
        }

    }
    public class SseReference
    {
        private SseHandler handler;

        public SseReference(SseHandler handler)
        {
            this.handler = handler;
        }

        public SseHandler getHandler()
        {
            return handler;
        }
        
        public void clear()
        {
            handler = null;
        }
    }
}
