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

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.input.ScrollEvent;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RotatingValueCanvas extends RotatingCanvas
{

    private final DoubleProperty value = new SimpleDoubleProperty();

    public double getValue()
    {
        return value.get();
    }

    public void setValue(double v)
    {
        value.set(v);
    }

    public DoubleProperty valueProperty()
    {
        return value;
    }
    
    public RotatingValueCanvas()
    {
        this(100);
    }
    public RotatingValueCanvas(double maxValue)
    {
        super(maxValue);
        valueProperty().addListener(evt->reDraw());
        onScrollProperty().setValue((e)->{if (isMouseEditable()) onScroll(e);});
    }
    
    private void onScroll(ScrollEvent e)
    {
        if (e.getDeltaY()>0)
        {
            setValue(getValue()+0.5);
        }
        else
        {
            setValue(Math.max(getValue()-0.5, 0));
        }
    }
}
