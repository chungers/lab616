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
import com.lab616.ib.api.ApiBuilder;
import com.lab616.ib.api.ApiMethods;
import com.lab616.ib.api.ManagedEClientSocket;
import com.lab616.ib.api.TWSClientManager.Managed;
import com.lab616.ib.api.proto.TWSProto;

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

  // TWSProto.Events to be sent to the EWrapper.
  private BlockingQueue<TWSProto.Event> eventQueue = 
    new LinkedBlockingQueue<TWSProto.Event>(10);
  
  public static abstract class DataSource implements Runnable {
    protected BlockingQueue<TWSProto.Event> sink;
    private boolean finished;
    
    public void setSink(BlockingQueue<TWSProto.Event> s) {
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
    
    abstract void source(BlockingQueue<TWSProto.Event> sink) throws Exception;
  }
  
  public static class CSVFileDataSource extends DataSource {
    LineNumberReader reader;
    String source;
    public CSVFileDataSource(String filename) throws IOException {
      source = filename;
      logger.info("Reading from " + filename);
      reader = new LineNumberReader(new FileReader(filename));
    }
    protected void source(BlockingQueue<TWSProto.Event> sink) throws Exception {
      logger.info("Writing to sink: " + sink);
      String line;
      while ((line = reader.readLine()) != null) {
        if (!line.startsWith("#")) {
          String[] cols = line.split(",");
          ApiBuilder b = ApiMethods.get(cols[1]);
          if (b != null) {
            TWSProto.Event event = b.buildProto(source, cols);
            sink.add(event); 
          }
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
          TWSProto.Event event1, event2;
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
  
  private void send(TWSProto.Event event) {
    System.out.println("Sending event = " + event);
    System.out.flush();
  }

}
