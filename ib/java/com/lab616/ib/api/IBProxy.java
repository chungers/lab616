// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.lab616.monitoring.Varz;
import com.lab616.monitoring.Varzs;
import com.lab616.omnibus.event.EventEngine;

/**
 * API handler
 *
 * @author david
 */
public final class IBProxy implements InvocationHandler {

  @Varz(name = "ib-api-events")
  public static AtomicLong events = new AtomicLong(0L);
  
  @Varz(name = "ib-api-events-exceptions")
  public static AtomicLong exceptions = new AtomicLong(0L);

  static {
    Varzs.export(IBProxy.class);
  }

  static Logger logger = Logger.getLogger(IBProxy.class);
  
  
  private final EventEngine engine;
  
  @Inject
  public IBProxy(EventEngine engine) {
    this.engine = engine;
  }
  
  public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
    events.incrementAndGet();
    try {
      IBEvent event = new IBEvent(m.getName(), args);
      engine.post(event);
      return null;
    } catch (Throwable t) {
      exceptions.incrementAndGet();
      logger.error("Exception from " + m, t);
      throw t;
    }
  }
}
