/*
 * Copyright (C) 2013 Timo Vesalainen
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

package org.vesalainen.parsers.nmea.ais;

import org.vesalainen.grammar.BnfGrammar;
import org.vesalainen.grammar.BnfParser;
import org.vesalainen.grammar.Grammar;
import org.vesalainen.parser.GenClassFactory;

/**
 * @author Timo Vesalainen
 */
public class NewMain
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        try
        {
            BnfGrammar p = BnfGrammar.newInstance();
            p.parseRhs("('\\|' cell?)+ '[\r\n]+'", new Grammar());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

}
