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

import org.vesalainen.nmea.viewer.gesture.GestureEmulator;
import javafx.beans.property.Property;
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
import javafx.scene.input.ScrollEvent.HorizontalTextScrollUnits;
import static javafx.scene.input.ScrollEvent.*;
import javafx.scene.input.ScrollEvent.VerticalTextScrollUnits;
import javafx.scene.input.SwipeEvent;
import static javafx.scene.input.SwipeEvent.*;
import javafx.stage.Stage;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ViewerScene extends Scene
{

    private static double PI_4 = Math.PI / 4;
    private static double PI_3_4 = 3 * Math.PI / 4;
    EventHandler<MouseEvent> eventHandler;
    private final Stage stage;
    private final ViewerPage[] pages;
    private final Property<Integer> currentPage;
    private final GestureEmulator gestureEmulator;

    public ViewerScene(Stage stage, Property<Integer> currentPage, ViewerPage... pages)
    {
        super(pages[currentPage.getValue()].getParent());
        this.stage = stage;
        this.currentPage = currentPage;
        this.gestureEmulator = new GestureEmulator(this);
        this.pages = pages;
        eventHandler = new MouseHandler();
        addEventHandler(SWIPE_RIGHT, e -> onSwipeRight(e));
        addEventHandler(SWIPE_LEFT, e -> onSwipeLeft(e));
        addEventHandler(SWIPE_UP, e -> onSwipeUp(e));
        addEventHandler(SWIPE_DOWN, e -> onSwipeDown(e));
    }

    private void updateRoot()
    {
        int page = currentPage.getValue();
        for (int ii = 0; ii < pages.length; ii++)
        {
            if (ii == page)
            {
                pages[ii].setActive(true);
            }
            else
            {
                pages[ii].setActive(false);
            }
        }
        setRoot(pages[page].getParent());
    }

    private void onSwipeRight(SwipeEvent e)
    {
        int page = currentPage.getValue();
        currentPage.setValue(Math.floorMod(page + 1, pages.length));
        updateRoot();
        e.consume();
    }

    private void onSwipeLeft(SwipeEvent e)
    {
        int page = currentPage.getValue();
        currentPage.setValue(Math.floorMod(page - 1, pages.length));
        updateRoot();
        e.consume();
    }

    private void onSwipeUp(SwipeEvent e)
    {
        stage.setFullScreen(true);
        e.consume();
    }

    private void onSwipeDown(SwipeEvent e)
    {
        stage.setFullScreen(false);
        e.consume();
    }

    private class MouseHandler implements EventHandler<MouseEvent>
    {

        private double x;
        private double y;
        private MouseEvent active;
        private ScrollHandler scrollHandler = new ScrollHandler();
        private RotateHandler rotateHandler = new RotateHandler();

        @Override
        public void handle(MouseEvent me)
        {
            if (me.equals(active))
            {
                return;
            }
            scrollHandler.handle(me);
            rotateHandler.handleRotate(me);
            if (me.getEventType() == MOUSE_PRESSED)
            {
                x = me.getScreenX();
                y = me.getScreenY();
                active = (MouseEvent) me.clone();
                me.consume();
            }
            else
            {
                if (active != null && me.getEventType() == MOUSE_RELEASED)
                {
                    double dx = me.getScreenX() - x;
                    double dy = me.getScreenY() - y;
                    double hypot = Math.hypot(dx, dy);
                    if (hypot < 10)
                    {
                        Event.fireEvent(active.getTarget(), active);
                        active = null;
                        return;
                    }
                    active = null;
                    me.consume();
                    double a = Math.atan2(-dy, dx);
                    double abs = Math.abs(a);
                    if (abs < PI_4)
                    {
                        SwipeEvent se = createSwipeEvent(me, SWIPE_RIGHT);
                        Event.fireEvent(se.getTarget(), se);
                    }
                    else
                    {
                        if (abs > PI_3_4)
                        {
                            SwipeEvent se = createSwipeEvent(me, SWIPE_LEFT);
                            Event.fireEvent(se.getTarget(), se);
                        }
                        else
                        {
                            if (a > 0)
                            {
                                SwipeEvent se = createSwipeEvent(me, SWIPE_UP);
                                Event.fireEvent(se.getTarget(), se);
                            }
                            else
                            {
                                SwipeEvent se = createSwipeEvent(me, SWIPE_DOWN);
                                Event.fireEvent(se.getTarget(), se);
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
    }

    private class ScrollHandler
    {
        private EventTarget target;
        private double x;
        private double y;
        private double deltaX;
        private double deltaY;
        private double totalDeltaX;
        private double totalDeltaY;                

        public void handle(MouseEvent me)
        {
            EventType<? extends MouseEvent> eventType = me.getEventType();
            if (eventType == MOUSE_PRESSED)
            {
                target = me.getTarget();
                x = me.getScreenX();
                y = me.getScreenY();
                deltaX = 0;
                deltaY = 0;
                totalDeltaX = 0;
                totalDeltaY = 0;
                ScrollEvent se = createScrollEvent(me, SCROLL_STARTED);
                Event.fireEvent(target, se);
            }
            else
            {
                double cx = me.getScreenX();
                double cy = me.getScreenY();
                deltaX = x-cx;
                deltaY = y-cy;
                totalDeltaX += deltaX;
                totalDeltaY += deltaY;
                x = cx;
                y = cy;
                if (eventType == MOUSE_DRAGGED)
                {
                    ScrollEvent se = createScrollEvent(me, SCROLL);
                    Event.fireEvent(target, se);
                }
                else
                {
                    ScrollEvent se = createScrollEvent(me, SCROLL_FINISHED);
                    Event.fireEvent(target, se);
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
                    HorizontalTextScrollUnits.NONE,
                    0,
                    VerticalTextScrollUnits.NONE,
                    0,
                    1,
                    me.getPickResult()
            );
        }
    }
    private class RotateHandler
    {
        private Node node;
        private double a;
        private double angle;
        private double totalAngle;                
        private double cx;
        private double cy;

        public void handleRotate(MouseEvent me)
        {
            EventType<? extends MouseEvent> eventType = me.getEventType();
            if (eventType == MOUSE_PRESSED)
            {
                EventTarget target = me.getTarget();
                if (target instanceof Node)
                {
                    node = (Node) target;
                }
                else
                {
                    node = null;
                    return;
                }
                Bounds bounds = node.getBoundsInLocal();
                Point2D local = node.screenToLocal(me.getScreenX(), me.getScreenY());
                cx = local.getX()-bounds.getWidth()/2;
                cy = local.getY()-bounds.getHeight()/2;
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
                Bounds bounds = node.getBoundsInLocal();
                Point2D local = node.screenToLocal(me.getScreenX(), me.getScreenY());
                cx = local.getX()-bounds.getWidth()/2;
                cy = local.getY()-bounds.getHeight()/2;
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
}
