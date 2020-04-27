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

import java.util.Arrays;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import static javafx.scene.input.MouseEvent.MOUSE_DRAGGED;
import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;
import static javafx.scene.input.MouseEvent.MOUSE_RELEASED;
import javafx.scene.input.TouchEvent;
import static javafx.scene.input.TouchEvent.TOUCH_MOVED;
import static javafx.scene.input.TouchEvent.TOUCH_PRESSED;
import static javafx.scene.input.TouchEvent.TOUCH_RELEASED;
import static javafx.scene.input.TouchEvent.TOUCH_STATIONARY;
import javafx.scene.input.TouchPoint;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TouchEmulator implements EventHandler<MouseEvent>
{
    private Node node;
    private double psx;
    private double psy;

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
            TouchEvent te = createTouchEvent(me, TouchPoint.State.PRESSED);
            Event.fireEvent(node, te);
        }
        else
        {
            if (eventType == MOUSE_DRAGGED)
            {
                double dx = sx - psx;
                double dy = sy - psy;
                double hypot = Math.hypot(dx, dy);
                if (hypot < 2)
                {
                    TouchEvent te = createTouchEvent(me, TouchPoint.State.STATIONARY);
                    Event.fireEvent(node, te);
                }
                else
                {
                    TouchEvent te = createTouchEvent(me, TouchPoint.State.MOVED);
                    Event.fireEvent(node, te);
                }
            }
            else
            {
                if (eventType == MOUSE_RELEASED)
                {
                    TouchEvent te = createTouchEvent(me, TouchPoint.State.RELEASED);
                    Event.fireEvent(node, te);
                }
            }
        }
    }
    
    private TouchEvent createTouchEvent(MouseEvent me, TouchPoint.State state)
    {
        TouchPoint tp = new TouchPoint(
                1, 
                state, 
                me.getSceneX(),
                me.getSceneY(),
                me.getScreenX(),
                me.getScreenY(),
                me.getTarget(), 
                me.getPickResult());
        EventType<TouchEvent> eventType = null;
        switch (state)
        {
            case PRESSED:
                eventType = TOUCH_PRESSED;
                break;
            case MOVED:
                eventType = TOUCH_MOVED;
                break;
            case STATIONARY:
                eventType = TOUCH_STATIONARY;
                break;
            case RELEASED:
                eventType = TOUCH_RELEASED;
                break;
        }
        return new TouchEvent(
                me.getSource(),
                me.getTarget(),
                eventType,
                tp,
                Arrays.asList(tp),
                0,
                me.isShiftDown(),
                me.isControlDown(),
                me.isAltDown(),
                me.isMetaDown()
        );
    }
}
