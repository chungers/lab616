// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api.simulator;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.google.inject.internal.Maps;
import com.google.inject.internal.Sets;
import com.ib.client.Contract;
import com.ib.client.EClientSocket;
import com.ib.client.EWrapper;
import com.lab616.common.Pair;
import com.lab616.concurrent.QueueProcessor;
import com.lab616.ib.api.ApiBuilder;
import com.lab616.ib.api.ApiMethods;
import com.lab616.ib.api.ManagedEClientSocket;
import com.lab616.ib.api.TWSClientManager.Managed;
import com.lab616.ib.api.proto.TWSProto;

/**
 *
 *
 * @author david
 *
 */
public class EClientSocketSimulator extends Thread 
  implements Managed {

  static Logger logger = Logger.getLogger(EClientSocketSimulator.class);
  
  private static Map<String, EClientSocketSimulator> simulators =
    Maps.newHashMap();
  
  public static EClientSocketSimulator getSimulator(String name, int id) {
    return simulators.get(makeKey(name, id));
  }
    
  private int clientId;
  private Set<Integer> ticks = Sets.newHashSet();
  private Set<Integer> bars = Sets.newHashSet();
  private Set<Integer> dom = Sets.newHashSet();
  private AtomicBoolean running = new AtomicBoolean(true);
  
  // Work queue for this simulator.  It is basically a work queue where
  // each data source on the queue is executed.  So more than one data source
  // can sequentially drive the simulated client.
  private QueueProcessor<DataSource, Void> queuedDataSources;

  // TWSProto.Events to be sent to the EWrapper.
  private BlockingQueue<TWSProto.Event> eventQueue = 
    new LinkedBlockingQueue<TWSProto.Event>();
  
  
  // Executor for running the data source.
  private EWrapper wrapper;
  private String clientName;
  private int eWrapperInvocations = 0;
  private String source = null;

  public EClientSocketSimulator(String name, int id) {
    clientName = name;
    source = makeKey(name, id);
    super.setName(getClass().getSimpleName() + "-" + makeKey(name, id));
    simulators.put(makeKey(name, id), this);
    this.queuedDataSources = new QueueProcessor<DataSource, Void>(name, false) {
      @Override
      protected boolean handleException(Exception e) {
        if (e instanceof IOException) {
          return false;
        } else {
          return true;
        }
      }
      @Override
      protected boolean onStart() {
        logger.info("Started simulator @" + clientName);
        return true;
      }
      @Override
      protected void onStop(int queueSize) {
        logger.info("Stopped simulator @" + clientName + ", depth=" + queueSize);
      }
    };
    logger.info("Starting queue " + this.queuedDataSources);
    this.queuedDataSources.start();
  }
  
  public static String makeKey(String profile, int id) {
  	return String.format("%s-%s", profile, id);
  }

  public void addDataSource(DataSource ds) {
    ds.setSink(this.eventQueue);
    this.queuedDataSources.enqueue(ds);
  }

  @Override
  public void halt() {
    running.set(false);
  }
  
  @Override
  public boolean isReady(long... timeout) {
    return this.wrapper != null;
  }

  public String getClientName() {
    return clientName;
  }

  public Set<Integer> getTickerIdsForMkData() {
    return ticks;
  }
  
  public Set<Integer> getTickerIdsForRealtimeBars() {
    return bars;
  }
  
  public Set<Integer> getTickerIdsForMkDepth() {
    return dom;
  }

  public Integer getClientId() {
    return clientId;
  }
  
  public EClientSocket create(final EWrapper wrapper) {
    this.wrapper = wrapper;
    return new ManagedEClientSocket(wrapper) {

      boolean isConnected = false;
      
      @Override
      public boolean isConnected() {
        return isConnected;
      }

      @Override
      protected boolean pre_eConnect(Socket socket, int clientId)
          throws IOException {
        EClientSocketSimulator.this.clientId = clientId;
        isConnected = true;
        return false;
      }

      @Override
      protected boolean pre_eConnect(String host, int port, int clientId) {
        EClientSocketSimulator.this.clientId = clientId;
        isConnected = true;
        return false;
      }

      @Override
      protected boolean pre_eDisconnect() {
        EClientSocketSimulator.this.running.set(false);
        isConnected = false;
        return false;
      }

      @Override
      protected boolean pre_reqMktData(int tickerId, Contract contract,
          String genericTickList, boolean snapshot) {
        if (!EClientSocketSimulator.this.ticks.contains(tickerId)) {
          EClientSocketSimulator.this.ticks.add(tickerId);
        }
        return false;
      }

      @Override
      protected boolean pre_reqMktDepth(int tickerId, Contract contract,
          int numRows) {
        if (!EClientSocketSimulator.this.dom.contains(tickerId)) {
          EClientSocketSimulator.this.dom.add(tickerId);
        }
        return false;
      }

      @Override
      protected boolean pre_reqRealTimeBars(int tickerId, Contract contract,
          int barSize, String whatToShow, boolean useRTH) {
        if (!EClientSocketSimulator.this.bars.contains(tickerId)) {
          EClientSocketSimulator.this.bars.add(tickerId);
        }
        return false;
      }
    };
  }

  /**
   * Returns true if the queue is empty.
   * @return True if empty.
   */
  public final boolean isEventQueueEmpty() {
  	return eventQueue.isEmpty();
  }

  /**
   * Returns the depth of the event queue.
   * @return The queue depth.
   */
  public final int getQueueDepth() {
    return eventQueue.size();
  }

  public final int getEWrapperInvokes() {
  	return this.eWrapperInvocations;
  }

  public final String getSourceId() {
    return source;
  }
  
  @Override
  public void run() {
  	if (wrapper == null) {
  		throw new IllegalStateException("Missing EWrapper.");
  	}
  	
  	while (running.get()) {
  		// send the event
			TWSProto.Event event = null;
  		try {
  			event = eventQueue.take();
  	  	logger.debug("Sending " + event);
  	  	ApiBuilder builder = ApiMethods.get(event.getMethod().name());
  	  	Pair<Method, Object[]> p = builder.buildArgs(event);
  	  	this.eWrapperInvocations++;
  	  	p.first.invoke(wrapper, p.second);
  		} catch (NullPointerException e) {
  			logger.warn("NullPointerException: event = " + event);
  			running.set(false);
  			return;
  		} catch (Exception e) {
  			logger.warn("Exception while loading data.", e);
  			running.set(false);
  			return;
  		}
  	}
  }
}
