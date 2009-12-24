// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api.simulator;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import com.lab616.ib.api.proto.TWSProto;
import com.lab616.ib.api.util.ProtoDataFile;

/**
 * Loads data from a CSV file.
 * @author david
 *
 */
public class ProtoFileDataSource extends DataSource {

  static Logger logger = Logger.getLogger(ProtoFileDataSource.class);

  private ProtoDataFile.Reader reader;
  private long latency = 0;
  
  public ProtoFileDataSource(String filename) throws IOException {
    super(filename);
    logger.info("Reading protos from " + filename);
    reader = ProtoDataFile.getReader(filename);
  }
  
  public void setEventLatency(long millis) {
  	latency = millis;
  }
  
  @Override
  public final boolean finished() {
  	return reader.isFinished();
  }
  
  /**
   * Reads the file record by record and writes to the queue with given latency.
   */
  protected void source(BlockingQueue<TWSProto.Event> sink) throws Exception {
    int count = 0;
    try {
      for (TWSProto.Event event : reader.readAll()) {
        if (latency > 0) {
        	try {
        		Thread.sleep(latency);
        	} catch (Exception e) { }
        }
        sink.put(event); 
        count++;
      }
    } finally {
      reader.close();
    }
  }
}
