/*
 * Copyright (C) 2014 tkv
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

package org.vesalainen.parsers.mmsi;

import java.net.URL;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.tags.TableRow;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.SimpleNodeIterator;

/**
 *
 * @author tkv
 */
public class CreateMMSIMIDs
{
    private static String[] getText(Node[] nodes)
    {
        String[] arr = new String[nodes.length];
        for (int ii=0;ii<nodes.length;ii++)
        {
            arr[ii] = getText(nodes[ii]);
        }
        return arr;
    }
    private static String getText(Node node)
    {
        NodeList children = node.getChildren();
        if (children != null)
        {
            SimpleNodeIterator elements = children.elements();
            while (elements.hasMoreNodes())
            {
                Node n = elements.nextNode();
                if (n instanceof TextNode)
                {
                    TextNode text = (TextNode) n;
                    return text.getText();
                }
                else
                {
                    String text = getText(n);
                    if (text != null)
                    {
                        return text;
                    }
                }
            }
        }
        return null;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            URL url = new URL("http://www.vtexplorer.com/vessel-tracking-mmsi-mid-codes.html");
            Parser parser = new Parser(url.openConnection());
            NodeList nl = (NodeList) parser.parse(new NodeClassFilter(TableRow.class));
            SimpleNodeIterator elements = nl.elements();
            while (elements.hasMoreNodes())
            {
                TableRow row = (TableRow) elements.nextNode();
                TableColumn[] cols = row.getColumns();
                String[] hdsr = getText(cols);
                if (hdsr.length == 2)
                {
                    String[] ss = hdsr[0].split(", ");
                    for (String s : ss)
                    {
                        System.err.println("map.put("+s+", \""+hdsr[1]+"\");");
                    }
                }

            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
}
