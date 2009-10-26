// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api;

import java.lang.reflect.Proxy;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import com.ib.client.EClientSocket;
import com.ib.client.EWrapper;
import com.lab616.ib.api.TWSProxy.EWrapperMessage;
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
public class TWSClient {

  @Varz(name = "tws-api-client-connects")
  public static AtomicInteger connects = new AtomicInteger(0);

  @Varz(name = "tws-api-client-disconnects")
  public static AtomicInteger disconnects = new AtomicInteger(0);
  
  static {
    Varzs.export(TWSClient.class);
  }
  
  public interface Factory {
    public TWSClient create(String name, int id);
  }
  
  public enum State {
    INITIALIZED,
    CONNECTED,
    READY,
    NOT_CONNECTED;
  }
  
  static Logger logger = Logger.getLogger(TWSClient.class);
  
  private String name;
  private String host;
  private int port;
  private int clientId;
  private String accountName;
  private int maxRetries;
  private EWrapper wrapper;
  private EventEngine eventEngine;
  private State state;
  private EClientSocket client;
  private ExecutorService executor;
  private String sourceId;
  private Integer nextValidId; // Next valid order id.
  private final TWSBlockingCallManager blockingCalls;
  
  @Inject
  public TWSClient(
      @Named("tws-host") String host, 
      @Named("tws-port") int port,
      @Named("tws-executor") ExecutorService executor,
      @Named("tws-max-retries") int maxRetries,
      EventEngine engine,
      @Assisted String name, 
      @Assisted int id,
      EClientSocketFactory clientFactory) {
    this.name = name;
    this.host = host;
    this.port = port;
    this.clientId = id;
    this.maxRetries = maxRetries;
    this.executor = executor;
    this.eventEngine = engine;
    
    // For implementing blocking calls.
    this.blockingCalls = new TWSBlockingCallManager(this.executor,
        500L, TimeUnit.MILLISECONDS,
        "currentTime", "historicalData", "updateAccountValue",
        "nextValidId");

    this.wrapper = (EWrapper)Proxy.newProxyInstance(
        EWrapper.class.getClassLoader(), 
        new Class[] { EWrapper.class }, 
        new TWSProxy(this, engine) {
          @Override
          public void handleConnectionClosed() {
            onDisconnect();
          }
          @Override
          protected Set<String> synchronousMethods() {
            return blockingCalls.getSynchronousMethods();
          }
          @Override
          protected void handleData(EWrapperMessage event) {
            if ("nextValidId".equals(event.method)) {
              nextValidId = (Integer) event.args[0];
            }
            blockingCalls.handleData(event);
          }
        });
    this.client = clientFactory.create(name, wrapper);
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
    return this.client.isConnected() && this.state == State.READY;
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
    return sourceId;
  }

  public final Integer getNextValidOrderId() {
    return nextValidId;
  }
  
  public String getAccountName() {
    return this.accountName;
  }
  
  private void setSourceId(String accountName) {
    this.accountName = accountName;
    this.sourceId = String.format("%s-%d", accountName, this.clientId);
    logger.info(String.format("Client %s connected to %s as %s.",
        this.name, this.accountName, this.sourceId));
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
    if (state != State.READY || state != State.CONNECTED) {
      Future<Boolean> blocking = this.executor.submit(new Callable<Boolean>() {
        public Boolean call() {
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
            return false;
          }
          logger.info(String.format(
                  "Established connection %s", getSourceId()));
          connects.incrementAndGet();
          state = State.CONNECTED;
          
          // Additional account information.
          String account = requestAccountCode(1000L, TimeUnit.MILLISECONDS);
          logger.info("Got account code = " + account);
          setSourceId(account);
          return true;
        }
      });
      try {
        blocking.get(5000L, TimeUnit.MILLISECONDS);
        if (nextValidId != null) {
          state = State.READY;
        }
        logger.info("Next valid order id = " + nextValidId + 
            ", state =" + state);
        return state == State.READY;
      } catch (InterruptedException e) {
        logger.warn("Interrupted: ", e);
      } catch (ExecutionException e) {
        logger.warn("Execution: ", e);
      } catch (TimeoutException e) {
        logger.warn("Timeout: ", e);
      }
    }
    return false;
  }
  
  /**
   * Disconnects from IB TWS.
   */
  public synchronized boolean disconnect() {
    this.client.eDisconnect();
    state = State.NOT_CONNECTED;
    logger.info(getSourceId() + " disconnected.");
    return true;
  }

  /**
   * Shuts down everything.
   */
  public synchronized boolean shutdown() {
    disconnect();
    logger.info(getSourceId() + " shutdown.");
    return true;
  }

  private void checkReady() {
    if (!isReady()) {
      throw new TWSClientException(String.format(
          "%s not ready: state=%s, connected=%s", 
          getSourceId(), this.state, client.isConnected()));
    }
  }
  
  @SuppressWarnings("unchecked")
  private String requestAccountCode(long timeout, TimeUnit unit) {
    String value = this.blockingCalls.blockingCall("updateAccountValue", 
        timeout, unit, 
        new Function<EWrapperMessage, String>() {
          public String apply(EWrapperMessage event) {
            return (String) event.args[1];
          }
        },
        new Runnable() {
          public void run() {
            client.reqAccountUpdates(true, "");
          }
        },
        new Predicate<EWrapperMessage> () {
          public boolean apply(EWrapperMessage event) {
            boolean matches = event.args.length > 0 &&
            event.args[0] instanceof String &&
            event.args[0].toString().equalsIgnoreCase("AccountCode");
            return matches;
          }
        });
    return value;
  }

  /**
   * Pings the client directly.  Note that this is a blocking call, up to 
   * the given timeout.
   * @param timeout The timeout.
   * @param unit The time unit.
   */
  public DateTime ping(long timeout, TimeUnit unit) {
    checkReady();
    return this.blockingCalls.blockingCall("currentTime", 
        timeout, unit, 
        new Function<EWrapperMessage, DateTime>() {
          public DateTime apply(EWrapperMessage event) {
            return new DateTime((Long) event.args[0] * 1000L);
          }
        },
        new Runnable() {
          public void run() {
            client.reqCurrentTime();
          }
        });
  }
  
  public Iterable<String> requestHistoricalData(MarketDataRequestBuilder builder,
      long timeout, TimeUnit unit) {
    checkReady();
    final MarketDataRequest req = builder.build();
    final int tickerId = req.getTickerId();
    
    return this.blockingCalls.blockingIterable(timeout, unit,
        new Predicate<EWrapperMessage>() {
          public boolean apply(EWrapperMessage event) {
            return "historicalData".equals(event.method) &&
              tickerId == (Integer) event.args[0];
          }
        },
        new Function<EWrapperMessage, String>() {
          public String apply(EWrapperMessage event) {
            return event.toString();
          }
        },
        new Runnable() {
          public void run() {
            DateTime now = new DateTime();
            client.reqHistoricalData(tickerId, req.getContract(), 
                DateTimeFormat.forPattern("yyyyMMdd HH:mm:ss").print(now), 
                "5 D", "1 min", 
                "TRADES", 1, 2);
          }
        });
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


  /**
   * Cancels request for market depth data.
   * @param builder The request builder.
   */
  public void cancelMarketDepth(MarketDataRequestBuilder builder) {
    checkReady();
    MarketDataRequest req = builder.build();
    client.cancelMktDepth(req.getTickerId());
    logger.info(String.format(
        "[%s]: Canceled market depth for %s / id = %d", 
        getSourceId(), req.getContract().m_symbol, req.getTickerId()));
  }
}
