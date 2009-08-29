// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api;

/**
 * A stock symbol.
 *
 * @author david
 *
 */
public final class Stock extends Symbol {

  public Stock(int tickerId) {
    super(tickerId);
  }
  
  public Stock(String symbol) {
    super(symbol);
  }
}
