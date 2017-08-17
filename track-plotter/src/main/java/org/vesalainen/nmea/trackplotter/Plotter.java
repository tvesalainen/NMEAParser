/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.nmea.trackplotter;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import org.vesalainen.nmea.util.TrackPlotter;
import org.vesalainen.util.CmdArgs;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Plotter extends CmdArgs
{

    public Plotter()
    {
        addArgument(File.class, "file");
        addOption("-w", "width", null, 1000);
        addOption("-h", "height", null, 1000);
        addOption("-ext", "[svg|jpg|png|gif]", null, "png");
    }
    
    public void plot() throws IOException
    {
        File file = (File) getArgument("file");
        String ext = (String) getOption("-ext");
        int width = (int) getOption("-w");
        int height = (int) getOption("-h");
        TrackPlotter trackPlotter = new TrackPlotter(file, width, height, Color.BLACK, Color.WHITE);
        String path = file.getPath();
        int idx = path.lastIndexOf('.');
        if (idx != -1)
        {
            path = path.substring(0, idx);
        }
        path = path+'.'+ext;
        trackPlotter.plot(path, ext);
    }
    
    public static void main(String... args)
    {
        try
        {
            Plotter plotter = new Plotter();
            plotter.setArgs(args);
            plotter.plot();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
