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
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.ConfigurationEngineDefaults.Threading;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.common.collect.Lists;
import com.google.inject.name.Named;
import com.lab616.common.flags.Flag;
import com.lab616.common.flags.Flags;
import com.lab616.monitoring.Varz;
import com.lab616.monitoring.Varzs;
import com.lab616.omnibus.Kernel;
import com.lab616.omnibus.Kernel.Shutdown;

/**
 * An event engine that supports addition of event-selecting filter expressions
 * as well as receiving events (and dispatching to interested EventWatchers).
 * 
 * @author david
 *
 */
public class EventEngine implements Kernel.Startable, Provider<Kernel.Shutdown<Boolean>> {

	@Varz(name = "event-engine-event-definitions-count")
	public static final AtomicInteger countEventDefinitions = new AtomicInteger();

	@Varz(name = "event-engine-event-watchers-count")
	public static final AtomicInteger countEventWatchers = new AtomicInteger();

	@Varz(name = "event-engine-event-count")
	public static final AtomicLong countEvents = new AtomicLong();

  @Flag(name = "event-engine-dispatch-timeout")
  public static Long DISPATCH_TIMEOUT = 100L;

	static {
		Varzs.export(EventEngine.class);
    Flags.register(EventEngine.class);
	}
	
	public enum State {
		INITIALIZED,
		RUNNING,
		STOPPED;
	}
	
  public interface Subscriber<T> {
    public void update(T obj);
  }

	static Logger logger = Logger.getLogger(EventEngine.class);
	
  @SuppressWarnings("unchecked")
  private final Set<EventDefinition> eventDefinitions;
	private final Set<AbstractEventWatcher> eventWatchers;
	private final Configuration esperConfiguration;
  private final EPServiceProvider epService;
 
  private State state;
  private List<AbstractEventWatcher> watchers = Lists.newArrayList();
  private final int threads;
  private final int capacity;
  
  @SuppressWarnings("unchecked")
  @Inject
	public EventEngine(
			Set<EventDefinition> eventDefinitions,
			Set<AbstractEventWatcher> eventWatchers,
			@Named("event-engine-threads") int threads,
      @Named("event-engine-queue-capacity") int queueCapacity) {
  	this.eventDefinitions = eventDefinitions;
  	this.eventWatchers = eventWatchers;
  	this.threads = threads;
  	this.capacity = queueCapacity;
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
	  defaults.getTimeSource().setTimeSourceType(
		    ConfigurationEngineDefaults.TimeSourceType.NANO);
	  
	  Threading threading = defaults.getThreading();
	  threading.setInternalTimerEnabled(true);

	  threading.setListenerDispatchPreserveOrder(true);
		threading.setInsertIntoDispatchPreserveOrder(true);

		threading.setInsertIntoDispatchTimeout(DISPATCH_TIMEOUT);
    threading.setListenerDispatchTimeout(DISPATCH_TIMEOUT);

    threading.setThreadPoolInbound(true);
		threading.setThreadPoolInboundNumThreads(threads);
		threading.setThreadPoolInboundCapacity(capacity);
		
		threading.setThreadPoolOutbound(true);
		threading.setThreadPoolOutboundNumThreads(threads);
		threading.setThreadPoolOutboundCapacity(capacity);
	}
	
	private void configureEventWatchers() {
		for (AbstractEventWatcher watcher : this.eventWatchers) {
			add(watcher);
		}
	}

  /**
   * Stoppable statement reference.
   */
  public interface Stoppable {
    public void halt();
  }

	/**
	 * Adds a simple event watcher.
	 * @param watcher The watcher.
   * @return Stoppable statement.
	 */
	public final <E> Stoppable add(EventWatcher<E> watcher) {
		return add((AbstractEventWatcher)watcher);
	}
	
	/**
	 * Given the statement and args, and implementation of a subscriber, add
	 * an event watcher.
	 * @param <E> The event object type.
	 * @param sub The subscriber interface.
	 * @param statement The statement.
	 * @param args The args, nullable.
	 * @return The stoppable statement.
	 */
	public final <E> Stoppable add(final EventSubscriber<E> sub,
			final String statement, Object... args) {
		final Object[] argv = (args != null) ? args : new Object[] {};
		return add(new EventWatcher<E>(statement, argv) {
			@Override
			public void update(E event) {
				sub.update(event);
			}
		});
	}
	
	/**
	 * Convenient method for adding static statement and subscriber to events of E.
	 * @param <E> Event 
	 * @param statement The statement with no ? variables.
	 * @param sub The subscriber.
	 * @return Statement that can be stopped.
	 */
	public final <E> Stoppable add(String statement, EventSubscriber<E> sub) {
		return add(sub, statement);
	}

	/**
	 * Adds a new event watcher during runtime.
	 * @param watcher The new watcher.
   * @return The stoppable statement instance.
	 */
	public final Stoppable add(final AbstractEventWatcher watcher) {
		EPAdministrator admin = this.epService.getEPAdministrator();
		watcher.setEngine(this);
		EPStatement statement = watcher.createStatement(admin);
		// Set the watcher as the subscriber.  The watcher is free to 
		// implement some kind of pubsub that further propagates the event.
		statement.setSubscriber(watcher.getSubscriber());
		watcher.setStatement(statement);
		watchers.add(watcher);
		watcher.start();
    watcher.addedToEngine();
		countEventWatchers.incrementAndGet();
		logger.info("Watcher added: " + watcher);
		return new Stoppable() {
      @Override
      public void halt() {
        watcher.stop();
      }
    };
	}

	
	public <T> StreamSplitter<T> splitEventStream(Class<T> eventType) {
	  return new StreamSplitter<T>(eventType, this.eventDefinitions,
	      this.epService.getEPAdministrator());
	}
	
	/**
	 * Posts an event to the engine.
	 * @param eventObject The event object.
	 */
	public final void post(Object eventObject) {
		try {
		  EPRuntime runtime = this.epService.getEPRuntime();
	    runtime.sendEvent(eventObject);
	    countEvents.incrementAndGet();
		} catch (NullPointerException e) {
	    logger.warn("No runtime. Engine stopped.");
		}
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
	 * Returns the state of the engine.
	 * @return The state.
	 */
	public final State getState() {
		return this.state;
	}
	
	/**
	 * Returns if this engine is in the RUNNING state.
	 * @return True if in RUNNING.
	 */
	public final boolean running() {
		return getState() == State.RUNNING;
	}
	
	/**
	 * Starts the engine.
	 * @return This engine.
	 */
	public final void start() {
		// Not interesting.  Just send another time event
		this.epService.getEPAdministrator().startAllStatements();
		this.state = State.RUNNING;
	}
	
	/**
	 * Stops the engine.
	 * @return This engine.
	 */
	public final boolean stop() {
		this.epService.getEPAdministrator().stopAllStatements();
		this.epService.destroy();
		this.state = State.STOPPED;
		return true;
	}

	/**
	 * Guice provider method for shutdown
	 * @return The shutdown hook.
	 */
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
