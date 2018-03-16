/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.nmea.icommanager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CheckedOutputStream;
import org.vesalainen.comm.channel.SerialChannel;
import org.vesalainen.comm.channel.SerialChannel.Builder;
import static org.vesalainen.comm.channel.SerialChannel.Speed.B4800;
import org.vesalainen.nio.channels.ByteBufferOutputStream;
import org.vesalainen.parsers.nmea.AbstractNMEAObserver;
import org.vesalainen.parsers.nmea.NMEAChecksum;
import org.vesalainen.parsers.nmea.NMEAParser;

/**
 * $PICOA,90,00,REMOTE,ON*58
                                                     
$PICOA,90,00,MODE,USB*1A
                                                      
$PICOA,90,00,RXF,7.055000*08
                                                  
$PICOA,90,00,TXF,7.055000*0E
                                                  
$PICOA,90,00,REMOTE,ON*58
                                                     
$PICOA,90,00,MODE,USB*1A
                                                      
$PICOA,90,00,RXF,7.053000*0E

Works without checkdigit if it is omitted

Returns the accepted sentence

$PICOA,90,00,ALL

$PICOA,90,00,ALL
Returns:

$PICOA,08,90,RFG,9*3F
                                                         
$PICOA,08,90,TXP,3*3A
                                                         
$PICOA,08,90,AGC,ON*11
                                                        
$PICOA,08,90,NB,OFF*16
                                                        
$PICOA,08,90,SQLC,OFF*17
                                                      
$PICOA,08,90,AFG,18*1C
                                                        
$PICOA,08,90,TUNER,OFF*42
                                                     
$PICOA,08,90,TRX,RX*01
                                                        
$PICOA,08,90,SQLS,OPEN*5C
                                                     
$PICOA,08,90,SIGM,0*75
                                                        
$PICOA,08,90,POM,0*37
                                                         
$PICOA,08,90,ANTM,0*73
                                                        
$PICOA,08,90,SP,ON*57
                                                         
$PICOA,08,90,DIM,ON*14
                                                        
$PICOA,08,90,REMOTE,OFF*1E
    

                                                  
$PICOA,90,00,TXF,7.053000*08
     

 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class IcomManager extends AbstractNMEAObserver implements Runnable, AutoCloseable 
{
    private int id;
    private Thread thread;
    private final SerialChannel serialChannel;
    private Semaphore initSemaphore = new Semaphore(0);
    private Semaphore syncSemaphore = new Semaphore(0);
    private final ByteBuffer bb = ByteBuffer.allocate(100); //.allocateDirect(100); TODO
    private final ByteBufferOutputStream out = new ByteBufferOutputStream(bb);
    private final CheckedOutputStream cout = new CheckedOutputStream(out, new NMEAChecksum());
    private final Map<String,String> map = new HashMap<>();
    private String waitKey;

    public IcomManager(int id, String port) throws IOException
    {
        this(id, SerialChannel.builder(port).setSpeed(B4800).build());
    }

    public IcomManager(int id, SerialChannel serialChannel)
    {
        this.id = id;
        this.serialChannel = serialChannel;
    }

    public static IcomManager getInstance() throws IOException, InterruptedException
    {
        return getInstance(0);
    }
    public static IcomManager getInstance(int id) throws IOException, InterruptedException
    {
        for (String port : SerialChannel.getFreePorts())
        {
            IcomManager manager = new IcomManager(id, port);
            if (manager.parse())
            {
                return manager;
            }
        }
        return null;
    }
    
    public boolean setRemote(boolean on) throws IOException, InterruptedException
    {
        return cmd("REMOTE", on);
    }
    public boolean setReceiveFrequency(double mHz) throws IOException, InterruptedException
    {
        return cmd("RXF", mHz);
    }
    private boolean cmd(String key, double value) throws IOException, InterruptedException
    {
        return cmd(key, String.format(Locale.US, "%f", value));
    }
    private boolean cmd(String key, boolean value) throws IOException, InterruptedException
    {
        return cmd(key, value ? "ON" : "OFF");
    }
    private boolean cmd(String key, String value) throws IOException, InterruptedException
    {
        bb.clear();
        IcomNMEAGen.set(cout, id, key, value);
        bb.flip();
        System.err.println(new String(bb.array()));
        serialChannel.write(bb);
        waitKey = key;
        return syncSemaphore.tryAcquire(1, TimeUnit.SECONDS);
    }
    protected boolean parse() throws InterruptedException, IOException
    {
        thread = new Thread(this);
        thread.start();
        bb.clear();
        IcomNMEAGen.all(cout, id);
        bb.flip();
        serialChannel.write(bb);
        return initSemaphore.tryAcquire(5, TimeUnit.SECONDS);
    }

    @Override
    public void commit(String reason)
    {
        System.err.println(reason);
    }

    @Override
    public void rollback(String reason)
    {
        throw new IllegalArgumentException("rollback: "+reason);
    }

    @Override
    public void setProprietaryData(List<CharSequence> data)
    {
        String key = data.get(2).toString();
        String value = data.size() >= 4 ? data.get(3).toString() : null;
        map.put(key, value);
        if (key.equals(waitKey))
        {
            waitKey = null;
            syncSemaphore.release();
        }
    }

    @Override
    public void setProprietaryType(CharSequence type)
    {
        if (initSemaphore != null && "ICOA".equals(type))
        {
            initSemaphore.release();
            initSemaphore = null;
        }
    }
    
    @Override
    public void run()
    {
        try
        {
            NMEAParser parser = NMEAParser.newInstance();
            parser.parse(serialChannel, this, null);
        }
        catch (IOException ex)
        {
            Logger.getLogger(IcomManager.class.getName()).log(Level.SEVERE, null, ex);
        }
            
    }

    @Override
    public void close() throws IOException, InterruptedException
    {
        setRemote(false);
    }
}
