// 2009 lab616.com, All Rights Reserved.

package com.lab616.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

/**
 * Trivial callable that waits for something else to set its value.  This
 * is used in context of Future.
 *
 * @author david
 *
 */
public class FutureData<V, T> implements Callable<T>, Future<T>, FutureHolder<V> {

  static Logger logger = Logger.getLogger(FutureData.class);
  
  private V result;
  private CountDownLatch latch = new CountDownLatch(1);
  private Future<T> future;
  private Predicate<V> acceptor;
  private Function<V, T> transform;
  private long timeout;
  private TimeUnit unit;
  
  public FutureData(ExecutorService executor,
      Predicate<V> acceptor, Function<V, T> transform) {
    this(executor, 50L, TimeUnit.MILLISECONDS, acceptor, transform);
  }
  
  public FutureData(ExecutorService executor, long timeout, TimeUnit unit,
      Predicate<V> acceptor, Function<V, T> transform) {
    this.timeout = timeout;
    this.unit = unit;
    this.acceptor = acceptor;
    this.transform = transform;
    this.future = executor.submit(this);
  }
 
  /**
   * Another thread calls this method to set the data.  If accepted, the other
   * thread that's blocked waiting for the result will unblock and get the
   * transformed value.
   * @param value Some value from external source.
   * @return True if the value has been accepted.
   */
  public boolean accept(V value) {
    if (value == null) {
      return false;
    }
    if (acceptor.apply(value)) {
      result = value;
      latch.countDown();
      return true;
    }
    return false;
  }
  
  public boolean cancel(boolean b) {
    return future.cancel(b);
  }
  
  public boolean isDone() {
    return future.isDone();
  }

  public boolean isCancelled() {
    return future.isCancelled();
  }
  
  public T get() {
    return get(timeout, unit);
  }
 
  public T get(long timeout, TimeUnit unit) {
    try {
      return future.get(timeout, unit);
    } catch (Exception e) {
      logger.warn("Returning null because of ", e);
      return null;
    }
  }
  
  public T call() {
    try {
      latch.await(timeout, unit);
      return this.transform.apply(result);
    } catch (InterruptedException e) {
      logger.warn("Returning null because of ", e);
    }
    return null;
  }
}
