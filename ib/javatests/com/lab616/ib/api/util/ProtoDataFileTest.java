// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api.util;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.TestCase;

import com.ib.client.EWrapper;
import com.lab616.ib.api.ApiBuilder;
import com.lab616.ib.api.ApiMethods;
import com.lab616.ib.api.proto.TWSProto;
import com.lab616.util.Time;

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
             TWSProto.Event e = builder.buildProto("test", Time.now(), args);
             ref.set(e);
             assertTrue(e.isInitialized());
             return null;
           }
         });
   }

  public void testWriting() throws Exception {
    ProtoDataFile p = new ProtoDataFile("/tmp", "test");
    File f = p.getFile();
    if (f.exists()) {
      f.delete();
    }
    
    final ApiBuilder builder = ApiMethods.TICK_PRICE;

    final AtomicReference<TWSProto.Event> eventRef = 
      new AtomicReference<TWSProto.Event>();
    
    EWrapper w = getEWrapper(eventRef, builder);
   
    // Create a bunch of TWSEvents.
    for (int i = 0; i < NUM_RECORDS; i++) {
      // Call the wrapper
      w.tickPrice(100, 1, 20.0 + i, 1);
      TWSProto.Event proto = eventRef.get();
      // send to writer
      p.getWriter().write(proto);
    }
    assertEquals(NUM_RECORDS, p.getWriter().countWritten());
    p.getWriter().close();
  }
  
  
  public void testReading() throws Exception {
    ProtoDataFile p = new ProtoDataFile("/tmp", "test");
   
    int count = 0;
    for (TWSProto.Event e : p.getReader().readAll()) {
      assertEquals("tickPrice", e.getMethod().name());
      assertEquals(100, e.getFields(0).getIntValue());
      assertTrue(e.getTimestamp() > 0);
      count++;
    }
    p.getReader().close();
    
    assertEquals(NUM_RECORDS, count);
  }
  
  
  private int writeBlock(ProtoDataFile p, int records) throws Exception {
    final ApiBuilder builder = ApiMethods.TICK_PRICE;
    final AtomicReference<TWSProto.Event> eventRef = 
      new AtomicReference<TWSProto.Event>();
    
    EWrapper w = getEWrapper(eventRef, builder);
   
    // Create a bunch of TWSEvents.
    int count = 0;
    for (int i = 0; i < records; i++) {
      // Call the wrapper
      w.tickPrice(100, 1, 20.0 + i, 1);
      TWSProto.Event proto = eventRef.get();
      // send to writer
      p.getWriter().write(proto);
      count++;
    }
    p.getWriter().close();
    return count;
  }
  
  public void testAppending() throws Exception {
    // Now read the entire file.
    ProtoDataFile w = new ProtoDataFile("/tmp", "test");
    File f = w.getFile();
    if (f.exists()) {
      f.delete();
    }
    
    int total = 0;
    for (int i = 0; i < 5; i++) {
      total += writeBlock(w, 100);
    }
    
    ProtoDataFile p = new ProtoDataFile("/tmp", "test");
    int count = 0;
    for (TWSProto.Event e : p.getReader().readAll()) {
      assertEquals(TWSProto.Method.tickPrice, e.getMethod());
      assertTrue(e.getTimestamp() > 0);
      assertTrue(e.isInitialized());
      count++;
    }
    p.getReader().close();

    assertEquals(total, count);
  }

  
  
}
