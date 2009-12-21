// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api.simulator;

import java.util.concurrent.BlockingQueue;

import com.lab616.ib.api.proto.TWSProto;

/**
 * @author david
 *
 */
abstract class DataSource implements Runnable {
  protected BlockingQueue<TWSProto.Event> sink;
  private boolean finished;
  
  public void setSink(BlockingQueue<TWSProto.Event> s) {
    sink = s;
  }

  public final void run() {
    try {
      source(sink);
    } catch (Exception e) {
    	throw new RuntimeException(e);
    }
    finished = true;
  }
  
  public boolean finished() {
    return finished;
  }
  
  abstract void source(BlockingQueue<TWSProto.Event> sink) throws Exception;
}
