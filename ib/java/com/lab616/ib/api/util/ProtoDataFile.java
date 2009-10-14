// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api.util;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import com.google.common.base.Function;
import com.google.inject.internal.Preconditions;
import com.lab616.ib.api.ApiBuilder;
import com.lab616.ib.api.ApiMethods;
import com.lab616.ib.api.TWSEvent;
import com.lab616.ib.api.proto.TWSProto;

/**
 * Class that write TWSEvent in protobuffer form to a disk file.
 *
 * @author david
 *
 */
public class ProtoDataFile {

  static Logger logger = Logger.getLogger(ProtoDataFile.class);
  
  private enum State {
    READY,
    CLOSED;
  }
 
  private String dir;
  private String root;
  private DateTime lastDate;
  private Writer currentWriter;
  
  public ProtoDataFile(String dir, String root) {
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
  
  private String getFileName(DateTime today) {
    return String.format("%s/%s-%s.proto",
        this.dir, DateTimeFormat.forPattern("YYYY-MM-dd").print(today), root);
  }
  
  /**
   * Returns the current file being written or read.
   * @return The file.
   */
  public File getFile() {
    DateTime today = new DateTime().withMillisOfDay(0);
    return new File(getFileName(today));
  }
  
  public class Writer {
    
    private RandomAccessFile file;
    private State state = null;
    private int written = 0;
    private String name;
    
    public Writer(String filename) throws IOException {
      this(filename, true);
    }
    
    public Writer(String filename, boolean append) throws IOException {
      name = filename;
      file = new RandomAccessFile(filename, "rwd");
      // Move to the end to append.
      if (append) {
        file.seek(file.length());
      }
      state = State.READY;
    }
    
    public boolean write(TWSEvent event) throws IOException {
      if (state != State.READY) {
        return false;
      }
      ApiBuilder b = ApiMethods.get(event.getMethod());
      if (b != null) {
        TWSProto.Event e = b.buildProto(event);
        byte[] bytes = e.toByteArray();
        file.writeByte(bytes.length);
        file.write(bytes);
        written++;
      }
      return true;
    }
    
    public int countWritten() {
      return written;
    }
    
    public void close() {
      try {
        file.close();
      } catch (IOException e) {
        // Nothing.
      } finally {
        state = State.CLOSED;
      }
    }
  }
  
  
  public static class Reader {
  
    private RandomAccessFile file;
    private String name;
    private State state = null;
    public Reader(String filename) throws IOException {
      file = new RandomAccessFile(filename, "rwd");
      name = filename;
      state = State.READY;
      file.seek(0L);
    }
    
    public Iterable<TWSProto.Event> readAll() {
      return readAll(new Function<TWSProto.Event, TWSProto.Event>() {
        public TWSProto.Event apply(TWSProto.Event event) {
          return event;
        }
      });
    }
    
    public <V> Iterable<V> readAll(final Function<TWSProto.Event, V> trans) {
      Preconditions.checkNotNull(trans);
      return new Iterable<V>() {
        public Iterator<V> iterator() {
          return Reader.this.iterator(trans);
        }
      };
    }
    
    private <V> Iterator<V> iterator(final Function<TWSProto.Event, V> trans) {
      return new Iterator<V>() {
        TWSProto.Event record = null;
        int size = 0;
        public boolean hasNext() {
          boolean next = false;
          if (state == State.READY) {
            try {
              size = file.readByte();
              byte[] buf = new byte[size];
              file.read(buf);
              record = TWSProto.Event.parseFrom(buf);
              if (!record.isInitialized()) {
                logger.warn("Record not initialized: " + record);
              }
              next = true;
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
        file.close();
      } catch (IOException e) {
        // Nothing.
      } finally {
        state = State.CLOSED;
      }
    }
  }
  
}
