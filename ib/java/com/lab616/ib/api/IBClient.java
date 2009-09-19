// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import com.ib.client.Contract;
import com.ib.client.EClientSocket;
import com.ib.client.EWrapper;
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
  
  static Logger logger = Logger.getLogger(Client.class);

  private String name;
  private String host;
  private int port;
  private int clientId;
  private EWrapper wrapper;
  private EventEngine eventEngine;
  private State state;
  private EClientSocket client;
  
  @Inject
  public IBClient(
      @Named("ib-api-host") String host, 
      @Named("ib-api-port") int port, 
      EventEngine engine,
      @Assisted String name, 
      @Assisted int id) {
    this.name = name;
    this.host = host;
    this.port = port;
    this.clientId = id;
    this.eventEngine = engine;
    this.wrapper = (EWrapper)Proxy.newProxyInstance(
        EWrapper.class.getClassLoader(), 
        new Class[] { EWrapper.class }, new IBProxy(engine));
    this.state = State.INITIALIZED;
  }

  public final State getState() {
    return this.state;
  }
  
  /**
   * Returns the name of this client.
   * @return The name.
   */
  public final String getName() {
    return this.name;
  }
  
  /**
   * Connects to the IB TWS.  The Api starts a separate thread to read from
   * the socket.
   */
  public synchronized boolean connect() {
    // Start a new thread:
    if (client == null && state != State.CONNECTED) {
      client = new EClientSocket(wrapper);
      client.eConnect(host, port, clientId);
      connects.incrementAndGet();
      state = State.CONNECTED;
      return true;
    }
    return false;
  }
  
  /**
   * Disconnects from IB TWS.
   */
  public synchronized boolean disconnect() {
    if (this.client != null && this.state == State.CONNECTED) {
      this.client.eDisconnect();
      disconnects.incrementAndGet();
      state = State.DISCONNECTED;
      return true;
    }
    return false;
  }
  
  // TODO Revise this to accept some kind of contract builder.
  public synchronized void requestMarketData(String symbol) {
    Contract contract = new Contract();
    contract.m_symbol = symbol;
    contract.m_secType = "STK";
    contract.m_currency = "USD";
    contract.m_exchange = "SMART";
    
    Stock stock = new Stock(symbol);
    client.reqMktData(stock.getTickerId(), contract, 
        "225,221,233", false);
  }
}
