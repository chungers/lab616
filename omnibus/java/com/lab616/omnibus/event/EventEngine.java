// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.event;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.ConfigurationEngineDefaults;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.ConfigurationEngineDefaults.Threading;
import com.espertech.esper.client.time.CurrentTimeEvent;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.internal.Lists;
import com.lab616.util.Time;

/**
 * An event engine that supports addition of event-selecting filter expressions
 * as well as receiving events (and dispatching to interested EventWatchers).
 * 
 * @author david
 *
 */
public class EventEngine {

	public enum State {
		INITIALIZED,
		RUNNING,
		STOPPED;
	}
	
	static Logger logger = Logger.getLogger(EventEngine.class);
	
	@SuppressWarnings("unchecked")
  private final Set<EventDefinition> eventDefinitions;
	private final Set<EventWatcher> eventWatchers;
	private final Configuration esperConfiguration;
  private final EPServiceProvider epService;
  
  private State state;
  
  @SuppressWarnings("unchecked")
  @Inject
	public EventEngine(Set<EventDefinition> eventDefinitions,
			Set<EventWatcher> eventWatchers) {
  	this.eventDefinitions = eventDefinitions;
  	this.eventWatchers = eventWatchers;
		this.esperConfiguration = new Configuration();
		defineEventTypes();
		defineEngineDefaults(this.esperConfiguration.getEngineDefaults());
    this.epService = EPServiceProviderManager.getDefaultProvider(
    		this.esperConfiguration);
    this.epService.initialize();
    initializeWatchers();
    state = State.INITIALIZED;
	}
	
  /**
   * Initializes the configuration by registering all statically defined
   * event types.
   */
	private void defineEventTypes() {
		// System events.
		for (EventDefinition<?> e : getEventDefinitions()) {
			e.configure(this.esperConfiguration);
		}
		
		// Application events.
		for (EventDefinition<?> e : this.eventDefinitions) {
			e.configure(this.esperConfiguration);
		}
	}

	protected void defineEngineDefaults(ConfigurationEngineDefaults defaults) {
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
	
	private void initializeWatchers() {
		now(); // Sets the time for the engine.
		for (EventWatcher w : this.eventWatchers) {
			w.setEngine(this);
		}
	}
	
	private void checkState(State toCheck, State... others) 
		throws EventEngineException {
		List<State> allowed = Lists.newArrayList(toCheck, others);
		if (!allowed.contains(this.state)) {
			throw new EventEngineException(
					EventEngineException.ErrorCode.INVALID_STATE, this.state);
		}
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
	 * 
	 * @return The list of event types.
	 */
	protected List<EventDefinition<?>> getEventDefinitions() {
		return Lists.newArrayList();
	}
	

	/**
	 * Returns the esper configuration used for this engine.  For testing only.
	 * 
	 * @return The configuration.
	 */
	Configuration getConfiguration() {
		return this.esperConfiguration;
	}
	
	/**
	 * returns the list of known event watchers.  For testing only.
	 * 
	 * @return The list of event watchers
	 */
	List<EventWatcher> getEventWatchers() {
		return ImmutableList.copyOf(this.eventWatchers);
	}
	
	/**
	 * Sends a time event to the engine so that the engine is running on the
	 * same clock as the external source.  In our case, we use microsecond
	 * resolution.
	 */
	public final void now() {
		CurrentTimeEvent ct = new CurrentTimeEvent(Time.now());
		try {
			this.epService.getEPRuntime().sendEvent(ct);
		} catch (Exception e) {
			throw new EventEngineException(e);
		}
	}
	
	public final State getState() {
		return this.state;
	}
	
	/**
	 * Starts the engine.
	 */
	public void start() {
		// Not interesting.  Just send another time event
		now();
	}
	
	public void stop() {
		checkState(State.INITIALIZED, State.RUNNING);
		this.epService.destroy();
	}
}
