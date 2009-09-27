// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api;

import java.io.EOFException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.internal.Lists;
import com.lab616.monitoring.Varz;
import com.lab616.monitoring.Varzs;
import com.lab616.omnibus.event.EventEngine;

/**
 * API handler
 *
 * @author david
 */
public class IBProxy implements InvocationHandler {

  @Varz(name = "ib-api-events")
  public static AtomicLong events = new AtomicLong(0L);
  
  @Varz(name = "ib-api-events-exceptions")
  public static AtomicLong exceptions = new AtomicLong(0L);

  static {
    Varzs.export(IBProxy.class);
  }

  static Logger logger = Logger.getLogger(IBProxy.class);
  
  
  private final EventEngine engine;
  private final String clientSourceId;
  
  public IBProxy(String clientSourceId, EventEngine engine) {
    this.clientSourceId = clientSourceId;
    this.engine = engine;
  }
  
  public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
    events.incrementAndGet();
    try {
      if ("connectionClosed".equals(m.getName())) {
        handleConnectionClosed();
      } else if ("error".equals(m.getName())) {
        handleError(args);
        // Special case when server goes down.
        if (args.length == 1 && 
            EOFException.class.isAssignableFrom(args[0].getClass())) {
          handleConnectionClosed();
        }
      } else {
        IBEvent event = new IBEvent(m.getName(), args);
        event.setSource(clientSourceId);
        engine.post(event);
      }
      return null;
    } catch (Throwable t) {
      exceptions.incrementAndGet();
      logger.error("Exception from " + m, t);
      throw t;
    }
  }
  
  protected void handleConnectionClosed() {
    logger.info("Connection closed.");
  }
  
  protected void handleError(Object[] args) {
    List<Object> list = Lists.newArrayList(args);
    logger.error("Error: " + list);
  }
}
