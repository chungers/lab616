// 2009 lab616.com, All Rights Reserved.

package com.lab616.monitoring;

/**
 *
 *
 * @author david
 *
 */
public class MinMaxAverage {

  private long min = 0;
  private long max = 0;
  private int count = 0;
  private long last = 0;
  private long sum = 0;
  
  public void set(long v) {
    min = Math.min(min, v);
    max = Math.max(max, v);
    count++;
    last = v;
    sum += v;
  }
  
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append(min);
    buf.append('/');
    buf.append(max);
    buf.append('/');
    buf.append((double) sum / (double) count);
    buf.append('/');
    buf.append(last);
    return buf.toString();
  }
}
