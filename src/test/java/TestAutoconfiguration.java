import eu.interactivematter.xmosleddriver.LEDDisplay;
import org.junit.Before;
import org.junit.Test;

import java.net.Inet4Address;
import java.net.SocketException;
import java.util.List;

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
    List<Inet4Address> addresses = display.getLocalAddresses();
    assertThat(addresses.size(), greaterThan(0));
  }

  @Test
  public void testAutoconfiguration() {
    display.configureDisplays();
    //TODO test if at least one XC3 is connected
  }

}
