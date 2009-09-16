// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.event;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import com.lab616.omnibus.Main;
import com.lab616.omnibus.SystemEvent;
import com.lab616.omnibus.event.watchers.SystemEventStats;
import com.lab616.omnibus.event.watchers.SystemEventWatcher;

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
			bindEventDefinition(new ObjectEventDefinition<SystemEvent>(
					SystemEvent.EVENT_NAME, SystemEvent.class));
		}
	}
	
	public static class EventWatchers extends AbstractEventWatcherModule {
		
		public void configure() {
			// Default system-wide watchers:
			bindEventWatcher(SystemEventWatcher.class);
			bindEventWatcher(new SystemEventStats("event", 1000L));
			bindEventWatcher(new SystemEventStats("http", 1000L));
			bindEventWatcher(new SystemEventStats("system", 1000L));
		}
	}

	public void configure() {
		bind(EventEngine.class).in(Scopes.SINGLETON);
    bind(Main.Shutdown.class).annotatedWith(Names.named("event-engine-shutdown"))
    .toProvider(EventEngine.class).in(Scopes.SINGLETON);
	}
	
	/**
	 * Builder interface for building the guice module.
	 * @author david
	 *
	 */
	public static class Builder {

		List<Module> modules = Lists.newArrayList();
		List<Class<? extends EventDefinition<?>>> dlist = Lists.newArrayList();
		List<Class<? extends AbstractEventWatcher>> wlist = Lists.newArrayList();
		List<AbstractEventWatcher> wInstances = Lists.newArrayList();
		
		
		public Builder bindEventWatcher(AbstractEventWatcher w) {
			wInstances.add(w);
			return this;
		}
		
		public Builder bindEventWatcher(
				Class<? extends AbstractEventWatcher> clz) {
			wlist.add(clz);
			return this;
		}

		public Builder bindEventDefinition(
				Class<? extends EventDefinition<?>> clz) {
			dlist.add(clz);
			return this;
		}
		
		public Iterable<Module> build() {
			Module events = new AbstractEventProducerModule() {
				public void configure() {
					for (Class<? extends EventDefinition<?>> clz : dlist) {
						bindEventDefinition(clz);
					}
				}
			};
			Module watchers = new AbstractEventWatcherModule() {
				public void configure() {
					for (Class<? extends AbstractEventWatcher> clz : wlist) {
						bindEventWatcher(clz);
					}
					for (AbstractEventWatcher w : wInstances) {
						bindEventWatcher(w);
					}
				}
			};
			modules.addAll(EventModule.allModules());
			modules.add(events);
			modules.add(watchers);
			return modules;
		}
	}
	
	public final static Builder builder() {
		return new Builder();
	}
}
