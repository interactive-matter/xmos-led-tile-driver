package eu.interactivematter.xmosleddriver;

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
import java.util.LinkedList;
import java.util.List;

/**
 * This class represents a led matrix consisting of several XMOS kits with the led matrix source code.
 * User: marcus
 */
public class LEDDisplay {
  private static short AUTOCONFIGURATION_ID = 0x08;
  private static short AUTOCONFIGURATION_END = 0x0A;
  /**
   * the port number where the XMOS software is listening
   */
  public static final int XMOS_PORT = 306;
  //default wait time for autoconfigure
  private static final int DEFAULT_WAIT_TIME = 10 * 1000;

  /**
   * just a logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(LEDDisplay.class);

  DatagramSocket outputSocket;
  List<Inet4Address> localAdresses;

  public LEDDisplay() {
    //retrieve the local adresses
    try {
      localAdresses = getLocalAddresses();
    }
    catch (SocketException e) {
      throw new DisplayException("Unable to get the local network adresses!");
    }
    try {
      outputSocket = new DatagramSocket();
    }
    catch (SocketException e) {
      final String MSG = "Unable to open datagram socket.";
      LOGGER.error(MSG, e);
      throw new DisplayException(MSG, e);
    }
    LEDDisplayListener listener = new LEDDisplayListener(localAdresses);
  }

  public void configureDisplays() {
    configureDisplays(DEFAULT_WAIT_TIME);
  }

  public void configureDisplays(int waittime) {
    for (Inet4Address localAddress : localAdresses) {
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
      //now send a autoconfigure package to any ip address
      XMOSLEDMatrixPayload payload = new XMOSLEDMatrixPayload(AUTOCONFIGURATION_ID);
      sendXMOSPackage(payload, broadcastAddress, true);
      synchronized (this) {
        try {
          this.wait(waittime);
        }
        catch (InterruptedException e) {
          LOGGER.error("Got somehow interrupted while waiting - strange", e);
        }
      }
      LOGGER.info("Sending end of autocofiguration to {}", broadcastAddress);
      payload = new XMOSLEDMatrixPayload(AUTOCONFIGURATION_END);
      sendXMOSPackage(payload, broadcastAddress, true);
    }

  }

  private void sendXMOSPackage(XMOSLEDMatrixPayload payload, InetAddress address, boolean broadcast) {
    LOGGER.debug("Sending package with id {} to {}", payload.getMessageId(), address);
    byte[] udpPacket = payload.getPayloadAsBytes();
    DatagramPacket packet = new DatagramPacket(udpPacket, udpPacket.length, address, XMOS_PORT);
    try {
      outputSocket.setBroadcast(broadcast);
      outputSocket.send(packet);
    }
    catch (IOException e) {
      LOGGER.error("Unable to send package to {}: {}", address, e.getMessage());
      //throw new DisplayException("Unable to send the data package", e);
    }
  }


  public List<Inet4Address> getLocalAddresses() throws SocketException {
    List<Inet4Address> localAddresses = new LinkedList<Inet4Address>();
    Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
    while (interfaces.hasMoreElements()) {
      NetworkInterface networkInterface = interfaces.nextElement();
      if (!networkInterface.isLoopback()) {
        LOGGER.debug("Getting addresses for {}", networkInterface.getDisplayName());
        Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
        while (addresses.hasMoreElements()) {
          final InetAddress inetAddress = addresses.nextElement();
          if (inetAddress instanceof Inet4Address) {
            LOGGER.debug("Found Address {} for {}.", inetAddress, networkInterface.getDisplayName());
            localAddresses.add((Inet4Address) inetAddress);
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
    //copy over the addresses to the result list
    return localAddresses;
  }
}
