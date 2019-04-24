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

import java.io.IOException;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public interface DeviationMXBean
{
    String getPath();
    double getVariation();
    void rotate(double diff);
    String getDeviation000();

    String getDeviation010();

    String getDeviation100();

    String getDeviation110();

    String getDeviation120();

    String getDeviation130();

    String getDeviation140();

    String getDeviation150();

    String getDeviation160();

    String getDeviation170();

    String getDeviation180();

    String getDeviation190();

    String getDeviation020();

    String getDeviation200();

    String getDeviation210();

    String getDeviation220();

    String getDeviation230();

    String getDeviation240();

    String getDeviation250();

    String getDeviation260();

    String getDeviation270();

    String getDeviation280();

    String getDeviation290();

    String getDeviation030();

    String getDeviation300();

    String getDeviation310();

    String getDeviation320();

    String getDeviation330();

    String getDeviation340();

    String getDeviation350();

    String getDeviation040();

    String getDeviation050();

    String getDeviation060();

    String getDeviation070();

    String getDeviation080();

    String getDeviation090();

    void load() throws IOException;

    void reset();

    void setDeviation000(String deviation);

    void setDeviation010(String deviation);

    void setDeviation100(String deviation);

    void setDeviation110(String deviation);

    void setDeviation120(String deviation);

    void setDeviation130(String deviation);

    void setDeviation140(String deviation);

    void setDeviation150(String deviation);

    void setDeviation160(String deviation);

    void setDeviation170(String deviation);

    void setDeviation180(String deviation);

    void setDeviation190(String deviation);

    void setDeviation020(String deviation);

    void setDeviation200(String deviation);

    void setDeviation210(String deviation);

    void setDeviation220(String deviation);

    void setDeviation230(String deviation);

    void setDeviation240(String deviation);

    void setDeviation250(String deviation);

    void setDeviation260(String deviation);

    void setDeviation270(String deviation);

    void setDeviation280(String deviation);

    void setDeviation290(String deviation);

    void setDeviation030(String deviation);

    void setDeviation300(String deviation);

    void setDeviation310(String deviation);

    void setDeviation320(String deviation);

    void setDeviation330(String deviation);

    void setDeviation340(String deviation);

    void setDeviation350(String deviation);

    void setDeviation040(String deviation);

    void setDeviation050(String deviation);

    void setDeviation060(String deviation);

    void setDeviation070(String deviation);

    void setDeviation080(String deviation);

    void setDeviation090(String deviation);

    void store() throws IOException;
    
}
