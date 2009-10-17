// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.inject.Inject;
import com.lab616.ib.api.TWSClientManager.Managed;
import com.lab616.ib.api.builders.ContractBuilder;
import com.lab616.ib.api.builders.MarketDataRequestBuilder;
import com.lab616.ib.api.simulator.EClientSocketSimulator;
import com.lab616.ib.api.simulator.EClientSocketSimulator.CSVFileDataSource;
import com.lab616.ib.api.watchers.TWSEventCSVWriter;
import com.lab616.ib.api.watchers.TWSEventProtoWriter;
import com.lab616.omnibus.SystemEvent;
import com.lab616.omnibus.event.AbstractEventWatcher;
import com.lab616.omnibus.event.annotation.Statement;

/**
 *
 *
 * @author david
 *
 */
@Statement("select * from SystemEvent where component='tws'")
public class SystemEventProcessor extends AbstractEventWatcher {

  static Logger logger = Logger.getLogger(SystemEventProcessor.class);

  private final TWSClientManager service;
  
  @Inject
  public SystemEventProcessor(TWSClientManager service) {
    this.service = service;
  }
  
  /**
   * Implements event subscriber.
   * @param event The event.
   */
  public void update(SystemEvent event) {
    if (!event.getComponent().equals("tws")) return;
    try {
      // Ping a connection:
      if ("ping".equals(event.getMethod())) {
        String name = event.getParam("client");
        Long timeout = 1000L; 
        try {
          timeout = Long.decode(event.getParam("timeout"));
        } catch (Exception e) {}
        
        TWSClient client = this.service.getClient(name);
        if (client == null) return;
        if (client.isReady()) {
          logger.info("Pinging connection " + name + " = " + client.isReady() +
              ", currentTime = " + client.ping(timeout, TimeUnit.MILLISECONDS));
        } else {
          logger.info("Pinging connection " + name + " = " + client.isReady() +
              ", currentState = " + client.getState());
        }
        return;
      }
      // Starting a connection:
      if ("start".equals(event.getMethod())) {
        String name = event.getParam("client");
        logger.info("Starting connection " + name + " = " + 
            this.service.newConnection(name));
        return;
      }
      // Stopping a connection:
      if ("stop".equals(event.getMethod())) {
        String name = event.getParam("client");
        logger.info("Stopping connection " + name + " = " +
            this.service.stopConnection(name));
        return;
      }
      // Request market data, including realtime bars.
      if ("mkt".equals(event.getMethod())) {
        final String name = event.getParam("client");
        final String symbol = event.getParam("symbol");
        logger.debug("Requesting market data for " + symbol + " on " + name);
        this.service.enqueue(name, true, new Function<TWSClient, Boolean>() {
          public Boolean apply(TWSClient client) {
            client.requestMarketData(
                new MarketDataRequestBuilder().withDefaultsForStocks()
                .forStock(new ContractBuilder(symbol)));
            return true;
          }
        });
        return;
      }
      // Request market depth:
      if ("dom".equals(event.getMethod())) {
        final String name = event.getParam("client");
        final String symbol = event.getParam("symbol");
        logger.debug("Requesting market depth for " + symbol + " on " + name);
        this.service.enqueue(name, true, new Function<TWSClient, Boolean>() {
          public Boolean apply(TWSClient client) {
            client.requestMarketDepth(
                new MarketDataRequestBuilder().withDefaultsForStocks()
                .forStock(new ContractBuilder(symbol)));
            return true;
          }
        });
        return;
      }
      // Request market depth:
      if ("cancel-dom".equals(event.getMethod())) {
        final String name = event.getParam("client");
        final String symbol = event.getParam("symbol");
        logger.debug("Requesting market depth for " + symbol + " on " + name);
        this.service.enqueue(name, true, new Function<TWSClient, Boolean>() {
          public Boolean apply(TWSClient client) {
            client.cancelMarketDepth(
                new MarketDataRequestBuilder().withDefaultsForStocks()
                .forStock(new ContractBuilder(symbol)));
            return true;
          }
        });
        return;
      }
      // Start CSV file writer
      if ("csv".equals(event.getMethod())) {
        String name = event.getParam("client");
        logger.debug("Starting csv writer for client=" + name);
        // Check to see if we already have a writer for this
        Managed managed = this.service.findAssociatedComponent(name,
            new Predicate<Managed>() {
          public boolean apply(Managed m) {
            return m instanceof TWSEventCSVWriter;
          }
        });
        if (managed == null || !managed.isReady()) {
          this.service.enqueue(name, true, new Function<TWSClient, Managed>() {
            public Managed apply(TWSClient client) {
              TWSEventCSVWriter w = new TWSEventCSVWriter(client.getSourceId());
              client.getEventEngine().add(w);
              return w;
            }
          });
        }
        return;
      }
      // Start proto file writer
      if ("proto".equals(event.getMethod())) {
        String name = event.getParam("client");
        String dir = event.getParam("dir");
        dir = (dir == null || dir.length() == 0) ? "." : dir;
        logger.debug("Starting proto writer for client=" + name);
        // Check to see if we already have a writer for this
        Managed managed = this.service.findAssociatedComponent(name,
            new Predicate<Managed>() {
          public boolean apply(Managed m) {
            return m instanceof TWSEventProtoWriter;
          }
        });
        final String clientName = name;
        final String directory = dir;
        if (managed == null || !managed.isReady()) {
          this.service.enqueue(name, true, new Function<TWSClient, Managed>() {
            public Managed apply(TWSClient client) {
              TWSEventProtoWriter w = 
                new TWSEventProtoWriter(directory, clientName, client.getSourceId());
              client.getEventEngine().add(w);
              return w;
            }
          });
        }
        return;
      }
      // Start simulated data source
      if ("simulate".equals(event.getMethod())) {
        final String name = event.getParam("client");
        final String fname = event.getParam("file");
        logger.debug("Simulating input for client=" + name + " from " + fname);
        // Check to see if we already have a writer for this
        Managed managed = this.service.findAssociatedComponent(name,
            new Predicate<Managed>() {
          public boolean apply(Managed m) {
            return m instanceof EClientSocketSimulator;
          }
        });
        if (managed == null) {
          final EClientSocketSimulator sim = 
            EClientSocketSimulator.getSimulator(name);
          if (sim != null) {
            this.service.enqueue(name, true, new Function<TWSClient, Managed>() {
              public Managed apply(TWSClient client) {
                // Start the file loader
                try {
                  CSVFileDataSource csv = new CSVFileDataSource(fname);
                  sim.addDataSource(csv);
                  return sim;
                } catch (IOException e) {
                  return null;
                }
              }
            });
          }
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
