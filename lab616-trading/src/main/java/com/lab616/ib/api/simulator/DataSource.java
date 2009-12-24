// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api.simulator;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.lab616.ib.api.proto.TWSProto;

/**
 * @author david
 *
 */
public abstract class DataSource implements Runnable {
	
	static Logger logger = Logger.getLogger(DataSource.class);
	
  protected BlockingQueue<TWSProto.Event> sink;
  private AtomicBoolean finished = new AtomicBoolean(false);
  private CountDownLatch start = new CountDownLatch(1);
  private final String resource;
  
  public DataSource(String resource) {
  	this.resource = resource;
  }
  
  /**
   * Sets the sink which is the destination of the events.
   * @param s The sink.
   */
  public final BlockingQueue<TWSProto.Event> setSink(BlockingQueue<TWSProto.Event> s) {
    sink = s;
    return sink;
  }

  /**
   * Creates and sets the sink to the returned blocking queue.
   * @return The blocking queue.
   */
  public final BlockingQueue<TWSProto.Event> createSink() {
  	return setSink(new LinkedBlockingQueue<TWSProto.Event>());
  }
  
  /**
   * Starts loading the data. This can be invoked by a work queue or by an
   * executor.  Nothing happens until the method {@link #start()} has been 
   * invoked and the method will simply block until started and run through 
   * completion.
   */
  public final void run() {
  	if (finished()) {
  		return;
  	}
  	try {
  		start.await();
  	} catch (InterruptedException e) {
  		throw new RuntimeException(String.format(
  				"DataSource[%s] was interrupted and not run.", resource));
  	}
  	try {
  		source(sink);
  	} catch (Exception e) {
  		throw new RuntimeException(e);
  	} finally {
  		finished.set(true);
  		sink = null; // clean up.
  	}
  }
  
  /**
   * Returns whether this data source has done loading the data.
   * @return True if completed.
   */
  public boolean finished() {
    return finished.get();
  }
  
  /**
   * Returns a string representation of the resource.  It can be a filename, a url
   * or some identifier for programmtic data generator.
   * @return The name of the resource.
   */
  public final String getResource() {
  	return this.resource;
  }
  
  /**
   * Starts the data loading process.
   */
  public final void start() {
  	start.countDown();
  }
  
  protected abstract void source(BlockingQueue<TWSProto.Event> sink)
  	throws Exception;
}
