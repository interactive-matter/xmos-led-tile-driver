package eu.interactivematter.xmosledtiledriver;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.LinkedList;

/**
 * Copyright (c) 2011, Interactive Matter, Marcus Nowotny, All rights reserved
 * This software is freely distributable under a derivative of the
 * University of Illinois/NCSA Open Source License posted in
 * LICENSE.txt and at <http://github.xcore.com/>
 */
public class XMOSLedTileResponseListener {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(XMOSLedTileResponseListener.class);

  private LinkedList<DatagramPacket> packets = new LinkedList<DatagramPacket>();
  private UPDListener listener;
  private Thread listenerThread;

  public XMOSLedTileResponseListener() {
    listener = new UPDListener();
    listenerThread = new Thread(listener);
    listenerThread.start();
  }

  private synchronized void addPacket(DatagramPacket packet) {
    packets.add(packet);
  }

  public boolean hasPacket() {
    return packets.size() > 0;
  }


  private class UPDListener implements Runnable {

    private static final String MAGIC_STRING = XMOSLedTilePacketPayload.MAGIC_STRING;
    private DatagramSocket socket;
    private boolean running = true;

    private UPDListener() {
      try {
        socket = new DatagramSocket();
      }
      catch (SocketException e) {
        throw new XMOSLedTileDriverException("Unable to create listening socket", e);
      }
    }

    public void setRunning(boolean running) {
      this.running = running;
    }

    public void run() {
      while (running) {
        try {
          //create a buffer for the datagram packet
          byte[] buf = new byte[XMOSLedTilePacketPayload.MAX_PAYLOAD_SIZE];
          //receive a packet
          DatagramPacket packet = new DatagramPacket(buf, buf.length);
          socket.receive(packet);
          LOGGER.debug("Packet received {}", packet);
          //analyze the data if it is a XMOS packet
          byte[] data = packet.getData();
          //we must have at least XMOS + package ID
          if (data.length > 5) {
            byte[] magicString = new byte[MAGIC_STRING.length()];
            System.arraycopy(data, 0, magicString, 0, MAGIC_STRING.length());
            String packageMagicString = new String(magicString);
            if (MAGIC_STRING.equals(packageMagicString)) {
              LOGGER.info("Retrieved a XMOS package from {}:{}", packet.getAddress(), packet.getSocketAddress());
              addPacket(packet);
            }

          }
        }
        catch (IOException e) {
          LOGGER.error("Unable to retrieve packet, ignoring it.", e);
        }

      }
    }
  }
}
