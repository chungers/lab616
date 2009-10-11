// 2009 lab616.com, All Rights Reserved.

package com.lab616.trading.ib;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Level;

import junit.framework.TestCase;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.internal.Lists;
import com.lab616.common.logging.Logging;
import com.lab616.ib.api.TWSClientModule;
import com.lab616.ib.api.TWSEvent;
import com.lab616.omnibus.event.EventEngine;
import com.lab616.omnibus.event.EventModule;
import com.lab616.omnibus.event.EventEngine.Subscriber;
import com.lab616.omnibus.event.EventEngineTest.SystemEventWatcher;

/**
 * Tests for splitting TWSEvents into different streams.
 *
 * @author david
 *
 */
public class TWSEventSplitterTest extends TestCase {

  protected EventEngine engine;

  static {
    Logging.init(Level.DEBUG);
  }
  
  @Override
  protected void setUp() throws Exception {
    List<Module> mods = Lists.newArrayList(
        EventModule.builder()
        .bindEventWatcher(SystemEventWatcher.class)
        .build());
    mods.add(new TWSClientModule());
    
    Injector injector = Guice.createInjector(mods);
    engine = injector.getInstance(EventEngine.class);
  }
  
  public void testCopyStream() throws Exception {

    final ConcurrentLinkedQueue<TWSEvent> prices = 
      new ConcurrentLinkedQueue<TWSEvent>();
    
    engine.splitEventStream(TWSEvent.class)
    .into("PriceTick")
    .where("method='tickPrice'")
    .then()
    .direct("select * from PriceTick")
    .to(new Subscriber<TWSEvent>() {
      public void update(TWSEvent event) {
        prices.add(event);
      }
    }).build();
  
    // Now we fire events
    int priceCount = 0;
    
    for (; priceCount < 10000; priceCount++) {
      engine.post(new TWSEvent().setMethod("tickPrice"));
      Thread.sleep(1L); // Won't need this if inbound/outbound thread pool =1
    }
    engine.stop();
    Thread.sleep(1000L);
    
    for (TWSEvent e : prices) {
      assertEquals("tickPrice", e.getMethod());
    }
    assertEquals(priceCount, prices.size());
  }


  public void testSplitStream() throws Exception {

    final ConcurrentLinkedQueue<TWSEvent> prices = 
      new ConcurrentLinkedQueue<TWSEvent>();
    final ConcurrentLinkedQueue<TWSEvent> sizes = 
      new ConcurrentLinkedQueue<TWSEvent>();
    
    engine.splitEventStream(TWSEvent.class)
    .into("TickData")
    .where("method='tickPrice' or method='tickSize'")
    .then()
    .direct("select * from TickData(method='tickPrice')")
    .to(new Subscriber<TWSEvent>() {
      public void update(TWSEvent event) {
        prices.add(event);
      }
    })
    .then()
    .direct("select * from TickData(method='tickSize')")
    .to(new Subscriber<TWSEvent>() {
      public void update(TWSEvent event) {
        sizes.add(event);
      }
    }).build();
  
    // Now we fire events
    Random rng = new Random();
    int priceCount = 0;
    int sizeCount = 0;
    int bogusCount = 0;
    
    for (int i = 0; i < 10000; i++) {
      int pick = rng.nextInt(3);
      switch (pick) {
        case 0:
          engine.post(new TWSEvent().setMethod("tickPrice"));
          priceCount++;
          break;
        case 1:
          engine.post(new TWSEvent().setMethod("tickSize"));
          sizeCount++;
          break;
        case 2:
          engine.post(new TWSEvent().setMethod("bogus"));
          bogusCount++;
          break;
      }
      Thread.sleep(1L); // HAVE TO ADD THIS??
    }
    engine.stop();
    Thread.sleep(1000L);
    
    for (TWSEvent e : prices) {
      assertEquals("tickPrice", e.getMethod());
    }
    for (TWSEvent e : sizes) {
      assertEquals("tickSize", e.getMethod());
    }
    assertEquals(priceCount, prices.size());
    assertEquals(sizeCount, sizes.size());
  }
}

