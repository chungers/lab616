// 2009-2010 lab616.com, All Rights Reserved.
package com.lab616.ib.scripting;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import com.ib.client.EWrapper;
import com.lab616.common.scripting.ScriptObjects;
import com.lab616.ib.api.TWSClientModule;
import com.lab616.ib.api.proto.TWSProto;
import com.lab616.ib.api.servlets.TWSControllerModule;
import com.lab616.ib.api.simulator.EClientSocketSimulator;
import com.lab616.ib.api.simulator.EWrapperDataSource;
import com.lab616.ib.api.util.ProtoDataFile;
import com.lab616.ib.scripting.PlaybackData.Playback.Source;
import com.lab616.omnibus.Kernel;
import com.lab616.omnibus.event.EventEngine;
import com.lab616.omnibus.event.EventMessage;
import com.lab616.omnibus.event.EventSubscriber;
import com.lab616.omnibus.event.EventWatcher;
import com.lab616.omnibus.event.EventEngine.Stoppable;
import com.lab616.util.Time;

/**
 * @author david
 *
 */
public class PlaybackDataTest extends TestCase {

	static Logger logger = Logger.getLogger(PlaybackDataTest.class);

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

	/**
	 * Tests the basic use case of starting a simulator and send events to clients.
	 * @throws Exception
	 */
	public void testSetupSimulator() throws Exception {
		long maxWait = 5000L;
		assertTrue(k.isRunning(maxWait) && !k.hasExceptions());

		final Integer tickerId = 999;
		final AtomicInteger count = new AtomicInteger();
		
		// Since the kernel may not have completely started up, we wait up to 
		// one second for the engine to be ready.
		EventEngine engine = k.getInstance(EventEngine.class, 1000L);
		
		final AtomicLong start = new AtomicLong();
		final AtomicLong last = new AtomicLong();
		
		engine.add(new EventWatcher<EventMessage>(
				"select * from EventMessage") {
      @Override
			public void update(EventMessage event) {
				if (count.get() == 0) {
					start.set(Time.now());
				}
				count.incrementAndGet();
				assertEquals(tickerId, (Integer)event.args[0]);
				assertEquals("tickPrice", event.method);
				last.set(Time.now());
			}
		});

    // Get the command
		PlaybackData p = k.getInstance(ScriptObjects.class)
      .load("PlaybackData").asInstanceOf(PlaybackData.class);

		assertNotNull(p);
	
		logger.info("Starting simulator");
		EClientSocketSimulator sim = p.startSimulator("simulate").first;
		
		logger.info("Looking up reference of simulator");
		EClientSocketSimulator sim2 = EClientSocketSimulator.getSimulator("simulate", 0);
		
		assertTrue(sim == sim2);
		
		// Now we programmatically pump data into this client.
		EWrapperDataSource ds = new EWrapperDataSource("FakeEWrapper");
		sim.addDataSource(ds);
		
		// Use the EWrapper interface to mock data received by the client:
		EWrapper w = ds.getEWrapper();
		
		// Maven's test framework seems to run multiple tests and we have problems
		// with port bindings for http.
		assertTrue(k.isRunning(maxWait) && !k.hasExceptions());
		
		int COUNT = 500;
		long t = 0, dt = 0, dt2 = 0;		

		// Start
		ds.start();
		
		t = Time.now();
		dt = t;
		for (int i = 0; i < COUNT; i++) {
			w.tickPrice(tickerId, 1, 30.0, 0);
			Thread.sleep(1);
			dt = Time.now() - t;
		}
		dt2 = last.get() - start.get();

		// Note this test will hang if somehow events are dropped by the engine.
		while (count.get() < COUNT) {
			Thread.sleep(500);  // Need sometime to let the events flow through.
		}

		assertEquals(COUNT, count.get());

		logger.info("latency: before = " + dt + ", after = " + dt2);
  }
  
  public void DISABLED_testPlayback() throws Exception {
    final AtomicInteger count1 = new AtomicInteger(0);

    // Select only events with tickId = 1000.
    k.getInstance(EventEngine.class, 10000L).add(
        new EventSubscriber<EventMessage>() {
          @Override
          public void update(EventMessage t) {
            count1.incrementAndGet();
          }
        },
        "select * from EventMessage s0 where s0.args[0]?=?", 1000);

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

	private File getDataFile() throws Exception {
		String workingDir = System.getProperty("user.dir");
		String dataFile = workingDir + "/" + System.getProperty(
				"dataFilesDir", "testing/") + "/unittest.proto";
		return new File(dataFile);
	}

  public void DISABLED_testPlayback2() throws Exception {

		// First read it and get record count.
    File pf = getDataFile();
		int actual = 0;
		for (TWSProto.Event event : ProtoDataFile.getReader(pf.getAbsolutePath()).readAll()) {
			assertTrue(event.isInitialized());
			actual++;
		}
		System.out.println("Actual read = " + actual); // 37787

    final AtomicInteger count1 = new AtomicInteger(0);

    Stoppable statement = k.getInstance(EventEngine.class, 1000L).add(
      "select * from EventMessage",
      new EventSubscriber<EventMessage>() {

        @Override
        public void update(EventMessage t) {
          count1.incrementAndGet();
          if (count1.get() % 5000 == 0) {
            logger.info("count = " + count1.get());
          }
        }
      });

    // Create a simulator session
    // Get the playback
    PlaybackData pb = k.getInstance(ScriptObjects.class)
      .load("PlaybackData").asInstanceOf(PlaybackData.class);

    // Load the data with 1 msec latency between ticks.
    Source src = pb.newSession("simulate").withProtoFile(
      pf.getAbsolutePath(), 1L).start();

    long t1 = System.nanoTime();

    while (!pb.isQueueEmpty("simulate", 0) || !src.finished()) {
      logger.info("Queue = " + pb.getQueueDepth("simulate", 0));
      Thread.sleep(1500L);
    }

    long dt = System.nanoTime() - t1;
    logger.info("dt = " + dt + ", qps=" + 1e9/dt * count1.get());
    assertEquals(actual, count1.get());

    // Stop the statement.
    statement.halt();
  }
}
