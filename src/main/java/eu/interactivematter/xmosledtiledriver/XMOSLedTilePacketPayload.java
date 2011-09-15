package eu.interactivematter.xmosledtiledriver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Represents the payload of the XMOS Led Tile UDP packages.
 * <strong>this class will most likely change massively during the development</strong>
 */
public class XMOSLedTilePacketPayload {
  public static final int MAX_PAYLOAD_SIZE = 1500;
  public static final String MAGIC_STRING = "XMOS";

  private static final Logger LOGGER = LoggerFactory.getLogger(XMOSLedTilePacketPayload.class);

  private short messageId = 0;

  private ByteArrayOutputStream payloadStream = new ByteArrayOutputStream(MAX_PAYLOAD_SIZE);
  private DataOutputStream dataStream = new DataOutputStream(payloadStream);

  /**
   * create payload for the given message id
   *
   * @param messageId
   */
  public XMOSLedTilePacketPayload(short messageId) {
    this.messageId = messageId;
    //all messages start with the magic word 'xmos'
  }

  /**
   * get the UDP payload as byte array to directly put it into an UDP package
   *
   * @return
   */
  public byte[] getPayloadAsBytes() {
    ByteArrayOutputStream udpByteStream = new ByteArrayOutputStream();
    DataOutputStream updDataStream = new DataOutputStream(udpByteStream);
    try {
      updDataStream.writeBytes(MAGIC_STRING);
      updDataStream.writeShort(messageId);
      updDataStream.flush();
      dataStream.flush();
      payloadStream.writeTo(updDataStream);
      udpByteStream.flush();

      if (payloadStream.size() > MAX_PAYLOAD_SIZE) {
        throw new XMOSLedTileDriverException("The output stream can only be " + MAX_PAYLOAD_SIZE + " bytes long");
      }
      return udpByteStream.toByteArray();
    }
    catch (IOException e) {
      throw new XMOSLedTileDriverException("unable to construct UDP data", e);
    }
  }

  /**
   * the message id
   *
   * @return the message ID
   */
  public short getMessageId() {
    return messageId;
  }
}
