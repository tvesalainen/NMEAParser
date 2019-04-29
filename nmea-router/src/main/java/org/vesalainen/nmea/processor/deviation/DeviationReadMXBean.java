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

    String getDeviation000();

    String getDeviation010();

    String getDeviation020();

    String getDeviation030();

    String getDeviation040();

    String getDeviation050();

    String getDeviation060();

    String getDeviation070();

    String getDeviation080();

    String getDeviation090();

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

    String getDeviation300();

    String getDeviation310();

    String getDeviation320();

    String getDeviation330();

    String getDeviation340();

    String getDeviation350();

    String getPath();

    double getVariation();

    void load() throws IOException;

    void reset();

    void store() throws IOException;
    
}
