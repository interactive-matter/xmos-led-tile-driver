package eu.interactivematter.xmosleddriver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Telekom .COM Relaunch 2011
 * User: marcus
 */
public class XMOSLEDMatrixPayload {
  public static final int MAX_PAYLOAD_SIZE = 1500;
  public static final String MAGIC_STRING = "XMOS";

  private static final Logger LOGGER = LoggerFactory.getLogger(XMOSLEDMatrixPayload.class);

  private short messageId = 0;

  private ByteArrayOutputStream payloadStream = new ByteArrayOutputStream(MAX_PAYLOAD_SIZE);
  private DataOutputStream dataStream = new DataOutputStream(payloadStream);

  public XMOSLEDMatrixPayload(short messageId) {
    this.messageId = messageId;
    //all messages start with the magic word 'xmos'
  }

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

  public short getMessageId() {
    return messageId;
  }

  public void setMessageId(short messageId) {
    this.messageId = messageId;
    throw new XMOSLedTileDriverException("not implemented yet");
    //TODO set message id in output stream
  }
}
