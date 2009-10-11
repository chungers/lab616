// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api.simulator;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.google.inject.internal.Maps;
import com.google.inject.internal.Sets;
import com.ib.client.Contract;
import com.ib.client.EClientSocket;
import com.ib.client.EWrapper;
import com.lab616.concurrent.AbstractQueueWorker;
import com.lab616.ib.api.ManagedEClientSocket;
import com.lab616.ib.api.TWSEvent;
import com.lab616.ib.api.TWSClientManager.Managed;

/**
 *
 *
 * @author david
 *
 */
public class EClientSocketSimulator extends Thread implements Managed {

  static Logger logger = Logger.getLogger(EClientSocketSimulator.class);
  
  private static Map<String, EClientSocketSimulator> simulators =
    Maps.newHashMap();
  
  public static EClientSocketSimulator getSimulator(String name) {
    return simulators.get(name);
  }
    
  private int clientId;
  private Set<Integer> ticks = Sets.newHashSet();
  private Set<Integer> bars = Sets.newHashSet();
  private Set<Integer> dom = Sets.newHashSet();
  private AtomicBoolean running = new AtomicBoolean(true);
  private AbstractQueueWorker<DataSource> dataSources;

  // TWSEvents to be sent to the EWrapper.
  private BlockingQueue<TWSEvent> eventQueue = 
    new LinkedBlockingQueue<TWSEvent>(10);
  
  public static abstract class DataSource implements Runnable {
    protected BlockingQueue<TWSEvent> sink;
    private boolean finished;
    
    public void setSink(BlockingQueue<TWSEvent> s) {
      sink = s;
    }

    public final void run() {
      try {
        source(sink);
      } catch (Exception e) {
      }
      finished = true;
    }
    
    public boolean finished() {
      return finished;
    }
    
    abstract void source(BlockingQueue<TWSEvent> sink) throws Exception;
  }
  
  public static class CSVFileDataSource extends DataSource {
    LineNumberReader reader;
    public CSVFileDataSource(String filename) throws IOException {
      logger.info("Reading from " + filename);
      reader = new LineNumberReader(new FileReader(filename));
    }
    protected void source(BlockingQueue<TWSEvent> sink) throws Exception {
      logger.info("Writing to sink: " + sink);
      String line;
      while ((line = reader.readLine()) != null) {
        if (!line.startsWith("#")) {
          TWSEvent event = new TWSEvent();
          event.copyFrom(line);
          sink.add(event);
        }
      }
      reader.close();
    }
  }
  
  
  // Executor for running the data source.
  private EWrapper wrapper;
  private String clientName;
  
  public EClientSocketSimulator(String name) {
    clientName = name;
    super.setName(getClass().getSimpleName() + "-" + name);
    simulators.put(name, this);
    this.dataSources = new AbstractQueueWorker<DataSource>(name, false) {
      @Override
      protected boolean handleException(Exception e) {
        if (e instanceof IOException) {
          return false;
        } else {
          return true;
        }
      }
      @Override
      protected boolean onStart() {
        logger.info("Started simulator @" + clientName);
        return true;
      }
      @Override
      protected void onStop(int queueSize) {
        logger.info("Stopped simulator @" + clientName + ", depth=" + queueSize);
      }
    };
    this.dataSources.start();
  }
  
  public boolean isReady() {
    return this.wrapper != null;
  }
  
  public void addDataSource(DataSource ds) {
    ds.setSink(this.eventQueue);
    this.dataSources.enqueue(ds);
  }
  
  public void stopRunning() {
    running.set(false);
  }
  
  public String getClientName() {
    return clientName;
  }

  public Set<Integer> getTickerIdsForMkData() {
    return ticks;
  }
  
  public Set<Integer> getTickerIdsForRealtimeBars() {
    return bars;
  }
  
  public Set<Integer> getTickerIdsForMkDepth() {
    return dom;
  }

  public Integer getClientId() {
    return clientId;
  }
  
  public EClientSocket create(final EWrapper wrapper) {
    this.wrapper = wrapper;
    return new ManagedEClientSocket(wrapper) {

      boolean isConnected = true;
      
      @Override
      public boolean isConnected() {
        return isConnected;
      }

      @Override
      protected boolean pre_eConnect(Socket socket, int clientId)
          throws IOException {
        EClientSocketSimulator.this.clientId = clientId;
        return false;
      }

      @Override
      protected boolean pre_eConnect(String host, int port, int clientId) {
        return false;
      }

      @Override
      protected boolean pre_eDisconnect() {
        EClientSocketSimulator.this.running.set(false);
        isConnected = false;
        return false;
      }

      @Override
      protected boolean pre_reqMktData(int tickerId, Contract contract,
          String genericTickList, boolean snapshot) {
        if (!EClientSocketSimulator.this.ticks.contains(tickerId)) {
          EClientSocketSimulator.this.ticks.add(tickerId);
        }
        return false;
      }

      @Override
      protected boolean pre_reqMktDepth(int tickerId, Contract contract,
          int numRows) {
        if (!EClientSocketSimulator.this.dom.contains(tickerId)) {
          EClientSocketSimulator.this.dom.add(tickerId);
        }
        return false;
      }

      @Override
      protected boolean pre_reqRealTimeBars(int tickerId, Contract contract,
          int barSize, String whatToShow, boolean useRTH) {
        if (!EClientSocketSimulator.this.bars.contains(tickerId)) {
          EClientSocketSimulator.this.bars.add(tickerId);
        }
        return false;
      }
    };
  }
  
  @Override
  public void run() {
    while (running.get()) {
      if (wrapper != null) {
        // send the event
        try {
          TWSEvent event1, event2;
          long t1;
          
          event1 = eventQueue.take();
          // put the first event
          send(event1);
          t1 = System.nanoTime();

          while (running.get()) {
            event2 = eventQueue.take();
            
            long expected = 
              (event2.getTimestamp() - event1.getTimestamp()) * 1000L;

            /*
            long l = 0L;
            while ((l = System.nanoTime() - t1) < expected) { 
              logger.info("dt = " + expected + ", wait=" + l);
            } */
            
            send(event2);
            event1 = event2;
            t1 = System.nanoTime();
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }
  
  private void send(TWSEvent event) {
    System.out.println("Sending event = " + event);
    System.out.flush();
  }

}
