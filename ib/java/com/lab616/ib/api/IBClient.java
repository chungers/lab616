// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import com.ib.client.EClientSocket;
import com.ib.client.EWrapper;
import com.lab616.common.Pair;
import com.lab616.ib.api.builders.MarketDataRequest;
import com.lab616.ib.api.builders.MarketDataRequestBuilder;
import com.lab616.monitoring.Varz;
import com.lab616.monitoring.Varzs;
import com.lab616.omnibus.event.EventEngine;

/**
 * Interactive Brokers API Client.
 *
 * @author david
 *
 */
public class IBClient {

  @Varz(name = "ib-api-client-connects")
  public static AtomicInteger connects = new AtomicInteger(0);

  @Varz(name = "ib-api-client-disconnects")
  public static AtomicInteger disconnects = new AtomicInteger(0);
  
  static {
    Varzs.export(IBClient.class);
  }
  
  public interface Factory {
    public IBClient create(String name, int id);
  }
  
  public enum State {
    INITIALIZED,
    CONNECTED,
    NOT_CONNECTED;
  }
  
  static Logger logger = Logger.getLogger(IBClient.class);
  
  private String name;
  private String host;
  private int port;
  private int clientId;
  private int maxRetries;
  private EWrapper wrapper;
  private EventEngine eventEngine;
  private State state;
  private EClientSocket client;
  private ExecutorService executor;
  
  // Stores the methodName, and a list of api results.
  private Map<String, Set<IBEvent>> apiResults = Maps.newHashMap();
  
  @Inject
  public IBClient(
      @Named("ib-api-host") String host, 
      @Named("ib-api-port") int port,
      @Named("ib-api-executor") ExecutorService executor,
      @Named("ib-api-max-retries") int maxRetries,
      EventEngine engine,
      @Assisted String name, 
      @Assisted int id) {
    this.name = name;
    this.host = host;
    this.port = port;
    this.clientId = id;
    this.maxRetries = maxRetries;
    this.executor = executor;
    this.eventEngine = engine;
    this.wrapper = (EWrapper)Proxy.newProxyInstance(
        EWrapper.class.getClassLoader(), 
        new Class[] { EWrapper.class }, 
        new IBProxy(getSourceId(), engine) {
          @Override
          public void handleConnectionClosed() {
            onDisconnect();
          }
          @Override
          protected String[] synchronousMethods() {
            return new String[] {"currentTime"};
          }
          @Override
          protected void handleData(IBEvent event) {
            synchronized (apiResults) {
              if (apiResults.get(event.getMethod()) == null) {
                Set<IBEvent> list = Sets.newTreeSet();
                apiResults.put(event.getMethod(), list);
              }
              apiResults.get(event.getMethod()).add(event);
            }
          }
        });
    this.client = new EClientSocket(wrapper);
    this.state = State.INITIALIZED;
  }

  public final State getState() {
    return this.state;
  }
  
  /**
   * Returns if the client is ready to accept requests.
   * @return True if connected and ready.
   */
  public final Boolean isReady() {
    return this.client.isConnected() && this.state == State.CONNECTED;
  }

  /**
   * Returns the associated event engine.
   * @return The engine.
   */
  public final EventEngine getEventEngine() {
    return this.eventEngine;
  }
  
  /**
   * Returns the name of this client.
   * @return The name.
   */
  public final String getName() {
    return this.name;
  }

  public final String getSourceId() {
    return String.format("ibConnection@%s:%d{name=%s,id=%d}", 
        host, port, name, clientId);
  }
  
  public synchronized void onDisconnect() {
    logger.info(String.format("Disconnected: %s", getSourceId()));
    client.eDisconnect();
    int tries = 0;
    while (client.isConnected() && tries++ < maxRetries/4) {
      try {
        logger.info(String.format(
            "Still connected: %s@%s:%d[%d]", 
            name, host, port, clientId));
        Thread.sleep(1000L);
      } catch (InterruptedException e) {
        //
      }
    }
    disconnects.incrementAndGet();
    state = State.NOT_CONNECTED;
  }
  
  /**
   * Connects to the IB TWS.  The Api starts a separate thread to read from
   * the socket.
   */
  public synchronized boolean connect() {
    if (state != State.CONNECTED) {
      this.executor.submit(new Runnable() {
        public void run() {
          client.eConnect(host, port, clientId);
          
          int tries = 0;
          while (!client.isConnected() && tries++ < maxRetries) {
            try {
              logger.info(String.format(
                  "[%d] Waiting to establish connection %s", 
                  tries, getSourceId()));
              Thread.sleep(1000L);
              if (!client.isConnected()) {
                // Try again.
                client.eConnect(host, port, clientId);
              } else {
                break;
              }
            } catch (InterruptedException e) {
              logger.warn(e);
            }
          } 
          if (tries >= maxRetries) {
            logger.warn("Retries exceeded for connection " + getSourceId());
            return;
          }
          logger.info(String.format(
                  "Established connection %s", getSourceId()));
          connects.incrementAndGet();
          state = State.CONNECTED;
        }
      });
      return true;
    }
    return false;
  }
  
  /**
   * Disconnects from IB TWS.
   */
  public synchronized boolean disconnect() {
    this.client.eDisconnect();
    state = State.NOT_CONNECTED;
    return true;
  }

  /**
   * Shuts down everything.
   */
  public synchronized boolean shutdown() {
    disconnect();
    return true;
  }

  private void checkReady() {
    if (!isReady()) {
      throw new IBClientException(String.format(
          "%s not ready: state=%s, connected=%s", 
          getSourceId(), this.state, client.isConnected()));
    }
  }
  
  /**
   * Pings the client directly.
   */
  public DateTime ping() {
    client.reqCurrentTime();
    Callable<DateTime> getResult = new Callable<DateTime>() {
      public DateTime call() {
        while (true) {
          Set<IBEvent> l = apiResults.get("currentTime");
          if (l != null && l.size() > 0) {
            for (IBEvent e : l) {
              l.remove(e);
              return new DateTime((Long)e.getArgs()[0]);
            }
          }
        }
      }
    };
    Future<DateTime> future = new FutureTask<DateTime>(getResult);
    this.executor.submit(getResult);
    try {
      return future.get(60L, TimeUnit.SECONDS);
    } catch (Exception e) {
      throw new IBClientException(e);
    }
  }
  
  /**
   * Requests market data: tick and realtime bars.
   * @param builder The request builder.
   */
  public void requestMarketData(MarketDataRequestBuilder builder) {
    checkReady();
    MarketDataRequest req = builder.build();
    client.reqMktData(
        req.getTickerId(), 
        req.getContract(), 
        req.getGenericTickList(), 
        req.getSnapShot());
    logger.info(String.format(
        "[%s]: Requested market data for %s / id = %d", 
        getSourceId(), req.getContract().m_symbol, req.getTickerId()));
    client.reqRealTimeBars(
        req.getTickerId(), 
        req.getContract(),
        5, "TRADES", false);
    logger.info(String.format(
        "[%s]: Requested realtime bars for %s / id = %d", 
        getSourceId(), req.getContract().m_symbol, req.getTickerId()));
  }
  
  /**
   * Requests market depth.
   * @param builder The request builder.
   */
  public void requestMarketDepth(MarketDataRequestBuilder builder) {
    checkReady();
    MarketDataRequest req = builder.build();
    client.reqMktDepth(
        req.getTickerId(), 
        req.getContract(),
        10);
    logger.info(String.format(
        "[%s]: Requested market depth for %s / id = %d", 
        getSourceId(), req.getContract().m_symbol, req.getTickerId()));
  }
}
