// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api;

import com.ib.client.EClientSocket;
import com.ib.client.EWrapper;

/**
 * Factory for creating an EClientSocket.
 *
 * @author david
 *
 */
public interface EClientSocketFactory {

  /**
   * Creates an EClientSocket.
   * @param profile The name of connection profile.
   * @param wrapper The warpper implementation.
   * @param simulate Optional.  True if simulating.
   * @return An EClientSocket.
   */
  public EClientSocket create(String profile, EWrapper wrapper, boolean... simulate);
}
