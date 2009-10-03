// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.inject.Inject;
import com.google.inject.internal.Lists;
import com.google.inject.internal.Maps;
import com.google.inject.name.Named;
import com.lab616.concurrent.AbstractQueueWorker;
import com.lab616.monitoring.Varz;
import com.lab616.monitoring.Varzs;
import com.lab616.omnibus.Main.Shutdown;
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

  /**
   * Objects to be managed by the service. 
   */
  public interface Managed {
    public boolean isReady();
    public void stop();
  }
  
  /**
   * Queue for work that depends on the client being properly connected.
   */
  class IBClientQueue extends AbstractQueueWorker<Function<IBClient, ?>> {
    IBClient client;
    IBClientQueue(IBClient c) {
      super(c.getSourceId(), false);
      client = c;
    }
    
    @Override
    protected boolean take() {
      return client.isReady();
    }
    
    @Override
    protected void execute(Function<IBClient, ?> work) throws Exception {
      Object result = work.apply(client);
      // Managed objects.
      if (result instanceof Managed) {
        if (managed.get(client) == null) {
          List<Managed> list = Lists.newArrayList();
          managed.put(client, list);
        }
        managed.get(client).add((Managed) result);
      }
    }
  }
  
  private final IBClient.Factory factory;
  private final ExecutorService executor;
  private final Map<String, IBClient> apiClients = Maps.newHashMap();
  private final Map<IBClient, List<Managed>> managed = Maps.newHashMap();
  private final Map<IBClient, IBClientQueue> clientQueues = Maps.newHashMap();
  
  @Inject
  public IBService(IBClient.Factory factory, 
      @Named("ib-api-executor") ExecutorService executor) {
    this.factory = factory;
    this.executor = executor;
  }
  
  /**
   * Searches through a list of managed components associated with a client,
   * for example, a CSV writer, and return the first component that matches
   * the provided filter.
   * @param name Name of the client.
   * @param cond The filter condition.
   * @return The first match.
   */
  public Managed findAssociatedComponent(String name, Predicate<Managed> cond) {
    if (getClient(name) != null && managed.get(getClient(name)) != null) {
      for (Managed comp : managed.get(getClient(name))) {
        if (cond.apply(comp)) {
          return comp;
        }
      }
    }
    return null;
  }
  
  /**
   * Implements Shutdown
   */
  public Boolean call() throws Exception {
    for (IBClient client : apiClients.values()) {
      // Stop the work queue.
      IBClientQueue q = clientQueues.get(client);
      if (q != null) {
        q.setRunning(false);
      }
      // Stop the managed objects for this client.
      if (managed.get(client) != null) {
        for (Managed m : managed.get(client)) {
          try {
            m.stop();
          } catch (Exception e) {
            logger.warn("Exception while trying to stop: ", e);
          }
        }
      }
      // Now shutdown the client connection.
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
      apiClients.put(name, client);
      clients.incrementAndGet();
      clientQueues.put(client, new IBClientQueue(client));
      clientQueues.get(client).start();
      return client.connect();
    } else {
      logger.info(String.format("Connection(%s) exists in state = %s",
          name, apiClients.get(name).getState()));
      return apiClients.get(name).connect();
    }
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
   * Enqueue a request for the named client connection.  Starts the connection
   * on demand if client is not already running.
   * 
   * @param <V> An object type.  Special Managed object can be shutdown by service.
   * @param name The client name.
   * @param startClientOnDemand True to start client if not running.
   * @param work Unit of work to be enqueued.
   */
  public <V> void enqueue(String name, boolean startClientOnDemand, 
      Function<IBClient, V> work) {
    if (getClient(name) == null && startClientOnDemand) {
      newConnection(name);
    }
    IBClientQueue q= clientQueues.get(getClient(name));
    q.enqueue(work);
  }
}
