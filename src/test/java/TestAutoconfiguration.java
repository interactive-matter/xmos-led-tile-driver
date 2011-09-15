import eu.interactivematter.xmosleddriver.XMOSLedTileDriver;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.SocketException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * To be able to run this test there must be at least on XC-3 connected to any network interface of the local host
 * User: marcus
 */
public class TestAutoconfiguration {
  Logger LOGGER = LoggerFactory.getLogger(TestAutoconfiguration.class);

  private XMOSLedTileDriver display;

  @Before
  public void setup() {
    display = new XMOSLedTileDriver();
  }

  @Test
  public void testLocalAddressResolver() throws SocketException {
    Inet4Address address = display.getLocalAddress();
    assertThat(address, notNullValue());
  }

  @Test
  public void testAutoconfiguration() {
    display.configureDisplays();
    //nothing else to test here since the XMOS devices are so quiet
  }
}
