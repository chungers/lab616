// 2009 lab616.com, All Rights Reserved.

package com.lab616.concurrent;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

/**
 * Iterable future.
 *
 * @author david
 *
 */
public class FutureIterable<V, T> implements Iterable<T>, FutureHolder<V> {

  static Logger logger = Logger.getLogger(FutureIterable.class);
  
  private BlockingQueue<T> values;
  private Predicate<V> acceptor;
  private Function<V, T> transform;
  private long timeout;
  private TimeUnit unit;
  private Callable<Boolean> noDataListener;
  
  public FutureIterable(Predicate<V> acceptor, Function<V, T> transform) {
    this(50L, TimeUnit.MILLISECONDS, acceptor, transform);
  }
  
  public FutureIterable(long timeout, TimeUnit unit,
      Predicate<V> acceptor, Function<V, T> transform) {
    this.values = new LinkedBlockingQueue<T>();
    this.timeout = timeout;
    this.unit = unit;
    this.acceptor = acceptor;
    this.transform = transform;
  }
 
  public void invokeOnNoData(Callable<Boolean> stop) {
    noDataListener = stop;
  }
  
  public Iterator<T> iterator() {
    return new Iterator<T>() {

      private T nextValue = null;

      public boolean hasNext() {
        try {
          nextValue = values.poll(timeout, unit);
          return nextValue != null;
        } catch (InterruptedException e) {
          if (noDataListener != null) {
            try {
              // The listener can chose to continue by returning true. 
              // For example, it can chose to try a few times before finally
              // agreeing to stop.
              if (noDataListener.call()) {
                return hasNext(); // Try again.
              } else {
                return false;
              }
            } catch (Exception ex) {
              return false;
            }
          }
          return false;
        }
      }

      public T next() {
        return nextValue;
      }

      public void remove() {
        // Nothing.
      }
    };
  }

  /**
   * Another thread calls this method to set the data.  If accepted, the other
   * thread that's blocked waiting for the result will unblock and get the
   * transformed value.
   * @param value Some value from external source.
   * @return True if the value has been accepted.
   */
  public boolean accept(V value) {
    if (acceptor.apply(value)) {
      values.add(this.transform.apply(value));
      return true;
    }
    return false;
  }
}
