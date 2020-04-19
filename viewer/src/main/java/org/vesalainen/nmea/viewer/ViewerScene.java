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

import javafx.beans.Observable;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import static javafx.scene.input.MouseEvent.*;
import javafx.scene.input.SwipeEvent;
import static javafx.scene.input.SwipeEvent.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ViewerScene extends Scene
{
    private static double PI_4 = Math.PI/4;
    private static double PI_3_4 = 3*Math.PI/4;
    EventHandler<MouseEvent> eventHandler;
    private final Parent[] parents;
    
    public ViewerScene(Parent... parents)
    {
        super(parents[0]);
        this.parents = parents;
        eventHandler = new MouseHandler();
        addEventFilter(MOUSE_PRESSED, eventHandler);
        addEventFilter(MOUSE_RELEASED, eventHandler);
        onSwipeRightProperty().addListener(e->onSwipeRight());
        onSwipeLeftProperty().addListener(e->onSwipeLeft());
        onSwipeUpProperty().addListener(e->onSwipeUp());
        onSwipeDownProperty().addListener(e->onSwipeDown());
    }
    private void onSwipeRight()
    {
        setRoot(parents[1]);
    }
    private void onSwipeLeft()
    {
        setRoot(parents[0]);
    }
    private void onSwipeUp()
    {
    }
    private void onSwipeDown()
    {
    }
    private class MouseHandler implements EventHandler<MouseEvent>
    {
        private double x;
        private double y;
        private MouseEvent active;

        @Override
        public void handle(MouseEvent me)
        {
            if (me.getEventType() == MOUSE_PRESSED)
            {
                x = me.getScreenX();
                y = me.getScreenY();
                active = me;
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
                        onSwipeRight();
                    }
                    else
                    {
                        if (abs > PI_3_4)
                        {
                            onSwipeLeft();
                        }
                        else
                        {
                            if (a > 0)
                            {
                                onSwipeUp();
                            }
                            else
                            {
                                onSwipeDown();
                            }
                        }
                    }
                }
            }
        }
    }
            
}
