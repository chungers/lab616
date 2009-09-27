// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.event;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import com.lab616.common.flags.Flag;
import com.lab616.common.flags.Flags;
import com.lab616.omnibus.Main;
import com.lab616.omnibus.SystemEvent;
import com.lab616.omnibus.event.watchers.SystemEventStats;
import com.lab616.omnibus.event.watchers.SystemEventWatcher;

/**
 * Guice module for the event engine.
 * @author david
 *
 */
public class EventModule extends AbstractEventModule {

  @Flag(name = "event-engine-threads")
  public static Integer NUM_THREADS = 20;
  
  static {
    Flags.register(EventModule.class);
  }

	public void configure() {
	  // Flags
	  bindConstant().annotatedWith(Names.named("event-engine-threads"))
	    .to(NUM_THREADS);
	  
    // Event definitions.
	  bindEventDefinition(new ObjectEventDefinition<SystemEvent>(
        SystemEvent.EVENT_NAME, SystemEvent.class));

    // Default system-wide watchers:
    bindEventWatcher(SystemEventWatcher.class);
    bindEventWatcher(new SystemEventStats("event", 1000L));
    bindEventWatcher(new SystemEventStats("http", 1000L));
    bindEventWatcher(new SystemEventStats("system", 1000L));

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
			Module events = new AbstractEventModule() {
				public void configure() {
					for (Class<? extends EventDefinition<?>> clz : dlist) {
						bindEventDefinition(clz);
					}
				}
			};
			Module watchers = new AbstractEventModule() {
				public void configure() {
					for (Class<? extends AbstractEventWatcher> clz : wlist) {
						bindEventWatcher(clz);
					}
					for (AbstractEventWatcher w : wInstances) {
						bindEventWatcher(w);
					}
				}
			};
			modules.add(events);
			modules.add(watchers);
      modules.add(new EventModule());
			return modules;
		}
	}
	
	public final static Builder builder() {
		return new Builder();
	}
}
