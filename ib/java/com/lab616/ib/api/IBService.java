// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.internal.Maps;
import com.lab616.monitoring.Varz;
import com.lab616.monitoring.Varzs;
import com.lab616.omnibus.SystemEvent;
import com.lab616.omnibus.Main.Shutdown;
import com.lab616.omnibus.event.AbstractEventWatcher;
import com.lab616.omnibus.event.annotation.Statement;
import com.lab616.omnibus.http.servlets.StatusServlet;

/**
 * A SystemEvent listener that can control the Api clients.  This provides
 * an EventEngine interface to the IB API clients.
 *
 * @author david
 *
 */
@Statement("select * from SystemEvent where component='ib-api'")
public class IBService extends AbstractEventWatcher 
  implements Shutdown<Boolean> {

  @Varz(name = "ib-api-started-clients")
  public static AtomicInteger clients = new AtomicInteger(0);
  
  static {
    Varzs.export(StatusServlet.class);
  }

  static Logger logger = Logger.getLogger(Client.class);

  private final IBClient.Factory factory;
  private final Map<String, IBClient> apiClients = Maps.newHashMap();
  
  @Inject
  public IBService(IBClient.Factory factory) {
    this.factory = factory;
  }
  
  
  /**
   * Implements Shutdown
   */
  public Boolean call() throws Exception {
    for (IBClient client : apiClients.values()) {
      client.disconnect();
    }
    return true;
  }

  /**
   * Implements Shutdown
   */
  public String getName() {
    return "ib-api-service";
  }

  /**
   * Implements event subscriber.
   * @param event The event.
   */
  public void update(SystemEvent event) {
    if (!event.getComponent().equals("ib-api")) return;
    logger.info("Received event " + event);
    
    try {
      // Starting a connection:
      if ("start".equals(event.getMethod())) {
        String name = event.getParam("name");
        logger.info("Starting connection " + name);
        synchronized (apiClients) {
          if (!apiClients.containsKey(name)) {
            int id = apiClients.size();
            IBClient client = this.factory.create(name, id);
            client.connect();
            apiClients.put(name, client);
            clients.incrementAndGet();
          }
        }
        return;
      }
      // Stopping a connection:
      if ("stop".equals(event.getMethod())) {
        String name = event.getParam("name");
 
        synchronized (apiClients) {
          if (apiClients.containsKey(name)) {
            logger.info("Stopping connection " + name);
            IBClient client = apiClients.get(name);
            client.disconnect();
            clients.decrementAndGet();
          }
        }
        return;
      }
      // Request market data:
      if ("mkt".equals(event.getMethod())) {
        String name = event.getParam("name");
        String symbol = event.getParam("symbol");
 
        if (apiClients.containsKey(name)) {
          logger.info("Request market data " + symbol + " from " + name);
          IBClient client = apiClients.get(name);
          client.requestMarketData(symbol);
        }
        return;
      }
    } catch (Exception e) {
      logger.error("Error while handling request " + event, e);
      SystemEvent error = new SystemEvent()
        .setComponent("error")
        .setMethod("log")
        .setParam("original-request", event.toString());
      post(error);
    }
  }
}
