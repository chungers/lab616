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

  public EClientSocket create(String name, EWrapper wrapper);

}
