import eu.interactivematter.xmosledtiledriver.XMOSLedTileDriver;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * A test class to test different display data.
 * <p/>
 * <p/>
 * Copyright (c) 2011, Interactive Matter, Marcus Nowotny, All rights reserved
 * This software is freely distributable under a derivative of the
 * University of Illinois/NCSA Open Source License posted in
 * LICENSE.txt and at <http://github.xcore.com/>
 */
public class TestDisplayData {

  private XMOSLedTileDriver driver;
  //silly display data in x,y,color
  private byte[][][] displayData = new byte[16][16][3];
  private InetAddress targetAddress;

  @Before
  public void setup() throws UnknownHostException {
    driver = new XMOSLedTileDriver();
    targetAddress = InetAddress.getByName("192.168.0.254");
  }

  @Test
  public void sendSimplePatterns() throws IOException, InterruptedException {
    for (int c = 0; c < 3; c++) {
      for (int i = 0; i < 256; i++) {
        for (byte[][] column : displayData) {
          for (byte[] colorValues : column) {
            colorValues[c] = (byte) i;
          }
        }
        driver.setPixelData((short) 0, getDisplayData(), targetAddress);
        waitMilliseconds(100);
      }
    }

  }

  private byte[] getDisplayData() throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream(16 * 16 * 3);
    for (byte[][] column : displayData) {
      for (byte[] colorValues : column) {
        outputStream.write(colorValues);
      }
    }
    return outputStream.toByteArray();
  }

  private void waitMilliseconds(long milliseconds) throws InterruptedException {
    synchronized (this) {
      wait(milliseconds);
    }
  }
}
