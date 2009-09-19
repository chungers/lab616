// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.lab616.omnibus.SystemEvent;
import com.lab616.omnibus.event.AbstractEventWatcher;
import com.lab616.omnibus.event.annotation.Statement;

/**
 *
 *
 * @author david
 *
 */
@Statement("select * from SystemEvent where component='ib-api'")
public class IBSystemEventWatcher extends AbstractEventWatcher {

  static Logger logger = Logger.getLogger(IBSystemEventWatcher.class);

  private final IBService service;
  
  @Inject
  public IBSystemEventWatcher(IBService service) {
    this.service = service;
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
        logger.info("Starting connection " + name + 
            this.service.newConnection(name));
        return;
      }
      // Stopping a connection:
      if ("stop".equals(event.getMethod())) {
        String name = event.getParam("name");
        logger.info("Stopping connection " + name + 
            this.service.stopConnection(name));
        return;
      }
      // Request market data:
      if ("mkt".equals(event.getMethod())) {
        String name = event.getParam("name");
        String symbol = event.getParam("symbol");
        
        IBClient client = this.service.getClient(name);
        if (client != null) {
          logger.info("Request market data " + symbol + " from " + name);
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
