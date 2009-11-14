// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api.util;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.TestCase;

import com.google.inject.internal.Lists;
import com.ib.client.EWrapper;
import com.lab616.ib.api.ApiBuilder;
import com.lab616.ib.api.ApiMethods;
import com.lab616.ib.api.avro.TWSEvent;
import com.lab616.ib.api.avro.TWSField;
import com.lab616.util.Time;

/**
 *
 *
 * @author david
 *
 */
public class AvroDataFileTest extends TestCase {

  static int NUM_RECORDS = 1000;
  
  private EWrapper getEWrapper(final AtomicReference<TWSEvent> ref,
      final ApiBuilder builder) {
    return (EWrapper)Proxy.newProxyInstance(
         EWrapper.class.getClassLoader(), 
         new Class[] { EWrapper.class }, 
         new InvocationHandler() {
           public Object invoke(Object proxy, Method m, Object[] args) 
             throws Throwable {
             TWSEvent e = builder.buildAvro("test", Time.now(), args);
             ref.set(e);
             return null;
           }
         });
   }

  public void testWriting() throws Exception {
    AvroDataFile p = new AvroDataFile("/tmp", "test");
    File f = p.getFile();
    if (f.exists()) {
      f.delete();
    }
    
    final ApiBuilder builder = ApiMethods.TICK_PRICE;

    final AtomicReference<TWSEvent> eventRef = 
      new AtomicReference<TWSEvent>();
    
    EWrapper w = getEWrapper(eventRef, builder);
   
    // Create a bunch of TWSEvents.
    for (int i = 0; i < NUM_RECORDS; i++) {
      // Call the wrapper
      w.tickPrice(100, 1, 20.0 + i, 1);
      TWSEvent event = eventRef.get();
      // send to writer
      p.getWriter().write(event);
    }
    assertEquals(NUM_RECORDS, p.getWriter().countWritten());
    p.getWriter().close();
  }
  
  
  public void testReading() throws Exception {
    AvroDataFile p = new AvroDataFile("/tmp", "test");
   
    int count = 0;
    for (TWSEvent e : p.getReader().readAll()) {
      assertEquals("tickPrice", e.method.name());
      assertTrue(e.timestamp > 0);
      List<TWSField> fields = Lists.newArrayList(e.fields);
      assertEquals(new Integer(100), fields.get(0).intValue);
      assertEquals(new Integer(1), fields.get(1).intValue);
      assertEquals(new Double(20.0 + count), fields.get(2).doubleValue);
      assertEquals(new Integer(1), fields.get(3).intValue);
      count++;
    }
    p.getReader().close();
    
    assertEquals(NUM_RECORDS, count);
  }
}
