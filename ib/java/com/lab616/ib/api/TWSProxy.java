// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api;

import java.io.EOFException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.lab616.ib.api.proto.TWSProto;
import com.lab616.monitoring.Varz;
import com.lab616.monitoring.Varzs;
import com.lab616.omnibus.event.EventEngine;
import com.lab616.util.Time;

/**
 * API handler
 *
 * @author david
 */
public class TWSProxy implements InvocationHandler {

  @Varz(name = "ib-api-events")
  public static AtomicLong events = new AtomicLong(0L);
  
  @Varz(name = "ib-api-events-exceptions")
  public static AtomicLong exceptions = new AtomicLong(0L);

  static {
    Varzs.export(TWSProxy.class);
  }

  static Logger logger = Logger.getLogger(TWSProxy.class);
  
  
  private final EventEngine engine;
  private TWSClient parent = null;
  private final Set<String> synchronousIBEvents;
  
  public TWSProxy(TWSClient parent, EventEngine engine) {
    this.parent = parent;
    this.engine = engine;
    synchronousIBEvents = Sets.newHashSet(synchronousMethods());
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
        ApiBuilder b = ApiMethods.get(m.getName());
        TWSProto.Event event = null; 
        if (b != null) {
          event = b.buildProto(parent.getSourceId(), Time.now(), args);
          if (synchronousIBEvents.contains(m.getName())) {
            // Call method directly.
            handleData(event);
          } else {
            engine.post(event);
          }
        }
      }
      return null;
    } catch (Throwable t) {
      exceptions.incrementAndGet();
      logger.error("Exception from " + m, t);
      throw t;
    }
  }
  
  /**
   * Override this to receive data directly instead of via the IBEvent stream.
   * @param event The event.
   */
  protected void handleData(TWSProto.Event event) {
    // Do nothing.
  }
  
  /**
   * Returns a list of method names where we don't fire IBEvents but instead
   * call the handleData method directly.
   * @return Set of filters.
   */
  protected Set<String> synchronousMethods() {
    return Sets.newHashSet();
  }
  
  protected void handleConnectionClosed() {
    logger.info("Connection closed.");
  }
  
  protected void handleError(Object[] args) {
    List<Object> list = Lists.newArrayList(args);
    logger.error("Error: " + list);
  }
}
