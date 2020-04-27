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
import static javafx.scene.input.MouseEvent.MOUSE_DRAGGED;
import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;
import javafx.scene.input.ScrollEvent;
import static javafx.scene.input.ScrollEvent.SCROLL;
import static javafx.scene.input.ScrollEvent.SCROLL_FINISHED;
import static javafx.scene.input.ScrollEvent.SCROLL_STARTED;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ScrollEmulator implements EventHandler<MouseEvent>
{
    private Node node;  // target node
    private double deltaX;
    private double deltaY;
    private double totalDeltaX;
    private double totalDeltaY;                

    private double lx;
    private double ly;

    @Override
    public void handle(MouseEvent me)
    {
        double sx = me.getScreenX();
        double sy = me.getScreenY();
        EventType<? extends MouseEvent> eventType = me.getEventType();
        if (eventType == MOUSE_PRESSED)
        {
            node = (Node) me.getTarget();
            lx = sx;
            ly = sy;
            deltaX = 0;
            deltaY = 0;
            totalDeltaX = 0;
            totalDeltaY = 0;
            ScrollEvent se = createScrollEvent(me, SCROLL_STARTED);
            Event.fireEvent(node, se);
        }
        else
        {
            deltaX = lx-sx;
            deltaY = ly-sy;
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
        lx = sx;
        ly = sy;
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
}
