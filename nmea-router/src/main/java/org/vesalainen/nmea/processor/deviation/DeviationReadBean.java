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

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class DeviationReadBean implements DeviationReadMXBean
{
    
    public DeviationReadBean()
    {
    }

    protected abstract String getDeviation(int deg);

    @Override
    public String getDeviation000()
    {
        return getDeviation(00);
    }

    @Override
    public String getDeviation010()
    {
        return getDeviation(10);
    }

    @Override
    public String getDeviation020()
    {
        return getDeviation(20);
    }

    @Override
    public String getDeviation030()
    {
        return getDeviation(30);
    }

    @Override
    public String getDeviation040()
    {
        return getDeviation(40);
    }

    @Override
    public String getDeviation050()
    {
        return getDeviation(50);
    }

    @Override
    public String getDeviation060()
    {
        return getDeviation(60);
    }

    @Override
    public String getDeviation070()
    {
        return getDeviation(70);
    }

    @Override
    public String getDeviation080()
    {
        return getDeviation(80);
    }

    @Override
    public String getDeviation090()
    {
        return getDeviation(90);
    }

    @Override
    public String getDeviation100()
    {
        return getDeviation(100);
    }

    @Override
    public String getDeviation110()
    {
        return getDeviation(110);
    }

    @Override
    public String getDeviation120()
    {
        return getDeviation(120);
    }

    @Override
    public String getDeviation130()
    {
        return getDeviation(130);
    }

    @Override
    public String getDeviation140()
    {
        return getDeviation(140);
    }

    @Override
    public String getDeviation150()
    {
        return getDeviation(150);
    }

    @Override
    public String getDeviation160()
    {
        return getDeviation(160);
    }

    @Override
    public String getDeviation170()
    {
        return getDeviation(170);
    }

    @Override
    public String getDeviation180()
    {
        return getDeviation(180);
    }

    @Override
    public String getDeviation190()
    {
        return getDeviation(190);
    }

    @Override
    public String getDeviation200()
    {
        return getDeviation(200);
    }

    @Override
    public String getDeviation210()
    {
        return getDeviation(210);
    }

    @Override
    public String getDeviation220()
    {
        return getDeviation(220);
    }

    @Override
    public String getDeviation230()
    {
        return getDeviation(230);
    }

    @Override
    public String getDeviation240()
    {
        return getDeviation(240);
    }

    @Override
    public String getDeviation250()
    {
        return getDeviation(250);
    }

    @Override
    public String getDeviation260()
    {
        return getDeviation(260);
    }

    @Override
    public String getDeviation270()
    {
        return getDeviation(270);
    }

    @Override
    public String getDeviation280()
    {
        return getDeviation(280);
    }

    @Override
    public String getDeviation290()
    {
        return getDeviation(290);
    }

    @Override
    public String getDeviation300()
    {
        return getDeviation(300);
    }

    @Override
    public String getDeviation310()
    {
        return getDeviation(310);
    }

    @Override
    public String getDeviation320()
    {
        return getDeviation(320);
    }

    @Override
    public String getDeviation330()
    {
        return getDeviation(330);
    }

    @Override
    public String getDeviation340()
    {
        return getDeviation(340);
    }

    @Override
    public String getDeviation350()
    {
        return getDeviation(350);
    }
    
}
