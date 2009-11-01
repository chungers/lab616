// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api;

import java.io.EOFException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.lab616.monitoring.Varz;
import com.lab616.monitoring.VarzMap;
import com.lab616.monitoring.Varzs;
import com.lab616.omnibus.event.EventEngine;
import com.lab616.omnibus.event.EventMessage;
import com.lab616.util.Time;

/**
 * API handler
 *
 * @author david
 */
public class TWSProxy implements InvocationHandler {

  @Varz(name = "tws-proxy-events")
  public static AtomicLong events = new AtomicLong(0L);
  
  @Varz(name = "tws-proxy-events-exceptions")
  public static AtomicLong exceptions = new AtomicLong(0L);

  @Varz(name = "tws-proxy-client-events")
  public static Map<String, AtomicLong> clientEvents = 
    VarzMap.create(AtomicLong.class);
  
  @Varz(name = "tws-proxy-client-methods")
  public static Map<String, AtomicLong> clientMethods = 
    VarzMap.create(AtomicLong.class);
  
  static {
    Varzs.export(TWSProxy.class);
  }

  static Logger logger = Logger.getLogger(TWSProxy.class);
  
  /**
   * Event from TWS proxy method invocations.
   */
  public static final class EWrapperMessage {
    public final String source;
    public final String method;
    public final Object[] args;
    public final long timestamp;
    
    public EWrapperMessage(String method, Object[] args, String source) {
      this.timestamp = Time.now();
      this.method = method;
      this.args = args;
      this.source = source;
    }
    
    public String toString() {
      StringBuffer buf = new StringBuffer(method);
      buf.append(",");
      buf.append(timestamp);
      buf.append(",");
      buf.append(source);
      for (Object o : args) {
        buf.append(",");
        buf.append(o);
      }
      return buf.toString();
    }
  }
  
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
        EWrapperMessage msg = new EWrapperMessage(
            m.getName(), args, parent.getAccountName());
        EventMessage<EWrapperMessage> event = new EventMessage<EWrapperMessage>(
            parent.getSourceId(), null, msg);
        
        if (synchronousIBEvents.contains(m.getName())) {
          // Call method directly.
          handleData(msg);
        } else {
          engine.post(event);
        }
        
        if (parent.getSourceId() != null) {
          clientEvents.get(parent.getSourceId()).incrementAndGet();
        }
        clientMethods.get(m.getName()).incrementAndGet();
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
  protected void handleData(EWrapperMessage event) {
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
