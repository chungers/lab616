// 2009 lab616.com, All Rights Reserved.

package com.lab616.concurrent;

/**
 * Holder of future result from another thread.
 *
 * @author david
 *
 */
public interface FutureHolder<V> {

  public boolean accept(V value);
  
}
