/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import static javafx.scene.input.MouseEvent.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class GaugeController implements Initializable
{
    private static double PI_4 = Math.PI/4;
    private static double PI_3_4 = 3*Math.PI/4;
    @FXML Label title;
    @FXML Label unit;
    @FXML Label value;
    EventHandler<MouseEvent> eventHandler;
    private double x;
    private double y;
    
    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        eventHandler = new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent event)
            {
                MouseEvent me = (MouseEvent) event;
                if (me.getEventType() == MOUSE_PRESSED)
                {
                    x = me.getScreenX();
                    y = me.getScreenY();
                }
                if (me.getEventType() == MOUSE_RELEASED)
                {
                    double dx = me.getScreenX() - x;
                    double dy = me.getScreenY() - y;
                    double a = Math.atan2(-dy, dx);
                    System.err.println(Math.toDegrees(a));
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
        };
        Parent parent = value.getParent();
        parent.addEventHandler(MOUSE_PRESSED, eventHandler);
        parent.addEventHandler(MOUSE_RELEASED, eventHandler);
    }
    
    @FXML
    private void onSwipeDown()
    {
        System.out.println("swipe down!");
    }
    @FXML
    private void onSwipeUp()
    {
        System.out.println("swipe up!");
    }
    @FXML
    private void onSwipeRight()
    {
        System.out.println("swipe right!");
    }
    @FXML
    private void onSwipeLeft()
    {
        System.out.println("swipe left!");
    }

}
