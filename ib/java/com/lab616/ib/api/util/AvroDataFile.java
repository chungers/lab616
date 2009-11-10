// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api.util;

import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.file.SeekableFileInput;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import com.google.common.base.Function;
import com.google.inject.internal.Preconditions;
import com.lab616.ib.api.avro.TWSEvent;
import com.lab616.omnibus.avro.test.Event;

/**
 * Class that write TWSEvent in avro form to a disk file.
 *
 * @author david
 *
 */
public class AvroDataFile {

  static Logger logger = Logger.getLogger(AvroDataFile.class);
  
  private enum State {
    READY,
    CLOSED;
  }
 
  private String dir;
  private String root;
  private DateTime lastDate;
  private Writer currentWriter;
  
  public AvroDataFile(String dir, String root) {
    this.dir = dir;
    this.root = root;
    this.lastDate = new DateTime().withMillisOfDay(0).minusDays(1);
  }
  
  
  public Writer getWriter() throws IOException {
    DateTime today = new DateTime().withMillisOfDay(0);
    if (today.isAfter(lastDate) || 
        (currentWriter != null && currentWriter.state == State.CLOSED)) {
      
      if (today.isAfter(lastDate) && currentWriter != null) {
        logger.info(root + ": New day. Flushing old file.");
        currentWriter.close();
      }
      currentWriter = new Writer(getFileName(today));
      logger.info(root + ": Writing to file: " + currentWriter.name);
      lastDate = today;
    }
    return currentWriter;
  }

  public static Reader getReader(String fname) throws IOException {
    return new Reader(fname);
  }
  
  public Reader getReader() throws IOException {
    DateTime today = new DateTime().withMillisOfDay(0);
    return new Reader(getFileName(today));
  }
  
  // Note do not append the file.  Just create a new file.
  private String getFileName(DateTime today) {
    File f = null;
    int tries = 0;
    do {
      String fn = (tries == 0) ? 
          String.format("%s/%s-%s.avro",
              this.dir, DateTimeFormat.forPattern("YYYY-MM-dd").print(today), root) :
          String.format("%s/%s-%s-%d.avro",
              this.dir, DateTimeFormat.forPattern("YYYY-MM-dd").print(today), root,
              tries);
      f = new File(fn);
      tries++;
    } while (f.exists());
    return f.getPath();
  }
  
  /**
   * Returns the current file being written or read.
   * @return The file.
   */
  public File getFile() {
    DateTime today = new DateTime().withMillisOfDay(0);
    return new File(getFileName(today));
  }
  
  public static class Writer {
    
    private File file;
    DataFileWriter<Object> dataFileWriter;
    private State state = null;
    private int written = 0;
    private String name;
    
    public Writer(String filename) throws IOException {
      name = filename;
      file = new File(filename);
      FileOutputStream fos = new FileOutputStream(file);

      Schema schema = new TWSEvent().getSchema();
      DatumWriter<Object> sdw = new SpecificDatumWriter(schema);
      dataFileWriter = new DataFileWriter<Object>(schema, fos, sdw);

      state = State.READY;
    }
    
    public boolean write(TWSEvent event) throws IOException {
      if (state != State.READY) {
        return false;
      }
      dataFileWriter.append(event);
      written++;
      return true;
    }
    
    public int countWritten() {
      return written;
    }
    
    public void close() {
      try {
        dataFileWriter.flush();
        dataFileWriter.close();
      } catch (IOException e) {
        // Nothing.
      } finally {
        state = State.CLOSED;
      }
    }
  }
  
  
  public static class Reader {
  
    private File file;
    private String name;
    private DataFileReader<Object> dataFileReader;
    private State state = null;
    public Reader(String filename) throws IOException {
      file = new File(filename);
      name = filename;
      Schema schema = new Event().getSchema();
      DatumReader<Object> sdr = new SpecificDatumReader(schema);
      SeekableFileInput sfi = new SeekableFileInput(file);
      dataFileReader = new DataFileReader<Object>(sfi, sdr);
      state = State.READY;
    }
    
    public Iterable<TWSEvent> readAll() {
      return readAll(new Function<TWSEvent, TWSEvent>() {
        public TWSEvent apply(TWSEvent event) {
          return event;
        }
      });
    }
    
    public <V> Iterable<V> readAll(final Function<TWSEvent, V> trans) {
      Preconditions.checkNotNull(trans);
      return new Iterable<V>() {
        public Iterator<V> iterator() {
          return Reader.this.iterator(trans);
        }
      };
    }
    
    private <V> Iterator<V> iterator(final Function<TWSEvent, V> trans) {
      return new Iterator<V>() {
        TWSEvent record = null;
        
        public boolean hasNext() {
          boolean next = false;
          if (state == State.READY) {
            try {
              TWSEvent reuse = new TWSEvent();
              reuse.timestamp = 0L; // Set this so we can tell if record is valid.
              Object read = dataFileReader.next(reuse);
              if (read != null) {
                record = (TWSEvent) read;
                next = record.timestamp > 0;
              }
            } catch (EOFException e) {
              next = false;
            } catch (IOException e) {
              logger.error("Exception while reading " + name, e);
              next = false;
            } catch (Exception e) {
              logger.error("Exception while reading " + name, e);
              next = true; // Just chug along.
            } finally {
              if (!next) {
                close();
              }
            }
          }
          return next;
        }

        public V next() {
          return trans.apply(record);
        }

        public void remove() {
        }
      };
    }
    
    public void close() {
      try {
        dataFileReader.close();
      } catch (IOException e) {
        // Nothing.
      } finally {
        state = State.CLOSED;
      }
    }
  }
  
}
