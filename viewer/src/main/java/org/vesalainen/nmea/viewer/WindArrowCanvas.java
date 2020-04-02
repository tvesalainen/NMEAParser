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

import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.scene.canvas.GraphicsContext;
import org.vesalainen.fx.FXPathMaker;
import org.vesalainen.navi.WindArrow;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class WindArrowCanvas extends RotatingCanvas
{

    private final FloatProperty windDirectionOverGround = new SimpleFloatProperty();

    public float getWindDirectionOverGround()
    {
        return windDirectionOverGround.get();
    }

    public void setWindDirectionOverGround(float value)
    {
        windDirectionOverGround.set(value);
    }

    public FloatProperty windDirectionOverGroundProperty()
    {
        return windDirectionOverGround;
    }
    private final FloatProperty windSpeedOverGround = new SimpleFloatProperty();

    public float getWindSpeedOverGround()
    {
        return windSpeedOverGround.get();
    }

    public void setWindSpeedOverGround(float value)
    {
        windSpeedOverGround.set(value);
    }

    public FloatProperty windSpeedOverGroundProperty()
    {
        return windSpeedOverGround;
    }

    private final GraphicsContext gc;
    private final FXPathMaker pathMaker;
    private final WindArrow windArrow;

    public WindArrowCanvas()
    {
        super(50);
        getStyleClass().add("wind-arror-canvas");
        this.gc = getGraphicsContext2D();
        this.pathMaker = new FXPathMaker(gc);
        this.windArrow = new WindArrow(pathMaker);
    }

    @Override
    protected void onDraw(GraphicsContext gc)
    {
        super.onDraw(gc);
        gc.scale(1, -1);
        windArrow.draw(90);
    }
    
}
