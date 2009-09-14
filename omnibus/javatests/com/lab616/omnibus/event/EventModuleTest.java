// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.event;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;

import junit.framework.TestCase;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.internal.Lists;
import com.lab616.common.logging.Logging;
import com.lab616.omnibus.SystemEvent;

/**
 * @author david
 *
 */
public class EventModuleTest extends TestCase {

	static {
		Logging.init(Level.INFO);
	}
	
	public static class TestEvent {
		public String name;
	}
	
	public static class TestEvent2 {
		public String name;
	}

	public void testEventModuel() throws Exception {

		final ObjectEventDefinition<TestEvent> eType1 = 
			new ObjectEventDefinition<TestEvent>("test1", TestEvent.class);
		
		final ObjectEventDefinition<TestEvent2> eType2 = 
			new ObjectEventDefinition<TestEvent2>(TestEvent2.class);

		Module extension = new AbstractEventProducerModule() {
			public void configure() {
				bind(eType1);
				bind(eType2);
			}
		};
		
		final EventWatcher watcher = new EventWatcher() {
			
		};
		
		Module watchers = new AbstractEventWatcherModule() {
			public void configure() {
				bind(watcher);
			}
		};
		
		List<Module> allModules = Lists.newArrayList(EventModule.allModules());
		allModules.add(extension);
		allModules.add(watchers);
		
		Injector injector = Guice.createInjector(allModules);
		
		EventEngine engine = injector.getInstance(EventEngine.class);
		
		assertNotNull(engine);
		
		Map<String, String> events = engine.getConfiguration().getEventTypeNames();
		assertTrue(events.keySet().contains(eType1.name()));
		assertTrue(events.keySet().contains(eType2.name()));
		
		assertEquals(eType1.type().getName(), events.get(eType1.name()));
		assertEquals(eType2.type().getName(), events.get(eType2.name()));
		
		assertTrue(events.keySet().contains(SystemEvent.EVENT_NAME));
		assertEquals(
				SystemEvent.class.getName(), events.get(SystemEvent.EVENT_NAME));

		List<EventWatcher> rwatchers = engine.getEventWatchers();
		assertTrue(rwatchers.contains(watcher));
		assertEquals(engine, rwatchers.get(0).getEngine());
	}
}
