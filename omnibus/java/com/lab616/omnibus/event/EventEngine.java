// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.event;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.ConfigurationEngineDefaults;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.ConfigurationEngineDefaults.Threading;
import com.espertech.esper.client.time.CurrentTimeEvent;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.internal.Lists;
import com.lab616.monitoring.Varz;
import com.lab616.monitoring.Varzs;
import com.lab616.omnibus.Main;
import com.lab616.omnibus.Main.Shutdown;
import com.lab616.util.Time;

/**
 * An event engine that supports addition of event-selecting filter expressions
 * as well as receiving events (and dispatching to interested EventWatchers).
 * 
 * @author david
 *
 */
public class EventEngine implements Provider<Main.Shutdown<Boolean>> {

	@Varz(name = "event-engine-event-definitions-count")
	public static final AtomicInteger countEventDefinitions = new AtomicInteger();

	@Varz(name = "event-engine-event-watchers-count")
	public static final AtomicInteger countEventWatchers = new AtomicInteger();

	@Varz(name = "event-engine-event-count")
	public static final AtomicLong countEvents = new AtomicLong();

	static {
		Varzs.export(EventEngine.class);
	}
	
	public enum State {
		INITIALIZED,
		RUNNING,
		STOPPED;
	}
	
	/**
	 * The Time source.  The default is current time in microsecond.
	 */
	public interface TimeSource {
		public long now();
	}
	
	private TimeSource timeSource = new TimeSource() {
		public long now() {
			return Time.now();
		}
	};
	
	
	static Logger logger = Logger.getLogger(EventEngine.class);
	
	@SuppressWarnings("unchecked")
  private final Set<EventDefinition> eventDefinitions;
	private final Set<AbstractEventWatcher> eventWatchers;
	private final Configuration esperConfiguration;
  private final EPServiceProvider epService;
  
  private State state;
  private List<AbstractEventWatcher> watchers = Lists.newArrayList();
  
  @SuppressWarnings("unchecked")
  @Inject
	public EventEngine(
			Set<EventDefinition> eventDefinitions,
			Set<AbstractEventWatcher> eventWatchers) {
  	
  	this.eventDefinitions = eventDefinitions;
  	this.eventWatchers = eventWatchers;
		this.esperConfiguration = new Configuration();
		configureEventTypes();
		configureEngineDefaults(this.esperConfiguration.getEngineDefaults());
    this.epService = EPServiceProviderManager.getDefaultProvider(
    		this.esperConfiguration);
    this.epService.initialize();
    configureEventWatchers();
    state = State.INITIALIZED;
	}
	
  /**
   * Sets the time source for the engine. Useful for testing.
   * @param ts The time source.
   * @return The engine.
   */
  public final EventEngine setTimeSource(TimeSource ts) {
  	this.timeSource = ts;
  	return this;
  }
  
  /**
   * Initializes the configuration by registering all statically defined
   * event types.
   */
	private void configureEventTypes() {
		// System events.
		for (EventDefinition<?> e : getEventDefinitions()) {
			e.configure(this.esperConfiguration);
			countEventDefinitions.incrementAndGet();
		}
		// Application events.
		for (EventDefinition<?> e : this.eventDefinitions) {
			e.configure(this.esperConfiguration);
			countEventDefinitions.incrementAndGet();
		}
	}

	protected void configureEngineDefaults(ConfigurationEngineDefaults defaults) {
		Threading threading = defaults.getThreading();
		threading.setInternalTimerEnabled(false);
		threading.setListenerDispatchPreserveOrder(true);
		threading.setInsertIntoDispatchPreserveOrder(true);
		
		threading.setThreadPoolInbound(true);
		threading.setThreadPoolInboundNumThreads(10);
		threading.setThreadPoolInboundCapacity(10000);
		
		threading.setThreadPoolOutbound(true);
		threading.setThreadPoolOutboundNumThreads(10);
		threading.setThreadPoolOutboundCapacity(10000);
	}
	
	private void configureEventWatchers() {
		now(this.timeSource.now()); // Sets the time for the engine.
		for (AbstractEventWatcher watcher : this.eventWatchers) {
			add(watcher);
		}
	}
	
	/**
	 * Adds a new event watcher during runtime.
	 * @param watcher The new watcher.
	 */
	public final void add(AbstractEventWatcher watcher) {
		EPAdministrator admin = this.epService.getEPAdministrator();
		watcher.setEngine(this);
		EPStatement statement = watcher.createStatement(admin);
		// Set the watcher as the subscriber.  The watcher is free to 
		// implement some kind of pubsub that further propagates the event.
		statement.setSubscriber(watcher.getSubscriber());
		watcher.setStatement(statement);
		watchers.add(watcher);
		watcher.start();
		countEventWatchers.incrementAndGet();
		logger.info("Watcher added: " + watcher);
	}

	/**
	 * Posts an event to the engine.
	 * @param eventObject The event object.
	 */
	public final void post(Object eventObject) {
		now();
		this.epService.getEPRuntime().sendEvent(eventObject);
		countEvents.incrementAndGet();
	}
	
	/**
	 * Removes the watcher from the list tracked by the engine.
	 * @param watcher The watcher.
	 */
	public final void remove(AbstractEventWatcher watcher) {
		watchers.remove(watcher);
		watcher.stop();
		countEventWatchers.decrementAndGet();
	}
	
	/**
	 * Returns the esper implementation.
	 * 
	 * @return The engine.
	 */
	protected final EPServiceProvider esper() {
		return this.epService;
	}
	
	/**
	 * Derived class can override this to provide its own default events.
	 * @return The list of event types.
	 */
	protected List<EventDefinition<?>> getEventDefinitions() {
		return Lists.newArrayList();
	}	

	/**
	 * Returns the esper configuration used for this engine.  For testing only.
	 * @return The configuration.
	 */
	Configuration getConfiguration() {
		return this.esperConfiguration;
	}
	
	/**
	 * returns the list of known event watchers.  For testing only.
	 * @return The list of event watchers
	 */
	List<AbstractEventWatcher> getEventWatchers() {
		return ImmutableList.copyOf(this.eventWatchers);
	}
	
	/**
	 * Sends a time event to the engine so that the engine is running on the
	 * same clock as the external source.  In our case, we use microsecond
	 * resolution.
	 */
	public final void now(long t) {
		CurrentTimeEvent ct = new CurrentTimeEvent(t);
		try {
			this.epService.getEPRuntime().sendEvent(ct);
		} catch (Exception e) {
			throw new EventEngineException(e);
		}
	}
	
	/**
	 * Advances the time tracked by the engine by the current time in microsecond.
	 */
	public final void now() {
		now(this.timeSource.now());
	}
	
	/**
	 * Returns the state of the engine.
	 * @return The state.
	 */
	public final State getState() {
		return this.state;
	}
	
	/**
	 * Starts the engine.
	 */
	public final boolean start() {
		// Not interesting.  Just send another time event
		now(this.timeSource.now());
		this.epService.getEPAdministrator().startAllStatements();
		this.state = State.RUNNING;
		return true;
	}
	
	public final boolean stop() {
		this.epService.getEPAdministrator().stopAllStatements();
		this.epService.destroy();
		this.state = State.STOPPED;
		return true;
	}

	public Shutdown<Boolean> get() {
	  return new Shutdown<Boolean>() {
			public Boolean call() throws Exception {
				logger.info("Event engine shutting down.");
				return stop();
      }
			public String getName() {
	      return "event-engine-shutdown";
      }
	  };
  }
	
	
}
