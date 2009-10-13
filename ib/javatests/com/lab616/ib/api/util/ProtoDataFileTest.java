// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.base.Function;
import com.ib.client.EWrapper;
import com.lab616.common.Pair;
import com.lab616.ib.api.ApiBuilder;
import com.lab616.ib.api.ApiMethods;
import com.lab616.ib.api.TWSEvent;
import com.lab616.ib.api.proto.TWSProto;
import com.lab616.util.Time;

import junit.framework.TestCase;

/**
 *
 *
 * @author david
 *
 */
public class ProtoDataFileTest extends TestCase {

  static int NUM_RECORDS = 1000;
  
  private EWrapper getEWrapper(final AtomicReference<TWSProto.Event> ref,
      final ApiBuilder builder) {
    return (EWrapper)Proxy.newProxyInstance(
         EWrapper.class.getClassLoader(), 
         new Class[] { EWrapper.class }, 
         new InvocationHandler() {
           public Object invoke(Object proxy, Method m, Object[] args) 
             throws Throwable {
             TWSProto.Event e = builder.buildProto(Time.now(), args);
             ref.set(e);
             assertTrue(e.isInitialized());
             return null;
           }
         });
   }

  public void testWriting() throws Exception {
    ProtoDataFile p = new ProtoDataFile("/tmp", "test");
    
    final ApiBuilder builder = ApiMethods.TICK_PRICE;

    final AtomicReference<TWSProto.Event> eventRef = 
      new AtomicReference<TWSProto.Event>();
    
    EWrapper w = getEWrapper(eventRef, builder);
   
    // Create a bunch of TWSEvents.
    for (int i = 0; i < NUM_RECORDS; i++) {
      // Call the wrapper
      w.tickPrice(100, 1, 20.0 + i, 1);
      TWSProto.Event proto = eventRef.get();
      
      Pair<Method, Object[]> methodArgs = builder.buildArgs(proto);
      TWSEvent e = new TWSEvent();
      e.setMethod(methodArgs.first.getName()).setArgs(methodArgs.second);
      
      // send to writer
      p.getWriter().write(e);
    }
    p.getWriter().close();
    
    assertEquals(NUM_RECORDS, p.getWriter().countWritten());
  }
  
  
  public void testReading() throws Exception {
    ProtoDataFile p = new ProtoDataFile("/tmp", "test");
    final ApiBuilder builder = ApiMethods.TICK_PRICE;

    Function<TWSProto.Event, TWSEvent> trans = 
      new Function<TWSProto.Event, TWSEvent> () {
      public TWSEvent apply(TWSProto.Event e) {
        TWSEvent event = new TWSEvent();
        try {
          Pair<Method, Object[]> callArgs = builder.buildArgs(e);
          return event
            .setMethod(callArgs.first.getName())
            .setArgs(callArgs.second);
        } catch (NoSuchMethodException ex) {
          throw new RuntimeException(ex);
        }
      }
    };
   
    int count = 0;
    for (TWSEvent e : p.getReader().readAll(trans)) {
      assertEquals("tickPrice", e.getMethod());
      assertEquals(100, e.getArgs()[0]);
      assertTrue(e.getTimestamp() > 0);
      count++;
    }
    p.getReader().close();
    
    assertEquals(NUM_RECORDS, count);
  }
}
