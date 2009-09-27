// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api.watchers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.ib.client.TickType;
import com.lab616.ib.api.IBClientException;
import com.lab616.ib.api.IBEvent;
import com.lab616.ib.api.TickerId;
import com.lab616.omnibus.event.AbstractEventWatcher;
import com.lab616.omnibus.event.annotation.Statement;
import com.lab616.omnibus.event.annotation.Var;

/**
 *
 *
 * @author david
 *
 */
@Statement("select * from IBEvent where source=?")
public class IBEventCSVWriter extends AbstractEventWatcher {

  static Logger logger = Logger.getLogger(IBEventCSVWriter.class);

  private PrintWriter print;
  private String clientSourceId;
  public IBEventCSVWriter(String clientSourceId) {
    this.clientSourceId = clientSourceId;
    try {
      String fn = getFileName();
      FileWriter fw = new FileWriter(fn, true);
      print = new PrintWriter(fw);
      logger.info("Writing to log file " + fn);
    } catch (IOException e) {
      throw new IBClientException(e);
    }
  }
  
  @Var(1)
  public String getSourceId() {
    return this.clientSourceId;
  }
  
  private String getFileName() {
    DateTime today = new DateTime();
    return String.format("./%s-%s.csv",
        DateTimeFormat.forPattern("YYYY-MM-dd").print(today),
        this.clientSourceId);
  }
  
  public void update(IBEvent event) {
    if (event == null) return;
    if (print == null) return;
    // Only one file but possibly invoked from different event subscriber
    // threads; therefore, we need to synchronize on the writer.
    synchronized (print) {
      print.print(event.getTimestamp() + "," + event.getMethod());
      if (event.getArgs() != null && event.getArgs().length > 0) {
        Object[] args = event.getArgs();
        
        print.print("," + TickerId.fromTickerId((Integer)args[0]));
        if (args.length > 1) {
          print.print("," + TickType.getField((Integer)args[1]));
        } else {
          print.print("NONE");
        }
       
        StringBuffer str = new StringBuffer(args[0].toString());
        for (int i = 1; i < args.length; i++) {
          str.append(",");
          str.append(args[i]);
        }
        print.print("," + str.toString());
      }
      print.println();
      print.flush();
    }
  }
}