package eu.interactivematter.xmosledtiledriver.packets;

import eu.interactivematter.xmosledtiledriver.XMOSLedTileDriver;
import eu.interactivematter.xmosledtiledriver.XMOSLedTileDriverException;
import eu.interactivematter.xmosledtiledriver.XMOSLedTilePacketPayload;

import java.io.DataOutputStream;
import java.io.IOException;

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
    try {
      DataOutputStream stream = getDataStream();
      //we encode 0 as width & height
      stream.writeShort(0); //the tile width
      stream.writeShort(0); //the tile height
      //the next two positions are reserved
      stream.write(new byte[]{0, 0});
      //next write out the pixel pointer
      stream.writeShort(pixelPointer);
      stream.writeShort(pixelData.length);
      //the next two positions are reserved
      stream.write(new byte[]{0, 0});
      stream.write(pixelData);
    }
    catch (IOException e) {
      throw new XMOSLedTileDriverException("Unable to write to UDP data stream", e);
    }
    return super.getPayloadAsBytes();
  }
}
