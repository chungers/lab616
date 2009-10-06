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
public final class IBEvent implements Comparable<IBEvent> {

  public static final String EVENT_NAME = "IBEvent";
  
  private String source;
  private String method;
  private long timestamp;
  private Object[] args;

  public IBEvent() {
    this.timestamp = Time.now();
  }
  
  public IBEvent(String method, Object[] args) {
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

  public void setSource(String s) {
    this.source = s;
  }
  
  public String getSource() {
    return this.source; 
  }
  
  public void setMethod(String m) {
    this.method = m;
  }
  
  public void setArgs(Object[] args) {
    this.args = args;
  }

  public int compareTo(IBEvent o) {
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
}
