// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.internal.Maps;
import com.google.inject.name.Named;
import com.lab616.ib.api.watchers.IBEventCSVWriter;
import com.lab616.monitoring.Varz;
import com.lab616.monitoring.Varzs;
import com.lab616.omnibus.Main.Shutdown;
import com.lab616.omnibus.event.EventEngine;
import com.lab616.omnibus.http.servlets.StatusServlet;

/**
 * A SystemEvent listener that can control the Api clients.  This provides
 * an EventEngine interface to the IB API clients.
 *
 * @author david
 *
 */
public final class IBService implements Shutdown<Boolean> {

  @Varz(name = "ib-api-started-clients")
  public static AtomicInteger clients = new AtomicInteger(0);
  
  static {
    Varzs.export(StatusServlet.class);
  }

  static Logger logger = Logger.getLogger(IBService.class);

  private final EventEngine eventEngine;
  private final IBClient.Factory factory;
  private final ExecutorService executor;
  private final Map<String, IBClient> apiClients = Maps.newHashMap();
  private final Map<IBClient, IBEventCSVWriter> csvWriters = Maps.newHashMap();
  
  @Inject
  public IBService(EventEngine eventEngine,
      IBClient.Factory factory, 
      @Named("ib-api-executor") ExecutorService executor) {
    this.eventEngine = eventEngine;
    this.factory = factory;
    this.executor = executor;
  }
  
  
  /**
   * Implements Shutdown
   */
  public Boolean call() throws Exception {
    for (IBClient client : apiClients.values()) {
      client.disconnect();
      client.shutdown();
    }
    this.executor.shutdown();
    return true;
  }

  /**
   * Implements Shutdown
   */
  public String getName() {
    return "ib-api-service";
  }

  public int getConnectionCount() {
    return apiClients.size();
  }

  /**
   * Starts a new connection of given name and assigns a connection id.
   * @param name The name of the connection.
   * @return True if successful.
   */
  public synchronized boolean newConnection(String name) {
    int id = apiClients.size(); 
    return newConnection(name, id);
  }
  
  /**
   * Starts a new connection of given name.
   * @param name The name of the connection.
   * @param id The connection id.
   * @return True if successful.
   */
  public synchronized boolean newConnection(String name, int id) {
    if (!apiClients.containsKey(name)) {
      IBClient client = this.factory.create(name, id);
      if (client.connect()) {
        apiClients.put(name, client);
        clients.incrementAndGet();
        return true;
      }
    } else {
      logger.info(String.format("Connection(%s) exists in state = %s",
          name, apiClients.get(name).getState()));
      return apiClients.get(name).connect();
    }
    return false;
  }
  
  /**
   * Stops the connection of the given name.
   * @param name The connection name.
   * @return True if successful.
   */
  public synchronized boolean stopConnection(String name) {
    if (apiClients.containsKey(name)) {
      logger.info("Stopping connection " + name);
      IBClient client = apiClients.get(name);
      client.disconnect();
      clients.decrementAndGet();
      return true;
    } else {
      logger.info(String.format("Connection(%s) does not exist.",
          name));
    }
    return false;
  }
  
  /**
   * Returns a reference to a client.
   * @param name The name of the client.
   * @return The client.
   */
  public IBClient getClient(String name) {
    return apiClients.get(name);
  }

  /**
   * Starts a CSV writer for this connection client.
   */
  public synchronized void startCsvWriter(String clientName) {
    IBClient client = getClient(clientName);
    if (client == null) {
      return;
    }
    
    if (client.isReady() && csvWriters.get(client) != null) {
      IBEventCSVWriter w = new IBEventCSVWriter(client.getSourceId());
      this.eventEngine.add(w);
      this.csvWriters.put(client, w);
    }
  }
  
}
