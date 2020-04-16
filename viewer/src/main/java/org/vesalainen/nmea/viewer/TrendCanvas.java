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

import static java.util.concurrent.TimeUnit.MINUTES;
import java.util.function.DoubleUnaryOperator;
import javafx.beans.binding.Binding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;
import org.vesalainen.math.sliding.TimeoutSlidingStats;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TrendCanvas extends ResizableCanvas implements PropertyBindable
{
    private final PathHelper pathHelper;
    private final ReadOnlyDoubleProperty value;

    private TimeoutSlidingStats stats;

    public TrendCanvas(DoubleProperty value)
    {
        this.pathHelper = new PathHelper();
        this.value = DoubleProperty.readOnlyDoubleProperty(value);
    }
    
    @Override
    public void bind(ViewerPreferences preferences, PropertyStore propertyStore)
    {
        Binding<Number> trendTimeout = preferences.getNumberBinding("trendTimeout");
        long minutes = trendTimeout.getValue().longValue();
        createStats(minutes);
        trendTimeout.addListener((b, o, n)->createStats(n.longValue()));
        
        value.addListener(evt->updateValue());
    }

    private void updateValue()
    {
        stats.accept(value.doubleValue());
    }
    private void draw()
    {
        int count = stats.count();
        if (count > 1)
        {
            double width = getWidth();
            double height = getHeight();
            GraphicsContext gc = getGraphicsContext2D();
            gc.clearRect(0, 0, width, height);
            gc.setFill(adjustColor(getTextFill()));
            double min = stats.getMin();
            double max = stats.getMax();
            long timeout = stats.getTimeout();
            long lastTime = System.currentTimeMillis();
            double dx = width/timeout;
            double x0 = lastTime - timeout;
            double dy = height/max;
            DoubleUnaryOperator tx = (t)->dx*(t-x0);
            DoubleUnaryOperator ty = (v)->height-v*dy;
            pathHelper.beginPath();
            pathHelper.moveTo(tx.applyAsDouble(stats.firstTime()), tx.applyAsDouble(stats.first()));
            stats.forEach((t,v)->
            {
                double x = tx.applyAsDouble(t);
                double y = ty.applyAsDouble(v);
                pathHelper.horizontalLineTo(x);
                pathHelper.verticalLineTo(y);
            });
            pathHelper.closePath();
            pathHelper.stroke();
        }
    }
    
    private void createStats(long minutes)
    {
        stats = new TimeoutSlidingStats((int) MINUTES.toSeconds(minutes), MINUTES.toMillis(minutes));
    }
    
    private class PathHelper
    {
        private GraphicsContext gc = getGraphicsContext2D();
        private double x;
        private double y;

        public void setStroke(Paint p)
        {
            gc.setStroke(p);
        }

        public void verticalLineTo(double y1)
        {
            y = y1;
            gc.lineTo(x, y1);
        }

        public void horizontalLineTo(double x1)
        {
            x = x1;
            gc.lineTo(x1, y);
        }

        public void beginPath()
        {
            gc.beginPath();
        }

        public void moveTo(double x0, double y0)
        {
            x = x0;
            y = y0;
            gc.moveTo(x0, y0);
        }

        public void lineTo(double x1, double y1)
        {
            x = x1;
            y = y1;
            gc.lineTo(x1, y1);
        }

        public void quadraticCurveTo(double xc, double yc, double x1, double y1)
        {
            x = x1;
            y = y1;
            gc.quadraticCurveTo(xc, yc, x1, y1);
        }

        public void bezierCurveTo(double xc1, double yc1, double xc2, double yc2, double x1, double y1)
        {
            x = x1;
            y = y1;
            gc.bezierCurveTo(xc1, yc1, xc2, yc2, x1, y1);
        }

        public void closePath()
        {
            gc.closePath();
        }

        public void stroke()
        {
            gc.stroke();
        }
        
    }
}
