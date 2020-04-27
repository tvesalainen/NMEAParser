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
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import static javafx.scene.input.MouseEvent.*;
import javafx.scene.input.RotateEvent;
import static javafx.scene.input.RotateEvent.*;
import javafx.scene.input.ScrollEvent;
import static javafx.scene.input.ScrollEvent.*;
import javafx.scene.input.SwipeEvent;
import static javafx.scene.input.SwipeEvent.*;
import javafx.scene.input.TouchEvent;
import static javafx.scene.input.TouchEvent.*;
import javafx.scene.input.TouchPoint;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class GestureEmulator implements EventHandler<MouseEvent>
{
    private final EventHandler<MouseEvent> touchEmulator = new TouchEmulator();
    private final EventHandler<MouseEvent> swipeEmulator = new SwipeEmulator();
    private final EventHandler<MouseEvent> scrollEmulator = new ScrollEmulator();
    private final EventHandler<MouseEvent> rotateEmulator = new RotateEmulator();
    private final EventHandler<MouseEvent> zoomEmulator = new ZoomEmulator();
    private MouseEvent pressedEvent;
    private boolean forward;
    private Map<MouseEvent,MouseEvent> ownEvents = new WeakHashMap<>();
    private double screenX;
    private double screenY;

    public GestureEmulator(Scene scene)
    {
        scene.addEventFilter(MOUSE_PRESSED, this);
        scene.addEventFilter(MOUSE_DRAGGED, this);
        scene.addEventFilter(MOUSE_RELEASED, this);
    }
    
    @Override
    public void handle(MouseEvent me)
    {
        if (!ownEvents.containsKey(me))
        {
            EventTarget target = me.getTarget();
            EventType<? extends MouseEvent> eventType = me.getEventType();
            if (target instanceof Node)
            {
                if (eventType == MOUSE_PRESSED)
                {
                    pressedEvent = (MouseEvent) me.clone();
                    screenX = me.getScreenX();
                    screenY = me.getScreenY();
                }
                else
                {
                    if (eventType == MOUSE_DRAGGED)
                    {
                        if (!forward)
                        {
                            double hypot = Math.hypot(screenX-me.getScreenX(), screenY-me.getScreenY());
                            if (hypot > 4)
                            {
                                forward(pressedEvent);
                                forward = true;
                            }
                        }
                        forward(me);
                    }
                    else
                    {
                        if (forward)
                        {
                            forward(me);
                            forward = false;
                        }
                        else
                        {
                            fireEvent(pressedEvent);
                            fireEvent((MouseEvent) me.clone());
                        }
                    }
                }
                me.consume();
            }
        }
    }
    private void forward(MouseEvent me)
    {
        touchEmulator.handle(me);
        swipeEmulator.handle(me);
        scrollEmulator.handle(me);
        rotateEmulator.handle(me);
        zoomEmulator.handle(me);
    }

    private void fireEvent(MouseEvent event)
    {
        ownEvents.put(event, event);
        Event.fireEvent(event.getTarget(), event);
    }
}
