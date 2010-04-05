// 2009-2010 lab616.com, All Rights Reserved.
package com.lab616.trading.tax;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.collect.ImmutableList;
import com.lab616.common.Pair;

/**
 * Class that load a CSV file of some type T
 * @author david
 *
 */
public abstract class CsvLoader<T> implements Iterable<T> {

  private static Logger logger = Logger.getLogger(CsvLoader.class);
  
  private final File file;
  private int lineNumber = 0;
  private DateTime current = new DateTime();
  private int itemForDay = 0;
  
  public CsvLoader(String filepath) throws IOException {
    file = new File(filepath);
    if (!file.canRead()) {
      throw new IOException("Not readable:" + filepath);
    }
  }
  
  public int getLineNumber() {
    return lineNumber;
  }
  
  protected abstract T parse(String line);
  
  static DateTimeFormatter formatter = DateTimeFormat.forPattern("MM/dd/yyyy");

  protected Pair<Long, Long> getTimeStampTradeId(String dateString) 
    throws IllegalArgumentException {
    DateTime date = formatter.parseDateTime(dateString);
    if (date.isEqual(current)){
      itemForDay++;
    } else {
      itemForDay = 0;
      current = date;
    }
    long ts = date.withTime(0, 0, 0, 0).getMillis();
    long id = ts * 1000L + itemForDay;
    return Pair.of(ts, id);
  }
  
  @Override
  public Iterator<T> iterator() {
    try {
      final LineNumberReader reader = new LineNumberReader(new FileReader(file));
      return new Iterator<T>() {
        T item = null;
        @Override
        public boolean hasNext() {
          try {
            if (reader.ready()) {
              lineNumber = reader.getLineNumber();
              item = parse(reader.readLine());
              String line = null;
              while (item == null && ((line = reader.readLine()) != null)) {
                // read ahead
                lineNumber = reader.getLineNumber();
                item = parse(line);
              }
              return item != null;
            }
            return false;
          } catch (IOException e) {
            close();
            return false;
          }
        }

        @Override
        public T next() {
          return item;
        }

        @Override
        public void remove() {
          // Nothing.
        }
        
        private void close() {
          try {
            reader.close();
          } catch (IOException e) {
            // Nothing.
          }
        }
      };
    } catch (IOException e) {
      logger.warn("Exception:", e);
      List<T> empty = ImmutableList.of();
      return empty.iterator();
    }
  }
}
