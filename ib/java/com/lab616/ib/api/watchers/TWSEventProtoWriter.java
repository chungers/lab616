// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api.watchers;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.lab616.concurrent.AbstractQueueWorker;
import com.lab616.ib.api.ApiBuilder;
import com.lab616.ib.api.ApiMethods;
import com.lab616.ib.api.TWSClientException;
import com.lab616.ib.api.TWSClientManager.Managed;
import com.lab616.ib.api.TWSProxy.EWrapperMessage;
import com.lab616.ib.api.proto.TWSProto;
import com.lab616.ib.api.util.ProtoDataFile;
import com.lab616.monitoring.MinMaxAverage;
import com.lab616.monitoring.Varz;
import com.lab616.monitoring.VarzMap;
import com.lab616.monitoring.Varzs;
import com.lab616.omnibus.event.AbstractEventWatcher;
import com.lab616.omnibus.event.annotation.Statement;
import com.lab616.omnibus.event.annotation.Var;
import com.lab616.util.Time;

/**
 * Simple CSV writer that works off a queue and continuously appends to 
 * a file whose name is computed based on the current date.
 *
 * @author david
 *
 */
@Statement("select payload from EventMessage where source=?")
public class TWSEventProtoWriter extends AbstractEventWatcher implements Managed {

  @Varz(name = "tws-event-proto-writer-subscriber-elapsed")
  public static final Map<String, MinMaxAverage> subscriberElapsed = 
    VarzMap.create(MinMaxAverage.class);

  static {
    Varzs.export(TWSEventProtoWriter.class);
  }
  
  static Logger logger = Logger.getLogger(TWSEventProtoWriter.class);

  private ProtoDataFile protoFile;
  private String clientSourceId;
  private AbstractQueueWorker<EWrapperMessage> queueWorker;
  
  public TWSEventProtoWriter(String dir, String rootName, String clientSourceId) {
    this.clientSourceId = clientSourceId;
    try {
      this.protoFile = new ProtoDataFile(dir, clientSourceId);
      this.protoFile.getWriter();
    } catch (IOException e) {
      throw new TWSClientException(e);
    }
    final String id = clientSourceId;
    this.queueWorker = new AbstractQueueWorker<EWrapperMessage>(id, false) {
      @Override
      protected void execute(EWrapperMessage event) throws Exception {
        // Modify the source id to use only the account name.
        ApiBuilder b = ApiMethods.get(event.method);
        if (b != null) {
          TWSProto.Event proto = b.buildProto(
              event.source, event.timestamp, event.args);
          protoFile.getWriter().write(proto);
        }
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
  public void update(Object event) {
    if (event != null && event instanceof EWrapperMessage) {
      long start = Time.now();
      this.queueWorker.enqueue((EWrapperMessage) event);
      subscriberElapsed.get(getSourceId()).set(Time.now() - start);
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
    try {
      super.stop();
    } catch (Exception e) {
      logger.warn("Problem while stopping:", e);
    }
    logger.info("Stopping work queue:" + getSourceId());
    this.queueWorker.setRunning(false);
    try {
      this.queueWorker.waitForStop(5L, TimeUnit.SECONDS);
      logger.info("Work queue stopped:" + getSourceId());
    } catch (InterruptedException e) {
      logger.info("Interrupted while waiting for queue:" + getSourceId());
    }
  }
}
