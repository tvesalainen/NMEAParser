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
package org.vesalainen.nmea.processor;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class DeviationBean implements DeviationMXBean
{
    
    public DeviationBean()
    {
    }
    protected abstract String getDeviation(int deg);

    protected abstract void setDeviation(int deg, String deviation);

    @Override
    public String getDeviation000()
    {
        return getDeviation(00);
    }

    @Override
    public void setDeviation000(String deviation)
    {
        setDeviation(00, deviation);
    }

    @Override
    public String getDeviation010()
    {
        return getDeviation(10);
    }

    @Override
    public void setDeviation010(String deviation)
    {
        setDeviation(10, deviation);
    }

    @Override
    public String getDeviation020()
    {
        return getDeviation(20);
    }

    @Override
    public void setDeviation020(String deviation)
    {
        setDeviation(20, deviation);
    }

    @Override
    public String getDeviation030()
    {
        return getDeviation(30);
    }

    @Override
    public void setDeviation030(String deviation)
    {
        setDeviation(30, deviation);
    }

    @Override
    public String getDeviation040()
    {
        return getDeviation(40);
    }

    @Override
    public void setDeviation040(String deviation)
    {
        setDeviation(40, deviation);
    }

    @Override
    public String getDeviation050()
    {
        return getDeviation(50);
    }

    @Override
    public void setDeviation050(String deviation)
    {
        setDeviation(50, deviation);
    }

    @Override
    public String getDeviation060()
    {
        return getDeviation(60);
    }

    @Override
    public void setDeviation060(String deviation)
    {
        setDeviation(60, deviation);
    }

    @Override
    public String getDeviation070()
    {
        return getDeviation(70);
    }

    @Override
    public void setDeviation070(String deviation)
    {
        setDeviation(70, deviation);
    }

    @Override
    public String getDeviation080()
    {
        return getDeviation(80);
    }

    @Override
    public void setDeviation080(String deviation)
    {
        setDeviation(80, deviation);
    }

    @Override
    public String getDeviation090()
    {
        return getDeviation(90);
    }

    @Override
    public void setDeviation090(String deviation)
    {
        setDeviation(90, deviation);
    }

    @Override
    public String getDeviation100()
    {
        return getDeviation(100);
    }

    @Override
    public void setDeviation100(String deviation)
    {
        setDeviation(100, deviation);
    }

    @Override
    public String getDeviation110()
    {
        return getDeviation(110);
    }

    @Override
    public void setDeviation110(String deviation)
    {
        setDeviation(110, deviation);
    }

    @Override
    public String getDeviation120()
    {
        return getDeviation(120);
    }

    @Override
    public void setDeviation120(String deviation)
    {
        setDeviation(120, deviation);
    }

    @Override
    public String getDeviation130()
    {
        return getDeviation(130);
    }

    @Override
    public void setDeviation130(String deviation)
    {
        setDeviation(130, deviation);
    }

    @Override
    public String getDeviation140()
    {
        return getDeviation(140);
    }

    @Override
    public void setDeviation140(String deviation)
    {
        setDeviation(140, deviation);
    }

    @Override
    public String getDeviation150()
    {
        return getDeviation(150);
    }

    @Override
    public void setDeviation150(String deviation)
    {
        setDeviation(150, deviation);
    }

    @Override
    public String getDeviation160()
    {
        return getDeviation(160);
    }

    @Override
    public void setDeviation160(String deviation)
    {
        setDeviation(160, deviation);
    }

    @Override
    public String getDeviation170()
    {
        return getDeviation(170);
    }

    @Override
    public void setDeviation170(String deviation)
    {
        setDeviation(170, deviation);
    }

    @Override
    public String getDeviation180()
    {
        return getDeviation(180);
    }

    @Override
    public void setDeviation180(String deviation)
    {
        setDeviation(180, deviation);
    }

    @Override
    public String getDeviation190()
    {
        return getDeviation(190);
    }

    @Override
    public void setDeviation190(String deviation)
    {
        setDeviation(190, deviation);
    }

    @Override
    public String getDeviation200()
    {
        return getDeviation(200);
    }

    @Override
    public void setDeviation200(String deviation)
    {
        setDeviation(200, deviation);
    }

    @Override
    public String getDeviation210()
    {
        return getDeviation(210);
    }

    @Override
    public void setDeviation210(String deviation)
    {
        setDeviation(210, deviation);
    }

    @Override
    public String getDeviation220()
    {
        return getDeviation(220);
    }

    @Override
    public void setDeviation220(String deviation)
    {
        setDeviation(220, deviation);
    }

    @Override
    public String getDeviation230()
    {
        return getDeviation(230);
    }

    @Override
    public void setDeviation230(String deviation)
    {
        setDeviation(230, deviation);
    }

    @Override
    public String getDeviation240()
    {
        return getDeviation(240);
    }

    @Override
    public void setDeviation240(String deviation)
    {
        setDeviation(240, deviation);
    }

    @Override
    public String getDeviation250()
    {
        return getDeviation(250);
    }

    @Override
    public void setDeviation250(String deviation)
    {
        setDeviation(250, deviation);
    }

    @Override
    public String getDeviation260()
    {
        return getDeviation(260);
    }

    @Override
    public void setDeviation260(String deviation)
    {
        setDeviation(260, deviation);
    }

    @Override
    public String getDeviation270()
    {
        return getDeviation(270);
    }

    @Override
    public void setDeviation270(String deviation)
    {
        setDeviation(270, deviation);
    }

    @Override
    public String getDeviation280()
    {
        return getDeviation(280);
    }

    @Override
    public void setDeviation280(String deviation)
    {
        setDeviation(280, deviation);
    }

    @Override
    public String getDeviation290()
    {
        return getDeviation(290);
    }

    @Override
    public void setDeviation290(String deviation)
    {
        setDeviation(290, deviation);
    }

    @Override
    public String getDeviation300()
    {
        return getDeviation(300);
    }

    @Override
    public void setDeviation300(String deviation)
    {
        setDeviation(300, deviation);
    }

    @Override
    public String getDeviation310()
    {
        return getDeviation(310);
    }

    @Override
    public void setDeviation310(String deviation)
    {
        setDeviation(310, deviation);
    }

    @Override
    public String getDeviation320()
    {
        return getDeviation(320);
    }

    @Override
    public void setDeviation320(String deviation)
    {
        setDeviation(320, deviation);
    }

    @Override
    public String getDeviation330()
    {
        return getDeviation(330);
    }

    @Override
    public void setDeviation330(String deviation)
    {
        setDeviation(330, deviation);
    }

    @Override
    public String getDeviation340()
    {
        return getDeviation(340);
    }

    @Override
    public void setDeviation340(String deviation)
    {
        setDeviation(340, deviation);
    }

    @Override
    public String getDeviation350()
    {
        return getDeviation(350);
    }

    @Override
    public void setDeviation350(String deviation)
    {
        setDeviation(350, deviation);
    }
    
}
