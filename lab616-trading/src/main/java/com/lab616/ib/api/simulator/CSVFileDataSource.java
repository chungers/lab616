// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api.simulator;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import com.lab616.ib.api.ApiBuilder;
import com.lab616.ib.api.ApiMethods;
import com.lab616.ib.api.proto.TWSProto;

/**
 * Loads data from a CSV file.
 * @author david
 *
 */
public class CSVFileDataSource extends DataSource {

  static Logger logger = Logger.getLogger(CSVFileDataSource.class);

  private LineNumberReader reader;
  private long latency = 0;
  
  public CSVFileDataSource(String filename) throws IOException {
    super(filename);
    logger.info("Reading from " + filename);
    reader = new LineNumberReader(new FileReader(filename));
  }
  
  public void setEventLatency(long millis) {
  	latency = millis;
  }
  
  /**
   * Writes to the sink line by line, each line is converted to proto.
   */
  protected void source(BlockingQueue<TWSProto.Event> sink) throws Exception {
    String line;
    try {
      while ((line = reader.readLine()) != null) {
        if (!line.startsWith("#")) {
          String[] cols = line.split(",");
          ApiBuilder b = ApiMethods.get(cols[1]);
          if (b != null) {
            TWSProto.Event event = b.buildProto(getResource(), cols);
            if (latency > 0) {
            	try {
            		Thread.sleep(latency);
            	} catch (Exception e) { }
            }
            sink.put(event); 
          }
        }
      }
    } finally {
      reader.close();
    }
  }
}
