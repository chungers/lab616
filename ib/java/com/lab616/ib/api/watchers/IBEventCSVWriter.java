// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api.watchers;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import com.lab616.concurrent.AbstractQueueWorker;
import com.lab616.ib.api.IBClientException;
import com.lab616.ib.api.IBEvent;
import com.lab616.ib.api.IBService.Managed;
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
@Statement("select * from IBEvent where source=?")
public class IBEventCSVWriter extends AbstractEventWatcher implements Managed {

  static Logger logger = Logger.getLogger(IBEventCSVWriter.class);

  private PrintWriter print;
  private String clientSourceId;
  private AbstractQueueWorker<IBEvent> queueWorker;
  private DateTime lastDate;
  
  public IBEventCSVWriter(String clientSourceId) {
    this.clientSourceId = clientSourceId;
    this.lastDate = new DateTime().withMillisOfDay(0).minusDays(1);
    try {
      print = getOutput();
    } catch (IOException e) {
      throw new IBClientException(e);
    }
    final String id = clientSourceId;
    this.queueWorker = new AbstractQueueWorker<IBEvent>(clientSourceId, false) {
      @Override
      protected void execute(IBEvent event) throws Exception {
        write(event);
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
        logger.info("Started csv writer @" + id);
        return true;
      }
      @Override
      protected void onStop(int queueSize) {
        logger.info("Stopped csv writer @" + id + " at queue=" + queueSize);
        try {
          getOutput().flush();
          getOutput().close();
        } catch (IOException e) {
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
  
  private PrintWriter getOutput() throws IOException {
    DateTime today = new DateTime().withMillisOfDay(0);
    if (today.isAfter(lastDate)) {
      
      if (print != null) {
        logger.info(clientSourceId + ": New day. Flushing old file.");
        print.flush();
        print.close();
      }
      
      logger.info(clientSourceId + ": Starting new file.");
      String fn = getFileName(today);
      FileWriter fw = new FileWriter(fn, true);
      print = new PrintWriter(fw);
      logger.info(clientSourceId + ": Writing to log file " + fn);
      print.println("## " + today);
      print.flush();
      lastDate = today;
      return print;
    }
    return print;
  }
  
  private String getFileName(DateTime today) {
    return String.format("./%s-%s.csv",
        DateTimeFormat.forPattern("YYYY-MM-dd").print(today),
        this.clientSourceId);
  }
  
  /**
   * Receives the IBEvent from the event engine.
   * @param event The event.
   */
  public void update(IBEvent event) {
    if (event != null) {
      this.queueWorker.enqueue(event);
    }
  }

  /**
   * Returns if the output writer is ready (open, no errors).
   */
  public boolean isReady() {
    try {
      return !getOutput().checkError();
    } catch (IOException e) {
      return false;
    }
  }
  
  /**
   * Stop this writer.
   */
  public void stop() {
    super.stop();
    this.queueWorker.setRunning(false);
  }

  /**
   * Writes the event to the output file.
   * @param event The event.
   * @throws Exception Exception during writes.
   */
  private void write(IBEvent event) throws Exception {
    PrintWriter p = getOutput();
    p.println(event.toString());
    p.flush();
  }
}
