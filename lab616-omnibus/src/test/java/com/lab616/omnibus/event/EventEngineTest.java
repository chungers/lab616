// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.event;

import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.lab616.common.logging.Logging;
import com.lab616.omnibus.SystemEvent;
import com.lab616.omnibus.event.annotation.Statement;

/**
 * @author david
 *
 */
public class EventEngineTest extends TestCase {

	static Logger logger = Logger.getLogger(EventEngineTest.class);
	
	static {
		Logging.init(Level.INFO);
	}
	
	@Statement("select * from SystemEvent")
	public static class SystemEventWatcher extends AbstractEventWatcher {
		static final AtomicInteger events = new AtomicInteger();
		public void update(SystemEvent event) {
			logger.info("Got " + event);
			events.incrementAndGet();
		}
	}
	
	private SystemEvent sendSystemEvent(EventEngine engine, 
			String component, String method, Object... args) throws Exception {
		return sendSystemEvent(10L, engine, component, method, args);
	}
	
	private SystemEvent sendSystemEvent(long wait, EventEngine engine, 
			String component, String method, Object... args) throws Exception {
		SystemEvent event = new SystemEvent();
		event.setComponent(component);
		event.setMethod(method);

		int i = 0; String p = null; String v = null;
		for (Object o : args) {
			if (i++ % 2 == 0) {
				p = o.toString();
				v = null;
			} else {
				v = o.toString();
			}
			if (p != null && v != null) {
				event.setParam(p, v);
				p = null; v = null;
			}
		}
		
		engine.post(event);
		
		Thread.sleep(wait);
		return event;
	}

	/**
	 * Tests sending SystemEvent.
	 * @throws Exception
	 */
	public void testSendEvents() throws Exception {

		Injector injector = Guice.createInjector(
				EventModule.builder()
					.bindEventWatcher(SystemEventWatcher.class)
					.build());
		
		EventEngine engine = injector.getInstance(EventEngine.class);
		engine.start();
		// Create a SytemEvent
		for (int i = 0; i < 5; i++) {
			sendSystemEvent(engine, "test", "test", "itr", i);
		}
		
		Thread.sleep(100L); // wait for all messages to be delivered.
		assertEquals(5, SystemEventWatcher.events.get());
	}

	
	@Statement("select * from SystemEvent where component = 'http'")
	public static class HttpEventWatcher extends AbstractEventWatcher {
		static final AtomicInteger events = new AtomicInteger();
		public void update(SystemEvent event) {
			logger.info("Got " + event);
			events.incrementAndGet();
		}
	}

	public void testSelectFilter() throws Exception {
		SystemEventWatcher.events.set(0);
		HttpEventWatcher.events.set(0);

		Injector injector = Guice.createInjector(
				EventModule.builder()
					.bindEventWatcher(SystemEventWatcher.class)
					.bindEventWatcher(HttpEventWatcher.class)
					.build());
		
		EventEngine engine = injector.getInstance(EventEngine.class);
		engine.start();
		// Create a SytemEvent
		for (int i = 0; i < 50; i++) {
			if (i % 2 == 0) {
				sendSystemEvent(engine, "test", "test", "itr", i);
			} else {
				sendSystemEvent(engine, "http", "test", "itr", i);
			}
		}
		
		Thread.sleep(100L); // wait for all messages to be delivered.
		assertEquals(50, SystemEventWatcher.events.get());
		assertEquals(25, HttpEventWatcher.events.get());
	}

	@Statement("select * from SystemEvent")
	public static class SpawnWatcher extends AbstractEventWatcher {
		static final AtomicInteger events = new AtomicInteger();
		static final AtomicInteger childEvents = new AtomicInteger();
		public void update(SystemEvent event) {
			logger.info("Got " + event);
			events.incrementAndGet();
			if (events.get() == 39) {
				add(new ChildWatcher(
						"select * from SystemEvent where component=?",
						"test"));
			}
		}
	}

	public static class ChildWatcher extends EventWatcher<SystemEvent> {
		static final AtomicInteger events = new AtomicInteger();
		public ChildWatcher(String exp, Object... args) {
			super(exp, args);
		}
		@Override
		public void update(SystemEvent event) {
			logger.info(this + " got " + event);
			events.incrementAndGet();
		}
	}

	public static class SystemEventSubscriber {
		static final AtomicInteger events = new AtomicInteger();
		public void receive(SystemEvent e) {
			logger.info(this + " received => " + e);
			events.incrementAndGet();
		}
	}
	
	/**
	 * Example of a watcher that uses Provider to get a new delegate that
	 * gets the update in a thread-safe way.  This is because esper allows only
	 * a single subscriber object and this subscriber object is shared by many
	 * threads.  So instead, each time update is called, a new SystemEventWatcher
	 * is obtained from the injected Provider.
	 */
	@Statement("select * from SystemEvent where component='http'")
	public static class PerThreadSystemEventWatcher extends AbstractEventWatcher {
		Provider<SystemEventSubscriber> provider;
		@Inject
		public PerThreadSystemEventWatcher(Provider<SystemEventSubscriber> pr) {
			provider = pr;
		}
		public void update(SystemEvent event) {
			SystemEventSubscriber w = provider.get();
			logger.info("Forwarding message to " + w);
			w.receive(event);
		}
	}
	
	/**
	 * Tests the dynamic creation of watcher.
	 * @throws Exception
	 */
	public void testDynamicWatcher() throws Exception {
		SystemEventWatcher.events.set(0);
		HttpEventWatcher.events.set(0);
		SpawnWatcher.events.set(0);
		ChildWatcher.events.set(0);

		Injector injector = Guice.createInjector(
				EventModule.builder()
					.bindEventWatcher(SystemEventWatcher.class)
					.bindEventWatcher(HttpEventWatcher.class)
					.bindEventWatcher(SpawnWatcher.class)
					.bindEventWatcher(PerThreadSystemEventWatcher.class)
					.build());
		
		EventEngine engine = injector.getInstance(EventEngine.class);
		engine.start();
		// Create a SytemEvent
		for (int i = 0; i < 50; i++) {
			if (i % 2 == 0) {
				// Longer delay because we need to let the engine add the statement
				// in time for the next event. This is normally not possibly in real
				// life but we need this to check for the event counts.
				// The lower bound of this wait can approximate the time it takes for
				// a new statement to become active in the engine.
				sendSystemEvent(200L, engine, "test", "rpc", "itr", i);
			} else {
				sendSystemEvent(200L, engine, "http", "rpc", "itr", i);
			}
		}
		
		Thread.sleep(100L); // wait for all messages to be delivered.
		assertEquals(50, SystemEventWatcher.events.get());
		assertEquals(25, HttpEventWatcher.events.get());
		assertEquals(5, ChildWatcher.events.get());
		assertEquals(25, SystemEventSubscriber.events.get());
	}
}
