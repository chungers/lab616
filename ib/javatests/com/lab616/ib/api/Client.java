// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.ib.client.Contract;
import com.ib.client.EClientSocket;
import com.ib.client.EWrapper;
import com.lab616.common.flags.Flag;
import com.lab616.common.flags.Flags;
import com.lab616.monitoring.Varzs;
import com.lab616.omnibus.Main;

/**
 * TWS Api client
 *
 * @author david
 */
public class Client implements Runnable {

  @Flag(name = "host", required = true)
  public static String API_HOST;

  @Flag(name = "port", required = true)
  public static Integer API_PORT;

  @Flag(name = "clientId", required = true)
  public static Integer API_CLIENT_ID;

  @Flag(name = "threads")
  public static Integer THREADS = 2;

  @Flag(name = "symbols", required = true)
  public static List<String> SYMBOLS;
  
  @Flag(name = "logLevel")
  public static String logLevel = "INFO";
  
  static Logger logger = Logger.getLogger(Client.class);

  static {
    Flags.register(Client.class);
    Varzs.export(Main.class);
  }
  
  public static CountDownLatch startSignal = new CountDownLatch(1);
  public static CountDownLatch doneSignal = null;
  
  final private String host;
  final private int port;
  final private int clientId;
  final List<String> symbols;
  final EWrapper wrapper;
  
  public Client(String host, int port, int id, List<String> symbols) {
    this.host = host;
    this.port = port;
    this.clientId = id;
    this.symbols = symbols;
    
    this.wrapper = (EWrapper)Proxy.newProxyInstance(
        EWrapper.class.getClassLoader(), 
        new Class[] { EWrapper.class }, new IBProxy(null));
  }
  
  //@Override //JDK1.5
  public void run() {
    if (startSignal != null) {
      try {
        startSignal.await();
      } catch (InterruptedException e) {
        return;
      }
    }
    
    EClientSocket client = new EClientSocket(this.wrapper);
    client.eConnect(host, port, clientId);
    
    for (String symbol : symbols) {
      Contract contract = new Contract();
      contract.m_symbol = symbol;
      contract.m_secType = "STK";
      contract.m_currency = "USD";
      contract.m_exchange = "SMART";
      
      Stock stock = new Stock(symbol);
      client.reqMktData(stock.getTickerId(), contract, 
          "225,221,233", false);
    }
    
    while (client.isConnected()) {
      // loop
    }
    if (doneSignal != null) {
      doneSignal.countDown();
    }
  }


  public static void main(String[] args) throws Exception {
    Main main = new Main() {
      @Override
      public void run() throws Exception {
        logger.info("Running.");
      }
    };
    main.run(args);
  }

  public static void main0(String[] args) throws Exception {
    Flags.parse(args);

    List<List<String>> symbols = Lists.partition(SYMBOLS, THREADS);
    int id = API_CLIENT_ID;
    
    doneSignal = new CountDownLatch(THREADS);
    
    for (List<String> set : symbols) {
      System.out.println("Starting with " + set);
      Client client = new Client(API_HOST, API_PORT, id++, set);
      Thread th = new Thread(client);
      th.setDaemon(true);
      th.start();
    }

    // Tell all to start
    startSignal.countDown();
    
    // Wait for all to complete
    doneSignal.await();
  }
}
