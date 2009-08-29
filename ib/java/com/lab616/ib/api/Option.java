// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api;

import org.joda.time.DateTime;

/**
 * An option symbol, including the type, strike, and expiration.
 * See {@linkplain http://www.optionsxpress.com/educate/opt_symbols.aspx}.
 * 
 * @author david
 *
 */
public final class Option extends Symbol {

  public enum Type {
    PUT,
    CALL,
  }
  
  public Option(String symbol, Type type, double strike, DateTime expiration) {
    super(symbol);
  }
}
