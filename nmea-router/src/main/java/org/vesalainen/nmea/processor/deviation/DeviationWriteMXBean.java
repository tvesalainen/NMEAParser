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
package org.vesalainen.nmea.processor.deviation;

import java.io.IOException;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public interface DeviationWriteMXBean
{
    void store() throws IOException;
    public void reset();
    void rotate(double diff);
    
    void setDeviation(int deg, String deviation);

    default void setDeviation000(String deviation)
    {
        setDeviation(00, deviation);
    }

    default void setDeviation010(String deviation)
    {
        setDeviation(10, deviation);
    }

    default void setDeviation020(String deviation)
    {
        setDeviation(20, deviation);
    }

    default void setDeviation030(String deviation)
    {
        setDeviation(30, deviation);
    }

    default void setDeviation040(String deviation)
    {
        setDeviation(40, deviation);
    }

    default void setDeviation050(String deviation)
    {
        setDeviation(50, deviation);
    }

    default void setDeviation060(String deviation)
    {
        setDeviation(60, deviation);
    }

    default void setDeviation070(String deviation)
    {
        setDeviation(70, deviation);
    }

    default void setDeviation080(String deviation)
    {
        setDeviation(80, deviation);
    }

    default void setDeviation090(String deviation)
    {
        setDeviation(90, deviation);
    }

    default void setDeviation100(String deviation)
    {
        setDeviation(100, deviation);
    }

    default void setDeviation110(String deviation)
    {
        setDeviation(110, deviation);
    }

    default void setDeviation120(String deviation)
    {
        setDeviation(120, deviation);
    }

    default void setDeviation130(String deviation)
    {
        setDeviation(130, deviation);
    }

    default void setDeviation140(String deviation)
    {
        setDeviation(140, deviation);
    }

    default void setDeviation150(String deviation)
    {
        setDeviation(150, deviation);
    }

    default void setDeviation160(String deviation)
    {
        setDeviation(160, deviation);
    }

    default void setDeviation170(String deviation)
    {
        setDeviation(170, deviation);
    }

    default void setDeviation180(String deviation)
    {
        setDeviation(180, deviation);
    }

    default void setDeviation190(String deviation)
    {
        setDeviation(190, deviation);
    }

    default void setDeviation200(String deviation)
    {
        setDeviation(200, deviation);
    }

    default void setDeviation210(String deviation)
    {
        setDeviation(210, deviation);
    }

    default void setDeviation220(String deviation)
    {
        setDeviation(220, deviation);
    }

    default void setDeviation230(String deviation)
    {
        setDeviation(230, deviation);
    }

    default void setDeviation240(String deviation)
    {
        setDeviation(240, deviation);
    }

    default void setDeviation250(String deviation)
    {
        setDeviation(250, deviation);
    }

    default void setDeviation260(String deviation)
    {
        setDeviation(260, deviation);
    }

    default void setDeviation270(String deviation)
    {
        setDeviation(270, deviation);
    }

    default void setDeviation280(String deviation)
    {
        setDeviation(280, deviation);
    }

    default void setDeviation290(String deviation)
    {
        setDeviation(290, deviation);
    }

    default void setDeviation300(String deviation)
    {
        setDeviation(300, deviation);
    }

    default void setDeviation310(String deviation)
    {
        setDeviation(310, deviation);
    }

    default void setDeviation320(String deviation)
    {
        setDeviation(320, deviation);
    }

    default void setDeviation330(String deviation)
    {
        setDeviation(330, deviation);
    }

    default void setDeviation340(String deviation)
    {
        setDeviation(340, deviation);
    }

    default void setDeviation350(String deviation)
    {
        setDeviation(350, deviation);
    }

}
