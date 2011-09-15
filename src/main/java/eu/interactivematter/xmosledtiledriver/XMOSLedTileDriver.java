package eu.interactivematter.xmosledtiledriver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * This class represents a led matrix consisting of several XMOS kits with the led matrix source code.
 * <p/>
 * Copyright (c) 2011, Interactive Matter, Marcus Nowotny, All rights reserved
 * This software is freely distributable under a derivative of the
 * University of Illinois/NCSA Open Source License posted in
 * LICENSE.txt and at <http://github.xcore.com/>
 */
public class XMOSLedTileDriver {
  //TODO migrate to enumeration
  public static short VERSION = 0x01;
  public static short DATA = 0x02;
  public static short LATCH = 0x03;
  public static short GAMMAADJ = 0x04;
  public static short INTENSITYADJ = 0x05;
  public static short SINTENSITYADJ = 0x06;
  public static short RESET = 0x07;
  public static short AUTOCONFIGURATION_ID = 0x08;
  public static short AUTOCONFIGURATION_END = 0x0A;
  public static short SINTENSITYADJ_PIX = 0x0C;
  //TODO disabled in current version - will it come back??
  public static short CHANGEDRIVER = 0x0D;
  /**
   * the port number where the XMOS software is listening
   */
  public static final int XMOS_PORT = 306;
  //default wait time for autoconfigure
  private static final int DEFAULT_WAIT_TIME = 3 * 1000;
  /**
   * IP4 address prefix of the led tile adderss space
   */
  private static final byte[] XMOS_ADRESS_RPREFIX = {(byte) 192, (byte) 168};

  /**
   * just a logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(XMOSLedTileDriver.class);

  DatagramSocket outputSocket;
  Inet4Address localAddress;
  private XMOSLedTileResponseListener listener;

  /**
   * Create the XMOS LED Tile Display driver.
   * Keep in mind that at least one network address must be in the 192.168.x.y range. Preferably 192.168.254.254.
   */
  public XMOSLedTileDriver() {
    //retrieve the local address
    try {
      //the address is automatically assigned to localAddress (TODO lil' bit bad style)
      getLocalAddress();
    }
    catch (SocketException e) {
      throw new XMOSLedTileDriverException("Unable to get the local network address", e);
    }
    if (localAddress == null) {
      throw new XMOSLedTileDriverException("No suitable local address in range 192.168.x.y found");
    }
    try {
      //create a socket to drive the
      outputSocket = new DatagramSocket();
    }
    catch (SocketException e) {
      final String MSG = "Unable to open datagram socket.";
      LOGGER.error(MSG, e);
      throw new XMOSLedTileDriverException(MSG, e);
    }
    //create a listener to receive the return packets from the display
    listener = new XMOSLedTileResponseListener();
  }

  /**
   * Start the auto configure process of the led tiles
   */
  public void configureDisplays() {
    configureDisplays(DEFAULT_WAIT_TIME);
  }

  /**
   * Start the auto configure process of the led tiles with a specified timeout between the initialization of the config
   * routine and the finishing of the config (AC_1 vs. AC_3 packages).
   *
   * @param waittime after which time the AC_3 package should be sent (in ms);
   */
  public void configureDisplays(int waittime) {
    //construct a broadcast adress for the adresss
    byte[] addressBytes = localAddress.getAddress();
    //we can assume it is a 4 byte adress since it is IP4
    //and create a broadcast address from it
    addressBytes[2] = (byte) 0xff;
    addressBytes[3] = (byte) 0xff;
    InetAddress broadcastAddress = null;
    try {
      broadcastAddress = InetAddress.getByAddress(addressBytes);
    }
    catch (UnknownHostException e) {
      LOGGER.error("I cannot get an address for the address {} - this is strange, ignoring the address", e);
    }
    LOGGER.info("Sending autoconfiguration package to broadcast address {}", broadcastAddress);
    //now send a autoconfigure package to the broadcast address
    XMOSLedTilePacketPayload payload = new XMOSLedTilePacketPayload(AUTOCONFIGURATION_ID);
    sendXMOSPackage(payload, broadcastAddress, true);
    //wait the specified wait time
    synchronized (this) {
      try {
        this.wait(waittime);
      }
      catch (InterruptedException e) {
        LOGGER.error("Got somehow interrupted while waiting - strange", e);
      }
    }
    //and send the end of auto configuration package
    LOGGER.info("Sending end of autocofiguration to {}", broadcastAddress);
    payload = new XMOSLedTilePacketPayload(AUTOCONFIGURATION_END);
    sendXMOSPackage(payload, broadcastAddress, true);
  }

  /**
   * A helper routine to send the packages. It will change in the future.
   *
   * @param payload   the payload of the package
   * @param address   the address where the packet is sent to
   * @param broadcast is this a broadcast address or not
   */
  private void sendXMOSPackage(XMOSLedTilePacketPayload payload, InetAddress address, boolean broadcast) {
    LOGGER.debug("Sending package with id {} to {}", payload.getMessageId(), address);
    byte[] udpPacket = payload.getPayloadAsBytes();
    DatagramPacket packet = new DatagramPacket(udpPacket, udpPacket.length, address, XMOS_PORT);
    try {
      outputSocket.setBroadcast(broadcast);
      outputSocket.send(packet);
    }
    catch (IOException e) {
      LOGGER.error("Unable to send package to {}: {}", address, e.getMessage());
      //throw new XMOSLedTileDriverException("Unable to send the data package", e);
    }
  }


  public Inet4Address getLocalAddress() throws SocketException {
    //if we have not retrieved the address previously
    if (localAddress == null) {
      //get all the network interfaces
      Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
      //for each network interface
      while (interfaces.hasMoreElements()) {
        NetworkInterface networkInterface = interfaces.nextElement();
        //if it is no loopback interface (we will never reach someone over this
        if (!networkInterface.isLoopback()) {
          //get the ip addresses of that interface will most often be (IP4 & IP6 addresses)
          LOGGER.debug("Getting addresses for {}", networkInterface.getDisplayName());
          //for each address
          Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
          while (addresses.hasMoreElements()) {
            final InetAddress inetAddress = addresses.nextElement();
            //check if it is an IP4 address
            if (inetAddress instanceof Inet4Address) {
              Inet4Address inet4Address = (Inet4Address) inetAddress;
              //check if it is in the 192.168.x.y target range
              byte[] adressBytes = inet4Address.getAddress();
              //we can safely assume that we have 4 bytes
              if (adressBytes[0] == XMOS_ADRESS_RPREFIX[0] && adressBytes[1] == XMOS_ADRESS_RPREFIX[1]) {
                LOGGER.info("Found address {} for {} as XMOS Led Tile sub net address.", inetAddress, networkInterface.getDisplayName());
                localAddress = inet4Address;
                break;
              }
              else {
                LOGGER.info("Address {} of {} is not suitable for XMOS Led Tile application", inet4Address, networkInterface.getDisplayName());
              }
            }
            else {
              LOGGER.info("Address {} for {} will be ignored since the XMOS Led Tile can only work with IP4", inetAddress, networkInterface.getDisplayName());
            }
          }
        }
        else {
          LOGGER.info("Network interface {} will be ignored since it is a loopback interface", networkInterface.getDisplayName());
        }
      }
    }
    //return the known local address for this dirver
    return localAddress;
  }
}
