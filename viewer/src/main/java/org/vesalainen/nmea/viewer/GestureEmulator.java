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
package org.vesalainen.nmea.viewer;

import java.util.ArrayList;
import java.util.Arrays;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
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
    private static double PI_4 = Math.PI / 4;
    private static double PI_3_4 = 3 * Math.PI / 4;

    private Node node;  // target node
    private double deltaX;
    private double deltaY;
    private double totalDeltaX;
    private double totalDeltaY;                
    private double a;   // last angle
    private double angle;   // reported angle
    private double totalAngle;  // cumulative angle 
    private double sx;      // screen coordinate x
    private double sy;      // screen coordinate y
    private double psx;      // pressed screen coordinate x
    private double psy;      // pressed screen centralized coordinate y
    private double px;      // pressed centralized coordinate x
    private double py;      // pressed last centralized coordinate y
    private double lx;      // last centralized coordinate x
    private double ly;      // last centralized coordinate y
    private double cx;      // centralized coordinate x
    private double cy;      // centralized coordinate y
    private MouseEvent me;
    private EventType<? extends MouseEvent> eventType;
    private long pTime;
    
    @Override
    public void handle(MouseEvent me)
    {
        System.err.println(me);
        this.me = me;
        EventTarget target = me.getTarget();
        if (target instanceof Node)
        {
            node = (Node) target;
            eventType = me.getEventType();
            Bounds bounds = node.getBoundsInLocal();
            sx = me.getScreenX();
            sy = me.getScreenY();
            Point2D local = node.screenToLocal(sx, sy);
            cx = local.getX()-bounds.getWidth()/2;
            cy = local.getY()-bounds.getHeight()/2;
            if (eventType == MOUSE_PRESSED)
            {
                px = cx;
                py = cx;
                psx = sx;
                psy = sy;
                pTime = System.currentTimeMillis();
            }
            handleTouch();
            handleSwipe();
            handleRotate();
            handleScroll();
            lx = cx;
            ly = cy;
            me.consume();
        }
    }

    public void handleTouch()
    {
        if (eventType == MOUSE_PRESSED)
        {
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
    public void handleSwipe()
    {
        if (eventType == MOUSE_PRESSED)
        {
        }
        else
        {
            if (eventType == MOUSE_RELEASED)
            {
                double dx = cx - px;
                double dy = cy - py;
                double hypot = Math.hypot(dx, dy);
                if (hypot > 10 && System.currentTimeMillis()-pTime < 1000)
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
    private void handleScroll()
    {
        if (eventType == MOUSE_PRESSED)
        {
            lx = cx;
            ly = cy;
            deltaX = 0;
            deltaY = 0;
            totalDeltaX = 0;
            totalDeltaY = 0;
            ScrollEvent se = createScrollEvent(me, SCROLL_STARTED);
            Event.fireEvent(node, se);
        }
        else
        {
            deltaX = lx-cx;
            deltaY = ly-cy;
            totalDeltaX += deltaX;
            totalDeltaY += deltaY;
            if (eventType == MOUSE_DRAGGED)
            {
                ScrollEvent se = createScrollEvent(me, SCROLL);
                Event.fireEvent(node, se);
            }
            else
            {
                ScrollEvent se = createScrollEvent(me, SCROLL_FINISHED);
                Event.fireEvent(node, se);
            }
        }
    }

    private ScrollEvent createScrollEvent(MouseEvent me, EventType<ScrollEvent> eventType)
    {
        return new ScrollEvent(
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
                false, // inertia
                deltaX,
                deltaY,
                totalDeltaX,
                totalDeltaY,
                ScrollEvent.HorizontalTextScrollUnits.NONE,
                0,
                ScrollEvent.VerticalTextScrollUnits.NONE,
                0,
                1,
                me.getPickResult()
        );
    }
    private void handleRotate()
    {
        if (eventType == MOUSE_PRESSED)
        {
            a = Math.toDegrees(Math.atan2(cy, cx));
            angle = 0;
            totalAngle = 0;
            RotateEvent re = createRotateEvent(me, ROTATION_STARTED);
            Event.fireEvent(node, re);
        }
        else
        {
            if (node == null)
            {
                return;
            }
            double da = Math.toDegrees(Math.atan2(cy, cx));
            angle = da - a;
            a = da;
            totalAngle += angle;
            if (eventType == MOUSE_DRAGGED)
            {
                RotateEvent re = createRotateEvent(me, ROTATE);
                Event.fireEvent(node, re);
            }
            else
            {
                RotateEvent re = createRotateEvent(me, ROTATION_FINISHED);
                Event.fireEvent(node, re);
            }
        }
    }

    private RotateEvent createRotateEvent(MouseEvent me, EventType<RotateEvent> eventType)
    {
        return new RotateEvent(
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
                false, // inertia
                angle,
                totalAngle,
                me.getPickResult()
        );
    }
}
