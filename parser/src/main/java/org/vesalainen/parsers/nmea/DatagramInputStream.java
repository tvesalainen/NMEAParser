/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.vesalainen.parsers.nmea;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DatagramInputStream extends InputStream
{
    private DatagramSocket socket;
    private DatagramPacket packet;
    private byte[] buffer;
    private int offset;
    private int length;

    public DatagramInputStream(int port) throws SocketException, UnknownHostException
    {
        socket = new DatagramSocket(port, InetAddress.getByName("0.0.0.0"));
        buffer = new byte[256];
        packet = new DatagramPacket(buffer, buffer.length);
    }

    @Override
    public int read() throws IOException
    {
        if (offset == length)
        {
            packet.setData(buffer);
            socket.receive(packet);
            offset = 0;
            length = packet.getLength();
            String line = new String(buffer, 0, length);
            System.err.print(line);
        }
        return buffer[offset++];
    }
    
}
