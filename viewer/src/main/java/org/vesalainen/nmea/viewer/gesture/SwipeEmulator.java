/*
 * Copyright (C) 2020 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nmea.viewer.gesture;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;
import static javafx.scene.input.MouseEvent.MOUSE_RELEASED;
import javafx.scene.input.SwipeEvent;
import static javafx.scene.input.SwipeEvent.SWIPE_DOWN;
import static javafx.scene.input.SwipeEvent.SWIPE_LEFT;
import static javafx.scene.input.SwipeEvent.SWIPE_RIGHT;
import static javafx.scene.input.SwipeEvent.SWIPE_UP;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SwipeEmulator implements EventHandler<MouseEvent>
{
    private static double PI_4 = Math.PI / 4;
    private static double PI_3_4 = 3 * Math.PI / 4;

    private double psx;
    private double psy;
    private Node node;
    private long pTime;

    @Override
    public void handle(MouseEvent me)
    {
        double sx = me.getScreenX();
        double sy = me.getScreenY();
        EventType<? extends MouseEvent> eventType = me.getEventType();
        if (eventType == MOUSE_PRESSED)
        {
            node = (Node) me.getTarget();
            psx = sx;
            psy = sy;
            pTime = System.currentTimeMillis();
        }
        else
        {
            if (eventType == MOUSE_RELEASED)
            {
                double dx = sx - psx;
                double dy = sy - psy;
                double hypot = Math.hypot(dx, dy);
                long now = System.currentTimeMillis();
                long delta = now-pTime;
                if (hypot > 10 && delta < 1000)
                {
                    double atan2 = Math.atan2(-dy, dx);
                    double abs = Math.abs(atan2);
                    if (abs < PI_4)
                    {
                        SwipeEvent se = createSwipeEvent(me, SWIPE_RIGHT);
                        Event.fireEvent(node, se);
                    }
                    else
                    {
                        if (atan2 > PI_3_4)
                        {
                            SwipeEvent se = createSwipeEvent(me, SWIPE_LEFT);
                            Event.fireEvent(node, se);
                        }
                        else
                        {
                            if (atan2 > 0)
                            {
                                SwipeEvent se = createSwipeEvent(me, SWIPE_UP);
                                Event.fireEvent(node, se);
                            }
                            else
                            {
                                SwipeEvent se = createSwipeEvent(me, SWIPE_DOWN);
                                Event.fireEvent(node, se);
                            }
                        }
                    }
                }
                else
                {
                    System.err.println("DELTA="+delta);
                }
            }
        }
    }
    
    private SwipeEvent createSwipeEvent(MouseEvent me, EventType<SwipeEvent> eventType)
    {
        return new SwipeEvent(
                me.getSource(),
                me.getTarget(),
                eventType,
                me.getSceneX(),
                me.getSceneY(),
                me.getScreenX(),
                me.getScreenY(),
                me.isShiftDown(),
                me.isControlDown(),
                me.isAltDown(),
                me.isMetaDown(),
                false,
                1,
                me.getPickResult()
        );
    }
}
