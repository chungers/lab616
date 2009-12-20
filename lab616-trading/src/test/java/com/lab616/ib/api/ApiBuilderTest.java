// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.TestCase;

import com.ib.client.EWrapper;
import com.lab616.common.Pair;
import com.lab616.ib.api.proto.TWSProto;
import com.lab616.util.Time;

/**
 * @author david
 *
 */
public class ApiBuilderTest extends TestCase {

  private EWrapper getEWrapper(final AtomicReference<TWSProto.Event> ref,
      final ApiBuilder builder) {
    return (EWrapper)Proxy.newProxyInstance(
         EWrapper.class.getClassLoader(), 
         new Class[] { EWrapper.class }, 
         new InvocationHandler() {
           public Object invoke(Object proxy, Method m, Object[] args) 
             throws Throwable {
             TWSProto.Event e = builder.buildProto("test", Time.now(), args);
             ref.set(e);
             assertTrue(e.isInitialized());
             return null;
           }
         });
   }

  private void verify(ApiBuilder b, TWSProto.Event event, Object... args)
    throws Exception {
    assertEquals(event.getFieldCount(), args.length);
    assertTrue(event.hasTimestamp());
    assertTrue(event.hasMethod());
    assertEquals(b.getMethodName(), event.getMethod().toString());
    int i = 0;
    for (Object v : args) {
      TWSProto.Field f = event.getField(i++);
      if (v instanceof Integer) {
        assertTrue(f.hasIntValue());
        assertEquals(v, f.getIntValue());
      }
      if (v instanceof Double) {
        assertTrue(f.hasDoubleValue());
        assertEquals(v, f.getDoubleValue());
      }
      if (v instanceof String) {
        assertTrue(f.hasStringValue());
        assertEquals(v, f.getStringValue());
      }
      if (v instanceof Long) {
        assertTrue(f.hasLongValue());
        assertEquals(v, f.getLongValue());
      }
    }
    
    Pair<Method, Object[]> p = b.buildArgs(event);
    assertEquals(args.length, p.second.length);
    for (i = 0; i < args.length; i++) {
      assertEquals(args[i], p.second[i]);
    }
  }
  
  public void testTickSize() throws Exception {
    final ApiBuilder builder = ApiMethods.TICK_SIZE;

    final AtomicReference<TWSProto.Event> eventRef = 
      new AtomicReference<TWSProto.Event>();
    
    EWrapper w = getEWrapper(eventRef, builder);

    // Call the wrapper
    w.tickSize(100, 0, 1000);
   
    verify(builder, eventRef.get(), 100, 0, 1000);
  }


  public void testTickPrice() throws Exception {
    final ApiBuilder builder = ApiMethods.TICK_PRICE;

    final AtomicReference<TWSProto.Event> eventRef = 
      new AtomicReference<TWSProto.Event>();
    
    EWrapper w = getEWrapper(eventRef, builder);

    // Call the wrapper
    w.tickPrice(100, 2, 20.0, 1);
   
    verify(builder, eventRef.get(), 100, 2, 20.0, 1);
  }
  
  
  public void testTickGeneric() throws Exception {
    final ApiBuilder builder = ApiMethods.TICK_GENERIC;

    final AtomicReference<TWSProto.Event> eventRef = 
      new AtomicReference<TWSProto.Event>();
    
    EWrapper w = getEWrapper(eventRef, builder);

    // Call the wrapper
    w.tickGeneric(100, 1, 27.);
   
    verify(builder, eventRef.get(), 100, 1, 27.);
  }
  
  public void testTickString() throws Exception {
    final ApiBuilder builder = ApiMethods.TICK_STRING;

    final AtomicReference<TWSProto.Event> eventRef = 
      new AtomicReference<TWSProto.Event>();
    
    EWrapper w = getEWrapper(eventRef, builder);

    // Call the wrapper
    w.tickString(100, 2, "test");
   
    verify(builder, eventRef.get(), 100, 2, "test");
  }


  public void testMktDepth() throws Exception {
    final ApiBuilder builder = ApiMethods.MKT_DEPTH;

    final AtomicReference<TWSProto.Event> eventRef = 
      new AtomicReference<TWSProto.Event>();
    
    EWrapper w = getEWrapper(eventRef, builder);

    // Call the wrapper
    w.updateMktDepth(1, 2, 3, 4, 5., 6);
   
    verify(builder, eventRef.get(), 1, 2, 3, 4, 5., 6);
  }

  public void testRealtimeBar() throws Exception {
    final ApiBuilder builder = ApiMethods.REALTIME_BAR;

    final AtomicReference<TWSProto.Event> eventRef = 
      new AtomicReference<TWSProto.Event>();
    
    EWrapper w = getEWrapper(eventRef, builder);

    // Call the wrapper
    w.realtimeBar(1, 2L, 3., 4., 5., 6., 7L, 8., 9);
   
    verify(builder, eventRef.get(), 1, 2L, 3., 4., 5., 6., 7L, 8., 9);
  }


  public void testUpdateAccountValue() throws Exception {
    final ApiBuilder builder = ApiMethods.UPDATE_ACCT_VALUE;

    final AtomicReference<TWSProto.Event> eventRef = 
      new AtomicReference<TWSProto.Event>();
    
    EWrapper w = getEWrapper(eventRef, builder);

    // Call the wrapper
    w.updateAccountValue("foo", "bar", "curr", "acct");
   
    verify(builder, eventRef.get(), "foo", "bar", "curr", "acct");
  }


  public void testHistoricalData() throws Exception {
    final ApiBuilder builder = ApiMethods.HISTORICAL_DATA;

    final AtomicReference<TWSProto.Event> eventRef = 
      new AtomicReference<TWSProto.Event>();
    
    EWrapper w = getEWrapper(eventRef, builder);

    // Call the wrapper
    w.historicalData(1, "2009-10-19", 25., 34., 20., 27., 100000, 50, 28., true);
    verify(builder, eventRef.get(), 
        1, "2009-10-19", 25., 34., 20., 27., 100000, 50, 28., true);
  }
}
