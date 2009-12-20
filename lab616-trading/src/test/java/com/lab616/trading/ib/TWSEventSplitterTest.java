// 2009 lab616.com, All Rights Reserved.

package com.lab616.trading.ib;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import junit.framework.TestCase;

import org.apache.log4j.Level;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.internal.Lists;
import com.ib.client.EWrapper;
import com.lab616.common.logging.Logging;
import com.lab616.ib.api.ApiBuilder;
import com.lab616.ib.api.ApiMethods;
import com.lab616.ib.api.TWSClientModule;
import com.lab616.ib.api.proto.TWSProto;
import com.lab616.omnibus.event.EventEngine;
import com.lab616.omnibus.event.EventMessage;
import com.lab616.omnibus.event.EventModule;
import com.lab616.omnibus.event.EventEngine.Subscriber;
import com.lab616.omnibus.event.watchers.SystemEventWatcher;
import com.lab616.util.Time;

/**
 * Tests for splitting TWSProto.Events into different streams.
 *
 * @author david
 *
 */
public class TWSEventSplitterTest extends TestCase {

  protected EventEngine engine;

  static {
    Logging.init(Level.DEBUG);
  }
  
  private EWrapper getEWrapper(final ApiBuilder builder) {
    return (EWrapper)Proxy.newProxyInstance(
         EWrapper.class.getClassLoader(), 
         new Class[] { EWrapper.class }, 
         new InvocationHandler() {
           public Object invoke(Object proxy, Method m, Object[] args) 
             throws Throwable {
             TWSProto.Event e = builder.buildProto("test", Time.now(), args);
             
             assertTrue(e.isInitialized());
             engine.post(e);
             
             EventMessage event = new EventMessage(
                 "test0", null, "test-account", m.getName(), args, Time.now());
             engine.post(event);
             
             return null;
           }
         });
   }

  @Override
  protected void setUp() throws Exception {
  	EventModule.QUEUE_CAPACITY = 1;
    List<Module> mods = Lists.newArrayList(
        EventModule.builder()
        .bindEventWatcher(SystemEventWatcher.class)
        .build());
    mods.add(new TWSClientModule());
    
    Injector injector = Guice.createInjector(mods);
    engine = injector.getInstance(EventEngine.class);
  }
  
  public void testCopyStream() throws Exception {

    final ConcurrentLinkedQueue<TWSProto.Event> prices = 
      new ConcurrentLinkedQueue<TWSProto.Event>();
    
    engine.splitEventStream(TWSProto.Event.class)
    .into("PriceTick")
    .where("method=Method.tickPrice")
    .then()
    .direct("select * from PriceTick")
    .to(new Subscriber<TWSProto.Event>() {
      public void update(TWSProto.Event event) {
        prices.add(event);
      }
    }).build();
  
    // Now we fire events
    int priceCount = 0;
    EWrapper w = getEWrapper(ApiMethods.TICK_PRICE);

    for (; priceCount < 100; priceCount++) {
      // Call the wrapper
      w.tickPrice(1001, 0, 26.0, 1);
    }
    Thread.sleep(1L); // HAVE TO ADD THIS??
    
    for (TWSProto.Event e : prices) {
      assertEquals(TWSProto.Method.tickPrice, e.getMethod());
    }
    assertEquals(priceCount, prices.size());
  }


  public void testSplitStream() throws Exception {

    final ConcurrentLinkedQueue<TWSProto.Event> prices = 
      new ConcurrentLinkedQueue<TWSProto.Event>();
    final ConcurrentLinkedQueue<TWSProto.Event> sizes = 
      new ConcurrentLinkedQueue<TWSProto.Event>();
    
    engine.splitEventStream(TWSProto.Event.class)
    .into("TickData")
    .where("method=Method.tickPrice or method=Method.tickSize")
    .then()
    .direct("select * from TickData(method=Method.tickPrice)")
    .to(new Subscriber<TWSProto.Event>() {
      public void update(TWSProto.Event event) {
        prices.add(event);
      }
    })
    .then()
    .direct("select * from TickData(method=Method.tickSize)")
    .to(new Subscriber<TWSProto.Event>() {
      public void update(TWSProto.Event event) {
        sizes.add(event);
      }
    }).build();
  
    // Now we fire events
    Random rng = new Random();
    int priceCount = 0;
    int sizeCount = 0;
    int bogusCount = 0;

    for (int i = 0; i < 100; i++) {
      int pick = rng.nextInt(3);
      switch (pick) {
        case 0:
        	getEWrapper(ApiMethods.TICK_PRICE).tickPrice(
        			1001, 3, 27.5, 1);
          priceCount++;
          break;
        case 1:
        	getEWrapper(ApiMethods.TICK_SIZE).tickSize(1001, 3, 100);
          sizeCount++;
          break;
        case 2:
        	getEWrapper(ApiMethods.TICK_GENERIC).tickGeneric(1001, 2, 45.);
          bogusCount++;
          break;
      }
      Thread.sleep(1L); // HAVE TO ADD THIS??
    }
    
    for (TWSProto.Event e : prices) {
      assertEquals(TWSProto.Method.tickPrice, e.getMethod());
    }
    for (TWSProto.Event e : sizes) {
      assertEquals(TWSProto.Method.tickSize, e.getMethod());
    }
    assertEquals(priceCount, prices.size());
    assertEquals(sizeCount, sizes.size());
  }


  public void testSplitStream2() throws Exception {

    final ConcurrentLinkedQueue<EventMessage> prices = 
      new ConcurrentLinkedQueue<EventMessage>();
    final ConcurrentLinkedQueue<EventMessage> sizes = 
      new ConcurrentLinkedQueue<EventMessage>();
    
    engine.splitEventStream(EventMessage.class)
    .into("TickData")
    .where("method='tickPrice' or method='tickSize'")
    .then()
    .direct("select * from TickData(method='tickPrice')")
    .to(new Subscriber<EventMessage>() {
      public void update(EventMessage event) {
        prices.add(event);
      }
    })
    .then()
    .direct("select * from TickData(method='tickSize')")
    .to(new Subscriber<EventMessage>() {
      public void update(EventMessage event) {
        sizes.add(event);
      }
    }).build();
  
    // Now we fire events
    Random rng = new Random();
    int priceCount = 0;
    int sizeCount = 0;
    int bogusCount = 0;

    for (int i = 0; i < 1000; i++) {
      int pick = rng.nextInt(3);
      switch (pick) {
        case 0:
        	getEWrapper(ApiMethods.TICK_PRICE).tickPrice(
        			1001, 3, 27.5, 1);
          priceCount++;
          break;
        case 1:
        	getEWrapper(ApiMethods.TICK_SIZE).tickSize(1001, 3, 100);
          sizeCount++;
          break;
        case 2:
        	getEWrapper(ApiMethods.TICK_GENERIC).tickGeneric(1001, 2, 45.);
          bogusCount++;
          break;
      }
      Thread.sleep(1L); // HAVE TO ADD THIS??
    }
    
    for (EventMessage e : prices) {
      assertEquals("tickPrice", e.method);
    }
    for (EventMessage e : sizes) {
      assertEquals("tickSize", e.method);
    }
    assertEquals(priceCount, prices.size());
    assertEquals(sizeCount, sizes.size());
  }
}

