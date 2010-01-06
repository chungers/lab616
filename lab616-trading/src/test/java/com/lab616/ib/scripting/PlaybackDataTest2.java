/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lab616.ib.scripting;

import com.ib.client.EWrapper;
import com.lab616.common.scripting.ScriptObjects;
import com.lab616.ib.api.TWSClientModule;
import com.lab616.ib.api.servlets.TWSControllerModule;
import com.lab616.omnibus.Kernel;
import com.lab616.omnibus.event.EventEngine;
import com.lab616.omnibus.event.EventMessage;
import com.lab616.omnibus.event.EventSubscriber;
import java.util.concurrent.atomic.AtomicInteger;
import junit.framework.TestCase;

/**
 *
 * @author dchung
 */
public class PlaybackDataTest2 extends TestCase {

  private static Kernel k;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    if (k == null) {
      k = new Kernel(null,
        new TWSClientModule(),
        new TWSControllerModule(),
        new ScriptingModule()).runInThread(new String[]{});
    }
  }

  public void testPlayback() throws Exception {
    final AtomicInteger count1 = new AtomicInteger(0);

    // Select only events with tickId = 1000.
    k.getInstance(EventEngine.class, 1000L).add(
      "select * from EventMessage e where e.args[0]?=?",
      new Object[] { new Integer(1000) },
      new EventSubscriber<EventMessage>() {

      @Override
      public void update(EventMessage t) {
        count1.incrementAndGet();
      }
    });

    // Create a simulator session
    // Get the playback
    PlaybackData pb = k.getInstance(ScriptObjects.class).load("PlaybackData").asInstanceOf(PlaybackData.class);

    EWrapper w1 = pb.newSession("simulate").withEWrapper("test").start();

    int COUNT1 = 1000;
    for (int i = 0; i < COUNT1; i++) {
      w1.tickPrice(1000, 0, 20., 0);
      w1.tickSize(2000, 0, 10000);
      w1.tickGeneric(3000, 2, 45.);
    }

    while (count1.get() < COUNT1) {
      Thread.sleep(500L);
    }
    Thread.sleep(1000L);

    assertEquals(COUNT1, count1.get());
  }
}
