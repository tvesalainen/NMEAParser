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
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import static javafx.scene.input.MouseEvent.MOUSE_DRAGGED;
import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;
import javafx.scene.input.RotateEvent;
import static javafx.scene.input.RotateEvent.ROTATE;
import static javafx.scene.input.RotateEvent.ROTATION_FINISHED;
import static javafx.scene.input.RotateEvent.ROTATION_STARTED;
import javafx.scene.input.ZoomEvent;
import static javafx.scene.input.ZoomEvent.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ZoomEmulator implements EventHandler<MouseEvent>
{

    private Node node;
    private double initialHypot;
    private double hypot;
    private double zoomFactor;
    private double totalZoomFactor;

    @Override
    public void handle(MouseEvent me)
    {
        node = (Node) me.getTarget();
        double sx = me.getScreenX();
        double sy = me.getScreenY();
        Point2D local = node.screenToLocal(sx, sy);
        if (local == null)
        {
            initialHypot = hypot = 0;
            zoomFactor = 1;
            totalZoomFactor = 1;
            return;
        }
        Bounds bounds = node.getBoundsInLocal();
        double cx = local.getX()-bounds.getWidth()/2;
        double cy = local.getY()-bounds.getHeight()/2;
        EventType<? extends MouseEvent> eventType = me.getEventType();
        if (eventType == MOUSE_PRESSED)
        {
            initialHypot = hypot = Math.hypot(cy, cx);
            zoomFactor = 1;
            totalZoomFactor = 1;
            ZoomEvent ze = createZoomEvent(me, ZOOM_STARTED);
            Event.fireEvent(node, ze);
        }
        else
        {
            double dHypot = Math.hypot(cy, cx);;
            zoomFactor = dHypot/hypot;
            totalZoomFactor = dHypot/initialHypot;
            hypot = dHypot;
            if (eventType == MOUSE_DRAGGED)
            {
                ZoomEvent ze = createZoomEvent(me, ZOOM);
                Event.fireEvent(node, ze);
            }
            else
            {
                ZoomEvent ze = createZoomEvent(me, ZOOM_FINISHED);
                Event.fireEvent(node, ze);
            }
        }
    }
    
    private ZoomEvent createZoomEvent(MouseEvent me, EventType<ZoomEvent> eventType)
    {
        return new ZoomEvent(
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
                zoomFactor,
                totalZoomFactor,
                me.getPickResult()
        );
    }
}
