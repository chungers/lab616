// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api.watchers;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.lab616.concurrent.AbstractQueueWorker;
import com.lab616.ib.api.TWSClientException;
import com.lab616.ib.api.TWSEvent;
import com.lab616.ib.api.TWSClientManager.Managed;
import com.lab616.ib.api.util.ProtoDataFile;
import com.lab616.omnibus.event.AbstractEventWatcher;
import com.lab616.omnibus.event.annotation.Statement;
import com.lab616.omnibus.event.annotation.Var;

/**
 * Simple CSV writer that works off a queue and continuously appends to 
 * a file whose name is computed based on the current date.
 *
 * @author david
 *
 */
@Statement("select * from TWSEvent where source=?")
public class TWSEventProtoWriter extends AbstractEventWatcher implements Managed {

  static Logger logger = Logger.getLogger(TWSEventProtoWriter.class);

  private ProtoDataFile protoFile;
  private String clientSourceId;
  private AbstractQueueWorker<TWSEvent> queueWorker;
  
  public TWSEventProtoWriter(String dir, String rootName, String clientSourceId) {
    this.clientSourceId = clientSourceId;
    try {
      this.protoFile = new ProtoDataFile(dir, rootName);
      this.protoFile.getWriter();
    } catch (IOException e) {
      throw new TWSClientException(e);
    }
    final String id = clientSourceId;
    this.queueWorker = new AbstractQueueWorker<TWSEvent>(clientSourceId, false) {
      @Override
      protected void execute(TWSEvent event) throws Exception {
        protoFile.getWriter().write(event);
      }
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
        logger.info("Started proto writer @" + id);
        return true;
      }
      @Override
      protected void onStop(int queueSize) {
        logger.info("Stopped proto writer @" + id + " at queue=" + queueSize);
        try {
          protoFile.getWriter().close();
        } catch (Exception e) {
          logger.warn("Exception", e);
        }
      }
    };
    this.queueWorker.start();
  }
  
  @Var(1)
  public String getSourceId() {
    return this.clientSourceId;
  }
  
  /**
   * Receives the IBEvent from the event engine.
   * @param event The event.
   */
  public void update(TWSEvent event) {
    if (event != null) {
      this.queueWorker.enqueue(event);
    }
  }

  /**
   * Returns if the output writer is ready (open, no errors).
   */
  public boolean isReady() {
    try {
      return this.protoFile.getWriter() != null;
    } catch (IOException e) {
      return false;
    }
  }
  
  /**
   * Stop this writer.
   */
  public void stopRunning() {
    super.stop();
    this.queueWorker.setRunning(false);
  }
}
