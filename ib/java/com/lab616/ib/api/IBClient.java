// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api;

import java.lang.reflect.Proxy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import com.ib.client.EClientSocket;
import com.ib.client.EWrapper;
import com.lab616.concurrent.AbstractQueueWorker;
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
    DISCONNECTED;
  }
  
  static Logger logger = Logger.getLogger(IBClient.class);

  /**
   * Work queue that needs to block until the connection is established.
   */
  class RequestQueue extends AbstractQueueWorker<Runnable> {
    RequestQueue() {
      super(getSourceId(), false);
    }
    @Override
    protected boolean take() {
      return isReady();
    }
  }
  
  private String name;
  private String host;
  private int port;
  private int clientId;
  private int maxRetries;
  private EWrapper wrapper;
  private EventEngine eventEngine;
  private State state;
  private EClientSocket client;
  private RequestQueue requestQueue;
  private ExecutorService executor;
  
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
          public void handleConnectionClosed() {
            onDisconnect();
          }
        });
    this.client = new EClientSocket(wrapper);
    this.requestQueue = new RequestQueue();
    this.requestQueue.start();
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
    return this.client.isConnected();
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
    state = State.DISCONNECTED;
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
    if (this.state == State.CONNECTED) {
      this.client.eDisconnect();
      state = State.DISCONNECTED;
      return true;
    }
    return false;
  }

  /**
   * Shuts down everything.
   */
  public synchronized boolean shutdown() {
    this.requestQueue.setRunning(false);
    return true;
  }

  /**
   * Requests market data.
   * @param builder The request builder.
   */
  public synchronized void requestMarketData(MarketDataRequestBuilder builder) {
    if (this.state !=  State.CONNECTED) {
      logger.warn(String.format(
          "Cannot request market data - not connected: %s", 
          getSourceId()));
    }
    final MarketDataRequest req = builder.build();
    logger.debug(String.format(
        "Adding request (%s,id=%d) to queue (N=%d) on connection %s" , 
        req.getContract().m_symbol, req.getTickerId(), 
        requestQueue.getQueueDepth(), getSourceId()));
    this.requestQueue.enqueue(new Runnable() {
      public void run() {
        client.reqMktData(
            req.getTickerId(), 
            req.getContract(), 
            req.getGenericTickList(), 
            req.getSnapShot());
        logger.info(String.format(
            "[%s]: Requested market data for %s / id = %d", 
            getSourceId(), req.getContract().m_symbol, req.getTickerId()));
      }
    });
    this.requestQueue.enqueue(new Runnable() {
      public void run() {
        client.reqMktDepth(
            req.getTickerId(), 
            req.getContract(),
            10);
        logger.info(String.format(
            "[%s]: Requested market depth for %s / id = %d", 
            getSourceId(), req.getContract().m_symbol, req.getTickerId()));
      }
    });
    this.requestQueue.enqueue(new Runnable() {
      public void run() {
        client.reqRealTimeBars(
            req.getTickerId(), 
            req.getContract(),
            5, "TRADES", false);
        logger.info(String.format(
            "[%s]: Requested realtime bars for %s / id = %d", 
            getSourceId(), req.getContract().m_symbol, req.getTickerId()));
      }
    });
  }
}
