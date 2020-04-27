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

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RotateEmulator implements EventHandler<MouseEvent>
{

    private Node node;
    private double a;   // last angle
    private double angle;   // reported angle
    private double totalAngle;  // cumulative angle 

    @Override
    public void handle(MouseEvent me)
    {
        node = (Node) me.getTarget();
        double sx = me.getScreenX();
        double sy = me.getScreenY();
        Point2D local = node.screenToLocal(sx, sy);
        if (local == null)
        {
            a = 0;
            angle = 0;
            totalAngle = 0;
            return;
        }
        Bounds bounds = node.getBoundsInLocal();
        double cx = local.getX()-bounds.getWidth()/2;
        double cy = local.getY()-bounds.getHeight()/2;
        EventType<? extends MouseEvent> eventType = me.getEventType();
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
