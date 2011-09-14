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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

/**
 * This class represents a led matrix consisting of several XMOS kits with the led matrix source code.
 * User: marcus
 */
public class LEDDisplay {
  private static short AUTOCONFIGURATION_ID = 0x08;
  /**
   * THE port number where the XMOS software is listening
   */
  private static final int PORT_XMOS = 306;

  /**
   * just a logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(LEDDisplay.class);

  DatagramSocket outputSocket;

  public LEDDisplay() {
    try {
      outputSocket = new DatagramSocket();
    }
    catch (SocketException e) {
      final String MSG = "Unable to open datagram socket.";
      LOGGER.error(MSG, e);
      throw new DisplayException(MSG, e);
    }
  }

  public void configureDisplays() {
    XMOSLEDMatrixPayload payload = new XMOSLEDMatrixPayload(AUTOCONFIGURATION_ID);
    sendXMOSPackage(payload);

  }

  private void sendXMOSPackage(XMOSLEDMatrixPayload payload) {
    byte[] udpPacket = payload.getPayloadAsBytes();
    DatagramPacket packet = new DatagramPacket(udpPacket, udpPacket.length);
    try {
      outputSocket.send(packet);
    }
    catch (IOException e) {
      throw new DisplayException("Unable to send the data package", e);
    }
  }

  public InetAddress[] getLocalAddresses() throws SocketException {
    List<InetAddress> localAddresses = new LinkedList<InetAddress>();
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
            localAddresses.add(inetAddress);
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
    ArrayList<InetAddress> list = new ArrayList<InetAddress>(localAddresses);
    return list.toArray(new InetAddress[list.size()]);
  }
}
