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
public abstract class DeviationReadWriteBean extends DeviationReadBean implements DeviationReadWriteMXBean
{
    
    public DeviationReadWriteBean()
    {
    }

    protected abstract void setDeviation(int deg, String deviation);


    @Override
    public void setDeviation000(String deviation)
    {
        setDeviation(00, deviation);
    }


    @Override
    public void setDeviation010(String deviation)
    {
        setDeviation(10, deviation);
    }


    @Override
    public void setDeviation020(String deviation)
    {
        setDeviation(20, deviation);
    }


    @Override
    public void setDeviation030(String deviation)
    {
        setDeviation(30, deviation);
    }


    @Override
    public void setDeviation040(String deviation)
    {
        setDeviation(40, deviation);
    }


    @Override
    public void setDeviation050(String deviation)
    {
        setDeviation(50, deviation);
    }


    @Override
    public void setDeviation060(String deviation)
    {
        setDeviation(60, deviation);
    }


    @Override
    public void setDeviation070(String deviation)
    {
        setDeviation(70, deviation);
    }


    @Override
    public void setDeviation080(String deviation)
    {
        setDeviation(80, deviation);
    }


    @Override
    public void setDeviation090(String deviation)
    {
        setDeviation(90, deviation);
    }


    @Override
    public void setDeviation100(String deviation)
    {
        setDeviation(100, deviation);
    }


    @Override
    public void setDeviation110(String deviation)
    {
        setDeviation(110, deviation);
    }


    @Override
    public void setDeviation120(String deviation)
    {
        setDeviation(120, deviation);
    }


    @Override
    public void setDeviation130(String deviation)
    {
        setDeviation(130, deviation);
    }


    @Override
    public void setDeviation140(String deviation)
    {
        setDeviation(140, deviation);
    }


    @Override
    public void setDeviation150(String deviation)
    {
        setDeviation(150, deviation);
    }


    @Override
    public void setDeviation160(String deviation)
    {
        setDeviation(160, deviation);
    }


    @Override
    public void setDeviation170(String deviation)
    {
        setDeviation(170, deviation);
    }


    @Override
    public void setDeviation180(String deviation)
    {
        setDeviation(180, deviation);
    }


    @Override
    public void setDeviation190(String deviation)
    {
        setDeviation(190, deviation);
    }


    @Override
    public void setDeviation200(String deviation)
    {
        setDeviation(200, deviation);
    }


    @Override
    public void setDeviation210(String deviation)
    {
        setDeviation(210, deviation);
    }


    @Override
    public void setDeviation220(String deviation)
    {
        setDeviation(220, deviation);
    }


    @Override
    public void setDeviation230(String deviation)
    {
        setDeviation(230, deviation);
    }


    @Override
    public void setDeviation240(String deviation)
    {
        setDeviation(240, deviation);
    }


    @Override
    public void setDeviation250(String deviation)
    {
        setDeviation(250, deviation);
    }


    @Override
    public void setDeviation260(String deviation)
    {
        setDeviation(260, deviation);
    }


    @Override
    public void setDeviation270(String deviation)
    {
        setDeviation(270, deviation);
    }


    @Override
    public void setDeviation280(String deviation)
    {
        setDeviation(280, deviation);
    }


    @Override
    public void setDeviation290(String deviation)
    {
        setDeviation(290, deviation);
    }


    @Override
    public void setDeviation300(String deviation)
    {
        setDeviation(300, deviation);
    }


    @Override
    public void setDeviation310(String deviation)
    {
        setDeviation(310, deviation);
    }


    @Override
    public void setDeviation320(String deviation)
    {
        setDeviation(320, deviation);
    }


    @Override
    public void setDeviation330(String deviation)
    {
        setDeviation(330, deviation);
    }


    @Override
    public void setDeviation340(String deviation)
    {
        setDeviation(340, deviation);
    }


    @Override
    public void setDeviation350(String deviation)
    {
        setDeviation(350, deviation);
    }
    
}
