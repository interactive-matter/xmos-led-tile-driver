package eu.interactivematter.xmosledtiledriver.packets;

import eu.interactivematter.xmosledtiledriver.XMOSLedTileDriver;
import eu.interactivematter.xmosledtiledriver.XMOSLedTilePacketPayload;

/**
 * <p/>
 * Copyright (c) 2011, Interactive Matter, Marcus Nowotny, All rights reserved
 * This software is freely distributable under a derivative of the
 * University of Illinois/NCSA Open Source License posted in
 * LICENSE.txt and at <http://github.xcore.com/>
 */
public class LatchPacket extends XMOSLedTilePacketPayload {
  /**
   * creates a latch package
   */
  public LatchPacket() {
    super(XMOSLedTileDriver.LATCH);
  }
}
