import eu.interactivematter.xmosleddriver.LEDDisplay;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.net.SocketException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * To be able to run this test there must be at least on XC-3 connected to any network interface of the local host
 * User: marcus
 */
public class TestAutoconfiguration {

  private LEDDisplay display;

  @Before
  public void setup() {
    display = new LEDDisplay();
  }

  @Test
  public void testLocalAddressResolver() throws SocketException {
    InetAddress[] addresses = display.getLocalAddresses();
    assertThat(addresses.length, greaterThan(0));
  }
}
