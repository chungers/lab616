// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api;

import java.lang.reflect.Proxy;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
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
import com.lab616.ib.api.TWSConnectionProfileManager.HostPort;
import com.lab616.ib.api.builders.MarketDataRequest;
import com.lab616.ib.api.builders.MarketDataRequestBuilder;
import com.lab616.monitoring.Varz;
import com.lab616.monitoring.Varzs;
import com.lab616.omnibus.event.EventEngine;
import com.lab616.omnibus.event.EventMessage;

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
    public TWSClient create(String profile, int id, boolean simulate);
  }
  
  public enum State {
    INITIALIZED,
    CONNECTED,
    READY,
    NOT_CONNECTED;
  }
  
  static Logger logger = Logger.getLogger(TWSClient.class);
  
  private String profile;
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
  private final TWSConnectionProfileManager profiles;
  private final HostPort hostPort;
  private CountDownLatch accountReady;
  @Inject
  public TWSClient(
      TWSConnectionProfileManager profiles,
      @Named("tws-executor") ExecutorService executor,
      @Named("tws-max-retries") int maxRetries,
      EventEngine engine,
      @Assisted String profile, 
      @Assisted int id,
      @Assisted boolean simulate,
      EClientSocketFactory clientFactory) {
    this.profiles = profiles;
    this.profile = profile;
    this.clientId = id;
    this.maxRetries = maxRetries;
    this.executor = executor;
    this.eventEngine = engine;
    this.hostPort = this.profiles.getHostPort(profile);
    
    // For implementing blocking calls.
    this.blockingCalls = new TWSBlockingCallManager(this.executor,
        5L, TimeUnit.SECONDS,
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
          protected void handleData(EventMessage event) {
            if ("nextValidId".equals(event.method)) {
              onNextValidId((Integer) event.args[0]);
              return;
            }
            if ("updateAccountValue".equals(event.method)) {
              onUpdateAccountValue(event.args);
            }
            blockingCalls.handleData(event);
          }
        });
    this.client = clientFactory.create(profile, wrapper);
    this.state = State.INITIALIZED;
  }

  private void onNextValidId(int id) {
      this.nextValidId = id;
      if (this.state == State.CONNECTED) {
        this.accountReady.countDown();
      }
  }
  
  private void onUpdateAccountValue(Object[] args) {
    // Check for the account code.
    if (args.length > 0 && args[0] != null) {
      if ("AccountCode".equals((String) args[0])) {
        // Get the account code
        String v = (String) args[1];
        if (v != null && v.length() > 0) {
          this.accountName = v;
          this.sourceId = String.format("%s-%d", accountName, this.clientId);
          logger.info(String.format(
              "Client %s connected to %s as %s.",
              this.profile, this.accountName, this.sourceId));
          if (this.state == State.CONNECTED) {
            accountReady.countDown();
          }
        }
      }
    }
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
  
  public final String getId() {
    return String.format("[%s-%d@%s]", 
        this.profile, this.clientId, this.hostPort);
  }
  /**
   * Returns the name of this client.
   * @return The name.
   */
  public final String getProfile() {
    return this.profile;
  }

  public final String getSourceId() {
    return (state == State.READY) ? sourceId : getId();
  }

  public final Integer getNextValidOrderId() {
    return nextValidId;
  }
  
  public String getAccountName() {
    return this.accountName;
  }
  
  public synchronized void onDisconnect() {
    logger.info(String.format("Disconnected: %s", getSourceId()));
    client.eDisconnect();
    int tries = 0;
    while (client.isConnected() && tries++ < maxRetries/4) {
      try {
        logger.info(String.format(
            "Still connected: %s@%s:%d[%d]", 
            profile, hostPort.host, hostPort.port, clientId));
        Thread.sleep(1000L);
      } catch (InterruptedException e) {
        //
      }
    }
    disconnects.incrementAndGet();
    state = State.NOT_CONNECTED;
  }
  
  private boolean doRetriesIfNecessary(){
    int tries = 0;
    while (!client.isConnected() && tries++ < maxRetries) {
      try {
        logger.info(String.format(
            "[%d] Waiting to establish connection %s", tries, getId()));
        Thread.sleep(1000L);
        if (!client.isConnected()) {
          // Try again.
          client.eConnect(hostPort.host, hostPort.port, clientId);
        } else {
          break;
        }
      } catch (InterruptedException e) {
        logger.warn(e);
      }
    } 
    if (tries >= maxRetries) {
      logger.warn("Retries exceeded for connection " + getId());
      return false;
    }
    return client.isConnected();
  }
  
  /**
   * Connects to the IB TWS.  The Api starts a separate thread to read from
   * the socket.
   */
  public boolean connect() {
    if (state != State.READY || state != State.CONNECTED) {
      // Set up the latch.
      synchronized (this) {
        accountReady = new CountDownLatch(2);
      }
      
      // Connect, retry if necessary.
      client.eConnect(hostPort.host, hostPort.port, clientId);
      boolean connected = doRetriesIfNecessary();
      
      if (connected) {
        state = State.CONNECTED;
        connects.incrementAndGet();
        
        client.reqAccountUpdates(true, "");

        // Here we need to block to get certain account related information.
        // Without this information, the connection may as well be bad.
        try {
          logger.info("Waiting for account data: " + getId());
          accountReady.await();
          state = State.READY;
          logger.info(String.format(
              "%s: state=%s, sourceId=%s, accountName=%s, nextValidOrderId=%d",
              getId(), this.state, 
              getSourceId(), getAccountName(), getNextValidOrderId()));
          return true;
        } catch (InterruptedException e) {
          logger.fatal("Interrupted while getting account data: " + getId());
          return false;
        }
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
    logger.info(getId() + " shutdown.");
    return true;
  }

  private void checkReady() {
    if (!isReady()) {
      throw new TWSClientException(String.format(
          "%s not ready: state=%s, connected=%s", 
          getId(), this.state, client.isConnected()));
    }
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
        new Function<EventMessage, DateTime>() {
          public DateTime apply(EventMessage event) {
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
        new Predicate<EventMessage>() {
          public boolean apply(EventMessage event) {
            return "historicalData".equals(event.method) &&
              tickerId == (Integer) event.args[0];
          }
        },
        new Function<EventMessage, String>() {
          public String apply(EventMessage event) {
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
   * Requests market data: tick data only.
   * @param builder The request builder.
   */
  public void requestTickData(MarketDataRequestBuilder builder) {
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
  }
  
  /**
   * Requests market data: tick data only.
   * @param builder The request builder.
   */
  public void requestRealtimeBars(MarketDataRequestBuilder builder) {
    checkReady();
    MarketDataRequest req = builder.build();
    client.reqRealTimeBars(
        req.getTickerId(), 
        req.getContract(),
        req.getBarSize(), req.getBarType(), req.getRegularTradingHours());
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
