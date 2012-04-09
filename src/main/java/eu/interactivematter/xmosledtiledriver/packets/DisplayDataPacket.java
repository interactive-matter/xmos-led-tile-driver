package eu.interactivematter.xmosledtiledriver.packets;

import eu.interactivematter.xmosledtiledriver.XMOSLedTileDriver;
import eu.interactivematter.xmosledtiledriver.XMOSLedTileDriverException;
import eu.interactivematter.xmosledtiledriver.XMOSLedTilePacketPayload;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * <p/>
 * Copyright (c) 2011, Interactive Matter, Marcus Nowotny, All rights reserved
 * This software is freely distributable under a derivative of the
 * University of Illinois/NCSA Open Source License posted in
 * LICENSE.txt and at <http://github.xcore.com/>
 */
public class DisplayDataPacket extends XMOSLedTilePacketPayload {
  private short pixelPointer;
  private byte[] pixelData;

  public DisplayDataPacket() {
    super(XMOSLedTileDriver.DATA);
  }

  //TODO this is no nice interface!
  public void setPixelData(short pixelPointer, byte[] pixelData) {
    //TODO how big can the pixel data get??
    //TODO don't we want to get some sanity check for the data??
    this.pixelPointer = pixelPointer;
    this.pixelData = pixelData;
  }

  @Override
  public byte[] getPayloadAsBytes() {
    ByteBuffer payloadBuffer = getPayloadBuffer();
    //we encode 0 as width & height
    payloadBuffer.putShort((short) 0); //the tile width
    payloadBuffer.putShort((short) 0); //the tile height
    //the next two positions are reserved
    payloadBuffer.put(new byte[]{0, 0});
    //next write out the pixel pointer
    payloadBuffer.putShort(pixelPointer);
    payloadBuffer.putShort((short) pixelData.length);
    //the next two positions are reserved
    payloadBuffer.put(new byte[]{0, 0});
    payloadBuffer.put(pixelData);
    return super.getPayloadAsBytes();
  }
}
