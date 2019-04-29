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
public interface DeviationReadMXBean
{
    void load() throws IOException;
    String getDeviation(int deg);
    String getPath();
    double getVariation();

    default String getDeviation000()
    {
        return getDeviation(00);
    }

    default String getDeviation010()
    {
        return getDeviation(10);
    }

    default String getDeviation020()
    {
        return getDeviation(20);
    }

    default String getDeviation030()
    {
        return getDeviation(30);
    }

    default String getDeviation040()
    {
        return getDeviation(40);
    }

    default String getDeviation050()
    {
        return getDeviation(50);
    }

    default String getDeviation060()
    {
        return getDeviation(60);
    }

    default String getDeviation070()
    {
        return getDeviation(70);
    }

    default String getDeviation080()
    {
        return getDeviation(80);
    }

    default String getDeviation090()
    {
        return getDeviation(90);
    }

    default String getDeviation100()
    {
        return getDeviation(100);
    }

    default String getDeviation110()
    {
        return getDeviation(110);
    }

    default String getDeviation120()
    {
        return getDeviation(120);
    }

    default String getDeviation130()
    {
        return getDeviation(130);
    }

    default String getDeviation140()
    {
        return getDeviation(140);
    }

    default String getDeviation150()
    {
        return getDeviation(150);
    }

    default String getDeviation160()
    {
        return getDeviation(160);
    }

    default String getDeviation170()
    {
        return getDeviation(170);
    }

    default String getDeviation180()
    {
        return getDeviation(180);
    }

    default String getDeviation190()
    {
        return getDeviation(190);
    }

    default String getDeviation200()
    {
        return getDeviation(200);
    }

    default String getDeviation210()
    {
        return getDeviation(210);
    }

    default String getDeviation220()
    {
        return getDeviation(220);
    }

    default String getDeviation230()
    {
        return getDeviation(230);
    }

    default String getDeviation240()
    {
        return getDeviation(240);
    }

    default String getDeviation250()
    {
        return getDeviation(250);
    }

    default String getDeviation260()
    {
        return getDeviation(260);
    }

    default String getDeviation270()
    {
        return getDeviation(270);
    }

    default String getDeviation280()
    {
        return getDeviation(280);
    }

    default String getDeviation290()
    {
        return getDeviation(290);
    }

    default String getDeviation300()
    {
        return getDeviation(300);
    }

    default String getDeviation310()
    {
        return getDeviation(310);
    }

    default String getDeviation320()
    {
        return getDeviation(320);
    }

    default String getDeviation330()
    {
        return getDeviation(330);
    }

    default String getDeviation340()
    {
        return getDeviation(340);
    }

    default String getDeviation350()
    {
        return getDeviation(350);
    }

}
