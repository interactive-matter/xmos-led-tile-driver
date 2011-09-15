package eu.interactivematter.xmosledtiledriver;

/**
 * Copyright (c) 2011, Interactive Matter, Marcus Nowotny, All rights reserved
 * This software is freely distributable under a derivative of the
 * University of Illinois/NCSA Open Source License posted in
 * LICENSE.txt and at <http://github.xcore.com/>
 */
public class XMOSLedTileDriverException extends RuntimeException {
  public XMOSLedTileDriverException(String message, Throwable reason) {
    super(message, reason);
  }

  public XMOSLedTileDriverException(String message) {
    super(message);
  }
}
