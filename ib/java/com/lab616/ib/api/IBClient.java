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
  
  static Logger logger = Logger.getLogger(Client.class);

  private String name;
  private String host;
  private int port;
  private int clientId;
  private EWrapper wrapper;
  private EventEngine eventEngine;
  
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
  public synchronized void connect() {
    // Start a new thread:
    if (client == null) {
      client = new EClientSocket(wrapper);
      client.eConnect(host, port, clientId);
      connects.incrementAndGet();
    }
  }
  
  /**
   * Disconnects from IB TWS.
   */
  public synchronized void disconnect() {
    if (this.client != null) {
      this.client.eDisconnect();
      disconnects.incrementAndGet();
    }
  }
  
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
