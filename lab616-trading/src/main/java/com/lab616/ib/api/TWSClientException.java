// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api;

/**
 * @author david
 *
 */
public class TWSClientException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public TWSClientException(String msg) {
    super(msg);
  }
  
  public TWSClientException(Throwable t) {
    super(t);
  }
}
