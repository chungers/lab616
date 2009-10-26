// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.lab616.concurrent.FutureData;
import com.lab616.concurrent.FutureHolder;
import com.lab616.concurrent.FutureIterable;
import com.lab616.ib.api.TWSProxy.EWrapperMessage;

/**
 * TWS API is asynchronous and this class provides the infrastructure to expose
 * these async calls as if they are synchronous/ blocking to the client by using
 * futures and queues so that the caller thread will block until results are 
 * available from the TWS EReader thread.
 *
 * @author david
 *
 */
public class TWSBlockingCallManager {

  static Logger logger = Logger.getLogger(TWSBlockingCallManager.class);
  
  private ExecutorService executor;
  
  // Result of future data is keyed by the thread requesting it.  It's ok
  // since the whole point is that the thread will block and will remove itself
  // from the map once the result is returned.  Even if it doesn't, the next
  // the same thread will simply put a new future when making a subsequent 
  // request.  Also, the map is bounded and easy to search.
  private Map<Thread, FutureHolder<EWrapperMessage>> futureData = Maps.newHashMap();

  // List of method names that are to be synchronous.
  private Set<String> synchronousMethods;
  
  private final long timeout;
  private final TimeUnit unit;
  
  public TWSBlockingCallManager(ExecutorService executor,
      long timeout, TimeUnit unit,
      String method, String... methods) {
    this.executor = executor;
    this.timeout = timeout;
    this.unit = unit;
    synchronousMethods = Sets.newHashSet(methods);
    synchronousMethods.add(method);
  }
  
  public Set<String> getSynchronousMethods() {
    return synchronousMethods;
  }

  /**
   * Invoked by the handleData method of the api EWrapper proxy. This
   * puts the result in the map by letting the FutureData elements accept the
   * right data based on some information about the IBEvent, such as by 
   * matching the method name.
   * @param event The event.
   */
  public void handleData(EWrapperMessage event) {
    for (FutureHolder<EWrapperMessage> future : futureData.values()) {
      if (future.accept(event)) {
        return;
      }
    }
  }

  /**
   * Executes a blocking call for the current calling thread.
   * @param <V> The return type.
   * @param method The method name.
   * @param trans The mapping function from IBEvent to the return type.
   * @param call Executes the call.
   * @return The value.
   */
  public <V> V blockingCall(String method, 
      Function<EWrapperMessage, V> trans, Runnable call) {
    return blockingCall(method, this.timeout, this.unit, trans, call);
  }
  
  /**
   * Executes a blocking call for the current calling thread.
   * @param <V> The return type.
   * @param method The method name.
   * @param timeout The timeout.
   * @param unit The timeout unit.
   * @param trans The mapping function from IBEvent to the return type.
   * @param call Executes the call.
   * @return The value.
   */
  public <V> V blockingCall(final String method, long timeout, TimeUnit unit,
      Function<EWrapperMessage, V> trans, Runnable call, 
      final Predicate<EWrapperMessage>... predicates) {
    if (!this.synchronousMethods.contains(method)) {
      // Not registered, and we will never get any response!
      throw new MisconfigurationException(
          "Not configured to read data: " + method);
    }
    return blockingCall(timeout, unit,
        new Predicate<EWrapperMessage>() {
      public boolean apply(EWrapperMessage event) {
        boolean matched = method.equals(event.method);
        if (matched) {
          for (Predicate<EWrapperMessage> p : predicates) {
            if (p.apply(event)) {
              return true;
            }
          }
        }
        return false;
      }
    }, trans, call);
  }

  /**
   * Most general form of executing a blocking call given a matcher for the
   * result, a transform that maps IBEvent to return value and the calling
   * closure.
   * @param <V> The return type.
   * @param timeout The timeout.
   * @param unit The timeout unit.
   * @param predicate The matcher for the result from the EReader thread.
   * @param trans The transform.
   * @param call The closure that invokes the client api to start the request.
   * @return The value.
   */
  public <V> V blockingCall(long timeout, TimeUnit unit,
      Predicate<EWrapperMessage> predicate,
      Function<EWrapperMessage, V> trans, Runnable call) {
    // Create a future data.
    FutureData<EWrapperMessage, V> f = new FutureData<EWrapperMessage, V>(
        this.executor,
        timeout, unit, predicate, trans);
    this.futureData.put(Thread.currentThread(), f);
    // Now make the request since we are ready to listen for results.
    call.run();
    // Now we wait.
    try {
      return f.get(timeout, unit);
    } finally {
      this.futureData.remove(Thread.currentThread());
    }
  }

  /**
   * Invoke a blocking call where the return Iterable can block pending data
   * availability.  This is somewhat similar to the generator pattern.
   * @param <V> The return type.
   * @param timeout The timeout.
   * @param unit The unit.
   * @param predicate Acceptor, data matcher.
   * @param trans The transform.
   * @param call Call to start the request.
   * @return The iterable.
   */
  public <V> Iterable<V> blockingIterable(long timeout, TimeUnit unit,
      Predicate<EWrapperMessage> predicate,
      Function<EWrapperMessage, V> trans, Runnable call) {
    // Create a future data.
    FutureIterable<EWrapperMessage, V> f = new FutureIterable<EWrapperMessage, V>(
        timeout, unit, predicate, trans);
    this.futureData.put(Thread.currentThread(), f);
    f.invokeOnNoData(new Callable<Boolean>() {
      public Boolean call() {
        futureData.remove(Thread.currentThread());
        return false;
      }
    });
    // Now make the request since we are ready to listen for results.
    call.run();
    return f;
  }

  /**
   * Exception thrown when the method is never configured to pass data
   * from the proxy.
   */
  public static class MisconfigurationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public MisconfigurationException(String message) {
      super(message);
    }
  }
}
