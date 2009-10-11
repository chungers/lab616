// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api;

import com.lab616.util.Time;

/**
 * Event corresponding to an IB TWS API method call.  This is simply the carrier
 * for the data from IB API calls (such as 'tickPrice' or 'tickSize').  Actual
 * tick data will be created by the event engine.
 *
 * @author david
 *
 */
public final class TWSEvent implements Comparable<TWSEvent> {

  public static final String EVENT_NAME = "TWSEvent";
  
  private String source;
  private String method;
  private long timestamp;
  private Object[] args;

  public TWSEvent() {
    this.timestamp = Time.now();
  }
  
  public TWSEvent(String method, Object[] args) {
    this.timestamp = Time.now();
    this.method = method;
    this.args = args;
  }

  public String getMethod() {
    return method;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public Object[] getArgs() {
    return args;
  }

  public TWSEvent setSource(String s) {
    this.source = s;
    return this;
  }
  
  public String getSource() {
    return this.source; 
  }
  
  public TWSEvent setMethod(String m) {
    this.method = m;
    return this;
  }
  
  public TWSEvent setArgs(Object[] args) {
    this.args = args;
    return this;
  }

  public int compareTo(TWSEvent o) {
    return (int) (this.getTimestamp() - o.getTimestamp());
  }
  
  public String toString() {
    StringBuffer line = new StringBuffer();
    line.append(String.format("%d,%s", getTimestamp(), getMethod()));
    Object[] args = getArgs();
    if (args != null && args.length > 0) {
      StringBuffer str = new StringBuffer(args[0].toString());
      for (int i = 1; i < args.length; i++) {
        str.append(",");
        str.append(args[i]);
      }
      line.append("," + str.toString());
    }
    return line.toString();
  }
  
  public void copyFrom(String s) {
    String[] cols = s.split(",");
    this.timestamp = Long.decode(cols[0]);
    this.method = cols[1];
    this.args = new Object[cols.length - 2];
    for (int i = 2, j = 0; i < cols.length; i++, j++) {
      try {
        args[j] = Integer.parseInt(cols[i]);
      } catch (NumberFormatException e1) {
        try {
          args[j] = Double.parseDouble(cols[i]);
        } catch (NumberFormatException e2) {
          try {
            args[j] = Boolean.valueOf(cols[i]);
          } catch (NumberFormatException e) {
            args[j] = cols[i]; // Finally a string.
          }
        }
      }
    }
  }
}
