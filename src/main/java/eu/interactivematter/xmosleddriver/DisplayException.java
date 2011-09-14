package eu.interactivematter.xmosleddriver;

/**
 * Telekom .COM Relaunch 2011
 * User: marcus
 */
public class DisplayException extends RuntimeException {
  public DisplayException(String message, Throwable reason) {
    super(message, reason);
  }

  public DisplayException(String message) {
    super(message);
  }
}
