// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.event;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.lab616.omnibus.SystemEvent;

/**
 * Guice module for the event engine.
 * @author david
 *
 */
public class EventModule extends AbstractModule {

	/**
	 * Returns all relevant modules for this component.
	 * 
	 * @return list of modules.
	 */
	public static List<Module> allModules() {
		List<Module> list = Lists.newArrayList();
		list.add(new EventProducers());
		list.add(new EventWatchers());
		list.add(new EventModule());
		return list;
	}
	
	public static class EventProducers extends AbstractEventProducerModule {

		public void configure() {
			bind(new ObjectEventDefinition<SystemEvent>(
					SystemEvent.EVENT_NAME, SystemEvent.class));
		}
	}
	
	public static class EventWatchers extends AbstractEventWatcherModule {
		
		public void configure() {
		}
	}

	public void configure() {
		bind(EventEngine.class).in(Scopes.SINGLETON);
	}
}
