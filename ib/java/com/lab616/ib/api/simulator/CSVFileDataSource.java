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
 * @author david
 *
 */
public class CSVFileDataSource extends DataSource {

  static Logger logger = Logger.getLogger(CSVFileDataSource.class);

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
