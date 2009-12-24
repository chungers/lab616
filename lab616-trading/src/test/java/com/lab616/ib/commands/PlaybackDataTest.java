// 2009-2010 lab616.com, All Rights Reserved.
package com.lab616.ib.commands;

import java.net.BindException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import com.ib.client.EWrapper;
import com.lab616.common.scripting.ScriptObjects;
import com.lab616.ib.api.TWSClientModule;
import com.lab616.ib.api.servlets.TWSControllerModule;
import com.lab616.ib.api.simulator.EClientSocketSimulator;
import com.lab616.ib.api.simulator.EWrapperDataSource;
import com.lab616.omnibus.Kernel;
import com.lab616.omnibus.event.EventEngine;
import com.lab616.omnibus.event.EventMessage;
import com.lab616.omnibus.event.EventWatcher;
import com.lab616.util.Time;

/**
 * @author david
 *
 */
public class PlaybackDataTest extends TestCase {

	static Logger logger = Logger.getLogger(PlaybackDataTest.class);
	
	/**
	 * Tests the basic use case of starting a simulator and send events to clients.
	 * @throws Exception
	 */
	public void testSetupSimulator() throws Exception {
		Kernel k = new Kernel(null, 
				new TWSClientModule(),
				new TWSControllerModule(),
				new CommandModule())
			.runInThread(new String[] {});

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
		PlaybackData p = (PlaybackData) ScriptObjects.load("tws.PlaybackData");
		assertNotNull(p);
	
		logger.info("Starting simulator");
		EClientSocketSimulator sim = p.startSimulator("simulate");
		
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

		k.shutdown();
		logger.info("latency: before = " + dt + ", after = " + dt2);
	}
}
