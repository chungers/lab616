/**
 * Copyright 2009 The Apache Software Foundation
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hbase.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.DoNotRetryIOException;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.HRegionLocation;
import org.apache.hadoop.hbase.HServerAddress;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.NotServingRegionException;
import org.apache.hadoop.hbase.UnknownScannerException;
import org.apache.hadoop.hbase.client.MetaScanner.MetaScannerVisitor;
import org.apache.hadoop.hbase.filter.RowFilterInterface;
import org.apache.hadoop.hbase.filter.StopRowFilter;
import org.apache.hadoop.hbase.filter.WhileMatchRowFilter;
import org.apache.hadoop.hbase.io.BatchOperation;
import org.apache.hadoop.hbase.io.BatchUpdate;
import org.apache.hadoop.hbase.io.Cell;
import org.apache.hadoop.hbase.io.HbaseMapWritable;
import org.apache.hadoop.hbase.io.RowResult;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Pair;
import org.apache.hadoop.hbase.util.Writables;


/**
 * Used to communicate with a single HBase table.
 * This class is not thread safe. Use one instance per thread.
 * 
 * Puts, deletes, checkAndPut and incrementColumnValue are 
 * done in an exclusive (and thus serial) fashion for each row. 
 * These calls acquire a row lock which is shared with the lockRow
 * calls. 
 * 
 * Gets and Scans will not return half written data. That is, 
 * all mutation operations are atomic on a row basis with
 * respect to other concurrent readers and writers. 
 */
public class HTable {
  private final HConnection connection;
  private final byte [] tableName;
  protected final int scannerTimeout;
  private volatile HBaseConfiguration configuration;
  private final ArrayList<Put> writeBuffer = new ArrayList<Put>();
  private long writeBufferSize;
  private boolean autoFlush;
  private long currentWriteBufferSize;
  protected int scannerCaching;
  private long maxScannerResultSize;
  
  /**
   * Creates an object to access a HBase table.
   *
   * @param tableName Name of the table.
   * @throws IOException if a remote or network exception occurs
   */
  public HTable(final String tableName)
  throws IOException {
    this(new HBaseConfiguration(), Bytes.toBytes(tableName));
  }

  /**
   * Creates an object to access a HBase table.
   *
   * @param tableName Name of the table.
   * @throws IOException if a remote or network exception occurs
   */
  public HTable(final byte [] tableName)
  throws IOException {
    this(new HBaseConfiguration(), tableName);
  }

  /**
   * Creates an object to access a HBase table.
   * 
   * @param conf Configuration object to use.
   * @param tableName Name of the table.
   * @throws IOException if a remote or network exception occurs
   */
  public HTable(HBaseConfiguration conf, final String tableName)
  throws IOException {
    this(conf, Bytes.toBytes(tableName));
  }

  /**
   * Creates an object to access a HBase table.
   * 
   * @param conf Configuration object to use.
   * @param tableName Name of the table.
   * @throws IOException if a remote or network exception occurs
   */
  public HTable(HBaseConfiguration conf, final byte [] tableName)
  throws IOException {
    this.tableName = tableName;
    if (conf == null) {
      this.scannerTimeout = 0;
      this.connection = null;
      return;
    }
    this.connection = HConnectionManager.getConnection(conf);
    this.scannerTimeout =
      conf.getInt("hbase.regionserver.lease.period", 60 * 1000);
    this.configuration = conf;
    this.connection.locateRegion(tableName, HConstants.EMPTY_START_ROW);
    this.writeBufferSize = conf.getLong("hbase.client.write.buffer", 2097152);
    this.autoFlush = true;
    this.currentWriteBufferSize = 0;
    this.scannerCaching = conf.getInt("hbase.client.scanner.caching", 1);
    this.maxScannerResultSize = conf.getLong(
      HConstants.HBASE_CLIENT_SCANNER_MAX_RESULT_SIZE_KEY, 
      HConstants.DEFAULT_HBASE_CLIENT_SCANNER_MAX_RESULT_SIZE);
  }

  /**
   * Tells whether or not a table is enabled or not.
   * @param tableName Name of table to check.
   * @return {@code true} if table is online.
   * @throws IOException if a remote or network exception occurs
   */
  public static boolean isTableEnabled(String tableName) throws IOException {
    return isTableEnabled(Bytes.toBytes(tableName));
  }

  /**
   * Tells whether or not a table is enabled or not.
   * @param tableName Name of table to check.
   * @return {@code true} if table is online.
   * @throws IOException if a remote or network exception occurs
   */
  public static boolean isTableEnabled(byte[] tableName) throws IOException {
    return isTableEnabled(new HBaseConfiguration(), tableName);
  }
  
  /**
   * Tells whether or not a table is enabled or not.
   * @param conf The Configuration object to use.
   * @param tableName Name of table to check.
   * @return {@code true} if table is online.
   * @throws IOException if a remote or network exception occurs
   */
  public static boolean isTableEnabled(HBaseConfiguration conf, String tableName)
  throws IOException {
    return isTableEnabled(conf, Bytes.toBytes(tableName));
  }

  /**
   * Tells whether or not a table is enabled or not.
   * @param conf The Configuration object to use.
   * @param tableName Name of table to check.
   * @return {@code true} if table is online.
   * @throws IOException if a remote or network exception occurs
   */
  public static boolean isTableEnabled(HBaseConfiguration conf, byte[] tableName)
  throws IOException {
    return HConnectionManager.getConnection(conf).isTableEnabled(tableName);
  }

  /**
   * Find region location hosting passed row using cached info
   * @param row Row to find.
   * @return The location of the given row.
   * @throws IOException if a remote or network exception occurs
   */
  public HRegionLocation getRegionLocation(final String row)
  throws IOException {
    return connection.getRegionLocation(tableName, Bytes.toBytes(row), false);
  }

  /**
   * Finds the region on which the given row is being served.
   * @param row Row to find.
   * @return Location of the row.
   * @throws IOException if a remote or network exception occurs
   */
  public HRegionLocation getRegionLocation(final byte [] row)
  throws IOException {
    return connection.getRegionLocation(tableName, row, false);
  }

  /**
   * Gets the name of this table.
   *
   * @return the table name.
   */
  public byte [] getTableName() {
    return this.tableName;
  }

  /**
   * <em>INTERNAL</em> Used by unit tests and tools to do low-level
   * manipulations.
   * @return An HConnection instance.
   */
  // TODO(tsuna): Remove this.  Unit tests shouldn't require public helpers.
  public HConnection getConnection() {
    return this.connection;
  }
  
  /**
   * Gets the number of rows that a scanner will fetch at once.
   * <p>
   * The default value comes from {@code hbase.client.scanner.caching}.
   */
  public int getScannerCaching() {
    return scannerCaching;
  }

  /**
   * Sets the number of rows that a scanner will fetch at once.
   * <p>
   * This will override the value specified by
   * {@code hbase.client.scanner.caching}.
   * Increasing this value will reduce the amount of work needed each time
   * {@code next()} is called on a scanner, at the expense of memory use
   * (since more rows will need to be maintained in memory by the scanners).
   * @param scannerCaching the number of rows a scanner will fetch at once.
   */
  public void setScannerCaching(int scannerCaching) {
    this.scannerCaching = scannerCaching;
  }

  /**
   * Gets the {@link HTableDescriptor table descriptor} for this table.
   * @throws IOException if a remote or network exception occurs.
   */
  public HTableDescriptor getTableDescriptor() throws IOException {
    return new UnmodifyableHTableDescriptor(
      this.connection.getHTableDescriptor(this.tableName));
  }

  /**
   * Gets the starting row key for every region in the currently open table.
   * <p>
   * This is mainly useful for the MapReduce integration.
   * @return Array of region starting row keys
   * @throws IOException if a remote or network exception occurs
   */
  public byte [][] getStartKeys() throws IOException {
    return getStartEndKeys().getFirst();
  }

  /**
   * Gets the ending row key for every region in the currently open table.
   * <p>
   * This is mainly useful for the MapReduce integration.
   * @return Array of region ending row keys
   * @throws IOException if a remote or network exception occurs
   */
  public byte[][] getEndKeys() throws IOException {
    return getStartEndKeys().getSecond();
  }

  /**
   * Gets the starting and ending row keys for every region in the currently
   * open table.
   * <p>
   * This is mainly useful for the MapReduce integration.
   * @return Pair of arrays of region starting and ending row keys
   * @throws IOException if a remote or network exception occurs
   */
  @SuppressWarnings("unchecked")
  public Pair<byte[][],byte[][]> getStartEndKeys() throws IOException {
    final List<byte[]> startKeyList = new ArrayList<byte[]>();
    final List<byte[]> endKeyList = new ArrayList<byte[]>();
    MetaScannerVisitor visitor = new MetaScannerVisitor() {
      public boolean processRow(Result rowResult) throws IOException {
        HRegionInfo info = Writables.getHRegionInfo(
            rowResult.getValue(HConstants.CATALOG_FAMILY, 
                HConstants.REGIONINFO_QUALIFIER));
        if (Bytes.equals(info.getTableDesc().getName(), getTableName())) {
          if (!(info.isOffline() || info.isSplit())) {
            startKeyList.add(info.getStartKey());
            endKeyList.add(info.getEndKey());
          }
        }
        return true;
      }
    };
    MetaScanner.metaScan(configuration, visitor, this.tableName);
    return new Pair(startKeyList.toArray(new byte[startKeyList.size()][]),
                endKeyList.toArray(new byte[endKeyList.size()][]));
  }

  /**
   * Gets all the regions and their address for this table.
   * <p>
   * This is mainly useful for the MapReduce integration.
   * @return A map of HRegionInfo with it's server address
   * @throws IOException if a remote or network exception occurs
   */
  public Map<HRegionInfo, HServerAddress> getRegionsInfo() throws IOException {
    final Map<HRegionInfo, HServerAddress> regionMap =
      new TreeMap<HRegionInfo, HServerAddress>();

    MetaScannerVisitor visitor = new MetaScannerVisitor() {
      public boolean processRow(Result rowResult) throws IOException {
        HRegionInfo info = Writables.getHRegionInfo(
            rowResult.getValue(HConstants.CATALOG_FAMILY, 
                HConstants.REGIONINFO_QUALIFIER));
        
        if (!(Bytes.equals(info.getTableDesc().getName(), getTableName()))) {
          return false;
        }

        HServerAddress server = new HServerAddress();
        byte [] value = rowResult.getValue(HConstants.CATALOG_FAMILY, 
            HConstants.SERVER_QUALIFIER);
        if (value != null && value.length > 0) {
          String address = Bytes.toString(value);
          server = new HServerAddress(address);
        }
        
        if (!(info.isOffline() || info.isSplit())) {
          regionMap.put(new UnmodifyableHRegionInfo(info), server);
        }
        return true;
      }

    };
    MetaScanner.metaScan(configuration, visitor, tableName);
    return regionMap;
  }

  /**
   * Return the row that matches <i>row</i> exactly, 
   * or the one that immediately precedes it.
   * 
   * @param row A row key.
   * @param family Column family to include in the {@link Result}.
   * @throws IOException if a remote or network exception occurs.
   * @since 0.20.0
    */
   public Result getRowOrBefore(final byte[] row, final byte[] family)
   throws IOException {
     return connection.getRegionServerWithRetries(
         new ServerCallable<Result>(connection, tableName, row) {
       public Result call() throws IOException {
         return server.getClosestRowBefore(location.getRegionInfo().getRegionName(),
           row, family);
       }
     });
   }

  /**
  * Return the row that matches <i>row</i> exactly, 
  * or the one that immediately preceeds it.
  * 
  * @param row row key
  * @param family Column family to look for row in.
  * @return map of values
  * @throws IOException
  * @deprecated As of hbase 0.20.0, replaced by {@link #getRowOrBefore(byte[], byte[])}
  */
  public RowResult getClosestRowBefore(final byte[] row, final byte[] family)
  throws IOException {
    // Do parse in case we are passed a family with a ':' on it.
    final byte [] f = KeyValue.parseColumn(family)[0];
    Result r = getRowOrBefore(row, f);
    return r == null || r.isEmpty()? null: r.getRowResult();
  }

  /** 
   * Returns a scanner on the current table as specified by the {@link Scan}
   * object.
   *
   * @param scan A configured {@link Scan} object.
   * @return A scanner.
   * @throws IOException if a remote or network exception occurs.
   * @since 0.20.0
   */
  public ResultScanner getScanner(final Scan scan) throws IOException {
    ClientScanner s = new ClientScanner(scan);
    s.initialize();
    return s;
  }

  /**
   * Gets a scanner on the current table for the given family.
   * 
   * @param family  The column family to scan.
   * @return A scanner.
   * @throws IOException if a remote or network exception occurs.
   * @since 0.20.0
   */
  public ResultScanner getScanner(byte [] family) throws IOException {
    Scan scan = new Scan();
    scan.addFamily(family);
    return getScanner(scan);
  }
  
  /**
   * Gets a scanner on the current table for the given family and qualifier.
   * 
   * @param family  The column family to scan.
   * @param qualifier  The column qualifier to scan.
   * @return A scanner.
   * @throws IOException if a remote or network exception occurs.
   * @since 0.20.0
   */
  public ResultScanner getScanner(byte [] family, byte [] qualifier)
  throws IOException {
    Scan scan = new Scan();
    scan.addColumn(family, qualifier);
    return getScanner(scan);
  }

  /**
   * Extracts certain cells from a given row.
   * @param get The object that specifies what data to fetch and from which row.
   * @return The data coming from the specified row, if it exists.  If the row
   * specified doesn't exist, the {@link Result} instance returned won't
   * contain any {@link KeyValue}, as indicated by {@link Result#isEmpty()}.
   * @throws IOException if a remote or network exception occurs.
   * @since 0.20.0
   */
  public Result get(final Get get) throws IOException {
    return connection.getRegionServerWithRetries(
        new ServerCallable<Result>(connection, tableName, get.getRow()) {
          public Result call() throws IOException {
            return server.get(location.getRegionInfo().getRegionName(), get);
          }
        }
    );
  }
  
  /**
   * Deletes the specified cells/row.
   * 
   * @param delete The object that specifies what to delete.
   * @throws IOException if a remote or network exception occurs.
   * @since 0.20.0
   */
  public void delete(final Delete delete)
  throws IOException {
    connection.getRegionServerWithRetries(
        new ServerCallable<Boolean>(connection, tableName, delete.getRow()) {
          public Boolean call() throws IOException {
            server.delete(location.getRegionInfo().getRegionName(), delete);
            return null;
          }
        }
    );
  }
  
  /**
   * Deletes the specified cells/rows in bulk.
   * @param deletes List of things to delete.  List gets modified by this
   * method (in particular it gets re-ordered, so the order in which the elements
   * are inserted in the list gives no guarantee as to the order in which the
   * {@link Delete}s are executed).
   * @throws IOException if a remote or network exception occurs. In that case
   * the {@code deletes} argument will contain the {@link Delete} instances
   * that have not be successfully applied.
   * @since 0.20.1
   */
  public synchronized void delete(final ArrayList<Delete> deletes)
  throws IOException {
    int last = 0;
    try {
      last = connection.processBatchOfDeletes(deletes, this.tableName);
    } finally {
      deletes.subList(0, last).clear();
    }
  }

  /**
   * Puts some data in the table.
   * <p>
   * If {@link #isAutoFlush isAutoFlush} is false, the update is buffered
   * until the internal buffer is full.
   * @param put The data to put.
   * @throws IOException if a remote or network exception occurs.
   * @since 0.20.0
   */
  public synchronized void put(final Put put) throws IOException {
    validatePut(put);
    writeBuffer.add(put);
    currentWriteBufferSize += put.heapSize();
    if(autoFlush || currentWriteBufferSize > writeBufferSize) {
      flushCommits();
    }
  }
  
  /**
   * Puts some data in the table, in batch.
   * <p>
   * If {@link #isAutoFlush isAutoFlush} is false, the update is buffered
   * until the internal buffer is full.
   * @param puts The list of mutations to apply.  The list gets modified by this
   * method (in particular it gets re-ordered, so the order in which the elements
   * are inserted in the list gives no guarantee as to the order in which the
   * {@link Put}s are executed).
   * @throws IOException if a remote or network exception occurs. In that case
   * the {@code puts} argument will contain the {@link Put} instances that
   * have not be successfully applied.
   * @since 0.20.0
   */
  public synchronized void put(final List<Put> puts) throws IOException {
    for (Put put : puts) {
      validatePut(put);
      writeBuffer.add(put);
      currentWriteBufferSize += put.heapSize();
    }
    if (autoFlush || currentWriteBufferSize > writeBufferSize) {
      flushCommits();
    }
  }
  
  /**
   * Atomically increments a column value.
   * <p>
   * Equivalent to {@code {@link #incrementColumnValue(byte[], byte[], byte[],
   * long, boolean) incrementColumnValue}(row, family, qualifier, amount,
   * <b>true</b>)}
   * @param row The row that contains the cell to increment.
   * @param family The column family of the cell to increment.
   * @param qualifier The column qualifier of the cell to increment.
   * @param amount The amount to increment the cell with (or decrement, if the
   * amount is negative).
   * @return The new value, post increment.
   * @throws IOException if a remote or network exception occurs.
   */
  public long incrementColumnValue(final byte [] row, final byte [] family, 
      final byte [] qualifier, final long amount)
  throws IOException {
    return incrementColumnValue(row, family, qualifier, amount, true);
  }

  /**
   * Atomically increments a column value. If the column value already exists
   * and is not a big-endian long, this could throw an exception. If the column
   * value does not yet exist it is initialized to <code>amount</code> and
   * written to the specified column.
   * 
   * <p>Setting writeToWAL to false means that in a fail scenario, you will lose
   * any increments that have not been flushed.
   * @param row The row that contains the cell to increment.
   * @param family The column family of the cell to increment.
   * @param qualifier The column qualifier of the cell to increment.
   * @param amount The amount to increment the cell with (or decrement, if the
   * amount is negative).
   * @param writeToWAL if {@code true}, the operation will be applied to the
   * Write Ahead Log (WAL).  This makes the operation slower but safer, as if
   * the call returns successfully, it is guaranteed that the increment will
   * be safely persisted.  When set to {@code false}, the call may return
   * successfully before the increment is safely persisted, so it's possible
   * that the increment be lost in the event of a failure happening before the
   * operation gets persisted.
   * @return The new value, post increment.
   * @throws IOException if a remote or network exception occurs.
   */
  public long incrementColumnValue(final byte [] row, final byte [] family, 
      final byte [] qualifier, final long amount, final boolean writeToWAL)
  throws IOException {
    NullPointerException npe = null;
    if (row == null) {
      npe = new NullPointerException("row is null");
    } else if (family == null) {
      npe = new NullPointerException("column is null");
    }
    if (npe != null) {
      IOException io = new IOException(
          "Invalid arguments to incrementColumnValue", npe);
      throw io;
    }
    return connection.getRegionServerWithRetries(
        new ServerCallable<Long>(connection, tableName, row) {
          public Long call() throws IOException {
            return server.incrementColumnValue(
                location.getRegionInfo().getRegionName(), row, family, 
                qualifier, amount, writeToWAL);
          }
        }
    );
  }

  /**
   * Atomically checks if a row/family/qualifier value match the expectedValue.
   * If it does, it adds the put.
   * 
   * @param row
   * @param family
   * @param qualifier
   * @param value the expected value
   * @param put
   * @throws IOException
   * @return true if the new put was execute, false otherwise
   */
  public synchronized boolean checkAndPut(final byte [] row, 
      final byte [] family, final byte [] qualifier, final byte [] value, 
      final Put put)
  throws IOException {
    return connection.getRegionServerWithRetries(
        new ServerCallable<Boolean>(connection, tableName, row) {
          public Boolean call() throws IOException {
            return server.checkAndPut(location.getRegionInfo().getRegionName(),
              row, family, qualifier, value, put)? Boolean.TRUE: Boolean.FALSE;
          }
        }
      ).booleanValue();
  }
  
  /**
   * Test for the existence of columns in the table, as specified in the Get.<p>
   * 
   * This will return true if the Get matches one or more keys, false if not.<p>
   * 
   * This is a server-side call so it prevents any data from being transfered
   * to the client.
   * @param get
   * @return true if the specified Get matches one or more keys, false if not
   * @throws IOException
   */
  public boolean exists(final Get get) throws IOException {
    return connection.getRegionServerWithRetries(
      new ServerCallable<Boolean>(connection, tableName, get.getRow()) {
        public Boolean call() throws IOException {
          return Boolean.valueOf(server.
            exists(location.getRegionInfo().getRegionName(), get));
        }
      }
    ).booleanValue();
  }
  
  /**
   * Executes all the buffered {@link Put} operations.
   * <p>
   * This method gets called once automatically for every {@link Put} or batch
   * of {@link Put}s (when {@link #put(List)} is used) when
   * {@link #isAutoFlush} is {@code true}.
   * @throws IOException if a remote or network exception occurs.
   */
  public void flushCommits() throws IOException {
    int last = 0;
    try {
      last = connection.processBatchOfRows(writeBuffer, tableName);
    } finally {
      writeBuffer.subList(0, last).clear();
      currentWriteBufferSize = 0;
      for (int i = 0; i < writeBuffer.size(); i++) {
        currentWriteBufferSize += writeBuffer.get(i).heapSize();
      }
    }
  }

  /**
   * Releases any resources help or pending changes in internal buffers.
   * 
   * @throws IOException if a remote or network exception occurs.
  */
  public void close() throws IOException{
    flushCommits();
  }
  
  /**
   * Utility method that verifies Put is well formed.
   * 
   * @param put
   * @throws IllegalArgumentException
   */
  private void validatePut(final Put put) throws IllegalArgumentException{
    if(put.isEmpty()) {
      throw new IllegalArgumentException("No columns to insert");
    }
  }
  
  /**
   * Obtains a lock on a row.
   *
   * @param row The row to lock.
   * @return A {@link RowLock} containing the row and lock id.
   * @throws IOException if a remote or network exception occurs.
   * @see RowLock
   * @see #unlockRow
   */
  public RowLock lockRow(final byte [] row)
  throws IOException {
    return connection.getRegionServerWithRetries(
      new ServerCallable<RowLock>(connection, tableName, row) {
        public RowLock call() throws IOException {
          long lockId =
              server.lockRow(location.getRegionInfo().getRegionName(), row);
          RowLock rowLock = new RowLock(row,lockId);
          return rowLock;
        }
      }
    );
  }

  /**
   * Releases a row lock.
   *
   * @param rl The row lock to release.
   * @throws IOException if a remote or network exception occurs.
   * @see RowLock
   * @see #lockRow
   */
  public void unlockRow(final RowLock rl)
  throws IOException {
    connection.getRegionServerWithRetries(
      new ServerCallable<Boolean>(connection, tableName, rl.getRow()) {
        public Boolean call() throws IOException {
          server.unlockRow(location.getRegionInfo().getRegionName(),
              rl.getLockId());
          return null;
        }
      }
    );
  }
  
  /**
   * Tells whether or not 'auto-flush' is turned on.
   *
   * @return {@code true} if 'auto-flush' is enabled (default), meaning
   * {@link Put} operations don't get buffered/delayed and are immediately
   * executed.
   * @see #setAutoFlush
   */
  public boolean isAutoFlush() {
    return autoFlush;
  }

  /**
   * Turns on or off 'auto-flush' on this instance.
   * @param autoFlush Whether or not to use 'auto-flush'.
   * @see #isAutoFlush
   */
  public void setAutoFlush(boolean autoFlush) {
    this.autoFlush = autoFlush;
  }

  /**
   * Get the maximum size in bytes of the write buffer for this HTable
   * @return the size of the write buffer in bytes
   */
  public long getWriteBufferSize() {
    return writeBufferSize;
  }

  /**
   * Set the size of the buffer in bytes.
   * If the new size is lower than the current size of data in the 
   * write buffer, the buffer is flushed.
   * @param writeBufferSize
   * @throws IOException
   */
  public void setWriteBufferSize(long writeBufferSize) throws IOException {
    this.writeBufferSize = writeBufferSize;
    if(currentWriteBufferSize > writeBufferSize) {
      flushCommits();
    }
  }

  /**
   * Get the write buffer
   * @return the current write buffer
   */
  public ArrayList<Put> getWriteBuffer() {
    return writeBuffer;
  }

  // Old API. Pre-hbase-880, hbase-1304.
  
  /**
   * Get a single value for the specified row and column
   * 
   * @param row row key
   * @param column column name
   * @return value for specified row/column
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #get(Get)}
   */
  public Cell get(final String row, final String column)
  throws IOException {
    return get(Bytes.toBytes(row), Bytes.toBytes(column));
  }

  /** 
   * Get a single value for the specified row and column
   *
   * @param row row key
   * @param column column name
   * @param numVersions - number of versions to retrieve
   * @return value for specified row/column
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #get(Get)}
   */
  public Cell [] get(final String row, final String column, int numVersions)
  throws IOException {
    return get(Bytes.toBytes(row), Bytes.toBytes(column), numVersions);
  }

  /** 
   * Get a single value for the specified row and column
   *
   * @param row row key
   * @param column column name
   * @return value for specified row/column
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #get(Get)}
   */
  public Cell get(final byte [] row, final byte [] column)
  throws IOException {
    Get g = new Get(row);
    byte [][] fq = KeyValue.parseColumn(column);
    g.addColumn(fq[0], fq[1]);
    Result r = get(g);
    return r == null || r.size() <= 0? null: r.getCellValue();
  }

  /** 
   * Get the specified number of versions of the specified row and column
   * @param row row key
   * @param column column name
   * @param numVersions number of versions to retrieve
   * @return Array of Cells.
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #get(Get)}
   */
  public Cell [] get(final byte [] row, final byte [] column,
    final int numVersions)
  throws IOException {
    return get(row, column, HConstants.LATEST_TIMESTAMP, numVersions);
  }

  /** 
   * Get the specified number of versions of the specified row and column with
   * the specified timestamp.
   *
   * @param row         - row key
   * @param column      - column name
   * @param timestamp   - timestamp
   * @param numVersions - number of versions to retrieve
   * @return            - array of values that match the above criteria
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #get(Get)}
   */
  public Cell[] get(final String row, final String column,
    final long timestamp, final int numVersions)
  throws IOException {
    return get(Bytes.toBytes(row), Bytes.toBytes(column), timestamp, numVersions);
  }

  /** 
   * Get the specified number of versions of the specified row and column with
   * the specified timestamp.
   *
   * @param row         - row key
   * @param column      - column name
   * @param timestamp   - timestamp
   * @param numVersions - number of versions to retrieve
   * @return            - array of values that match the above criteria
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #get(Get)}
   */
  public Cell[] get(final byte [] row, final byte [] column,
    final long timestamp, final int numVersions)
  throws IOException {
    Get g = new Get(row);
    byte [][] fq = KeyValue.parseColumn(column);
    if (fq[1].length == 0) {
      g.addFamily(fq[0]);
    } else {
      g.addColumn(fq[0], fq[1]);
    }
    g.setMaxVersions(numVersions);
    g.setTimeRange(0, 
        timestamp == HConstants.LATEST_TIMESTAMP ? timestamp : timestamp+1);
    Result r = get(g);
    return r == null || r.size() <= 0? null: r.getCellValues();
  }

  /** 
   * Get all the data for the specified row at the latest timestamp
   * 
   * @param row row key
   * @return RowResult is <code>null</code> if row does not exist.
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #get(Get)}
   */
  public RowResult getRow(final String row) throws IOException {
    return getRow(Bytes.toBytes(row));
  }

  /** 
   * Get all the data for the specified row at the latest timestamp
   * 
   * @param row row key
   * @return RowResult is <code>null</code> if row does not exist.
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #get(Get)}
   */
  public RowResult getRow(final byte [] row) throws IOException {
    return getRow(row, HConstants.LATEST_TIMESTAMP);
  }
 
  /** 
   * Get more than one version of all columns for the specified row
   * 
   * @param row row key
   * @param numVersions number of versions to return
   * @return RowResult is <code>null</code> if row does not exist.
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #get(Get)}
   */
  public RowResult getRow(final String row, final int numVersions)
  throws IOException {
    return getRow(Bytes.toBytes(row), null, 
                  HConstants.LATEST_TIMESTAMP, numVersions, null);
  }

  /** 
   * Get more than one version of all columns for the specified row
   * 
   * @param row row key
   * @param numVersions number of versions to return
   * @return RowResult is <code>null</code> if row does not exist.
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #get(Get)}
   */
  public RowResult getRow(final byte[] row, final int numVersions)
  throws IOException {
    return getRow(row, null, HConstants.LATEST_TIMESTAMP, numVersions, null);
  }

  /** 
   * Get all the data for the specified row at a specified timestamp
   * 
   * @param row row key
   * @param ts timestamp
   * @return RowResult is <code>null</code> if row does not exist.
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #get(Get)}
   */
  public RowResult getRow(final String row, final long ts) 
  throws IOException {
    return getRow(Bytes.toBytes(row), ts);
  }

  /** 
   * Get all the data for the specified row at a specified timestamp
   * 
   * @param row row key
   * @param ts timestamp
   * @return RowResult is <code>null</code> if row does not exist.
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #get(Get)}
   */
  public RowResult getRow(final byte [] row, final long ts) 
  throws IOException {
    return getRow(row,null,ts);
  }
  
  /** 
   * Get more than one version of all columns for the specified row
   * at a specified timestamp
   * 
   * @param row row key
   * @param ts timestamp
   * @param numVersions number of versions to return
   * @return RowResult is <code>null</code> if row does not exist.
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #get(Get)}
   */
  public RowResult getRow(final String row, final long ts,
      final int numVersions) throws IOException {
    return getRow(Bytes.toBytes(row), null, ts, numVersions, null);
  }
  
  /** 
   * Get more than one version of all columns for the specified row
   * at a specified timestamp
   * 
   * @param row row key
   * @param timestamp timestamp
   * @param numVersions number of versions to return
   * @return RowResult is <code>null</code> if row does not exist.
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #get(Get)}
   */
  public RowResult getRow(final byte[] row, final long timestamp,
      final int numVersions) throws IOException {
    return getRow(row, null, timestamp, numVersions, null);
  }

  /** 
   * Get selected columns for the specified row at the latest timestamp
   * 
   * @param row row key
   * @param columns Array of column names and families you want to retrieve.
   * @return RowResult is <code>null</code> if row does not exist.
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #get(Get)}
   */
  public RowResult getRow(final String row, final String [] columns) 
  throws IOException {
    return getRow(Bytes.toBytes(row), Bytes.toByteArrays(columns));
  }

  /** 
   * Get selected columns for the specified row at the latest timestamp
   * 
   * @param row row key
   * @param columns Array of column names and families you want to retrieve.
   * @return RowResult is <code>null</code> if row does not exist.
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #get(Get)}
   */
  public RowResult getRow(final byte [] row, final byte [][] columns) 
  throws IOException {
    return getRow(row, columns, HConstants.LATEST_TIMESTAMP);
  }
  
  /** 
   * Get more than one version of selected columns for the specified row
   * 
   * @param row row key
   * @param columns Array of column names and families you want to retrieve.
   * @param numVersions number of versions to return
   * @return RowResult is <code>null</code> if row does not exist.
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #get(Get)}
   */
  public RowResult getRow(final String row, final String[] columns,
      final int numVersions) throws IOException {
    return getRow(Bytes.toBytes(row), Bytes.toByteArrays(columns),
                  HConstants.LATEST_TIMESTAMP, numVersions, null);
  }
  
  /** 
   * Get more than one version of selected columns for the specified row
   * 
   * @param row row key
   * @param columns Array of column names and families you want to retrieve.
   * @param numVersions number of versions to return
   * @return RowResult is <code>null</code> if row does not exist.
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #get(Get)}
   */
  public RowResult getRow(final byte[] row, final byte[][] columns,
      final int numVersions) throws IOException {
    return getRow(row, columns, HConstants.LATEST_TIMESTAMP, numVersions, null);
  }

  /** 
   * Get selected columns for the specified row at a specified timestamp
   * 
   * @param row row key
   * @param columns Array of column names and families you want to retrieve.
   * @param ts timestamp
   * @return RowResult is <code>null</code> if row does not exist.
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #get(Get)}
   */
  public RowResult getRow(final String row, final String [] columns, 
    final long ts) 
  throws IOException {  
    return getRow(Bytes.toBytes(row), Bytes.toByteArrays(columns), ts);
  }

  /** 
   * Get selected columns for the specified row at a specified timestamp
   * 
   * @param row row key
   * @param columns Array of column names and families you want to retrieve.
   * @param ts timestamp
   * @return RowResult is <code>null</code> if row does not exist.
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #get(Get)}
   */
  public RowResult getRow(final byte [] row, final byte [][] columns, 
    final long ts) 
  throws IOException {       
    return getRow(row,columns,ts,1,null);
  }
  
  /** 
   * Get more than one version of selected columns for the specified row,
   * using an existing row lock.
   * 
   * @param row row key
   * @param columns Array of column names and families you want to retrieve.
   * @param numVersions number of versions to return
   * @param rowLock previously acquired row lock
   * @return RowResult is <code>null</code> if row does not exist.
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #get(Get)}
   */
  public RowResult getRow(final String row, final String[] columns,
      final long timestamp, final int numVersions, final RowLock rowLock)
  throws IOException {
    return getRow(Bytes.toBytes(row), Bytes.toByteArrays(columns), timestamp,
                  numVersions, rowLock);
  }

  /** 
   * Get selected columns for the specified row at a specified timestamp
   * using existing row lock.
   * 
   * @param row row key
   * @param columns Array of column names and families you want to retrieve.
   * @param ts timestamp
   * @param numVersions 
   * @param rl row lock
   * @return RowResult is <code>null</code> if row does not exist.
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #get(Get)}
   */
  public RowResult getRow(final byte [] row, final byte [][] columns, 
    final long ts, final int numVersions, final RowLock rl) 
  throws IOException {
    Get g = rl != null? new Get(row, rl): new Get(row);
    if (columns != null) {
      for (int i = 0; i < columns.length; i++) {
        byte[][] splits = KeyValue.parseColumn(columns[i]);
        if (splits[1].length == 0) {
          g.addFamily(splits[0]);
        } else {
          g.addColumn(splits[0], splits[1]);
        }
      }
    }
    g.setMaxVersions(numVersions);
    g.setTimeRange(0,  
        ts == HConstants.LATEST_TIMESTAMP ? ts : ts+1);
    Result r = get(g);
    return r == null || r.size() <= 0? null: r.getRowResult();
  }

  /** 
   * Get a scanner on the current table starting at first row.
   * Return the specified columns.
   *
   * @param columns columns to scan. If column name is a column family, all
   * columns of the specified column family are returned.  Its also possible
   * to pass a regex in the column qualifier. A column qualifier is judged to
   * be a regex if it contains at least one of the following characters:
   * <code>\+|^&*$[]]}{)(</code>.
   * @return scanner
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #getScanner(Scan)}
   */
  public Scanner getScanner(final String [] columns)
  throws IOException {
    return getScanner(Bytes.toByteArrays(columns), HConstants.EMPTY_START_ROW);
  }

  /** 
   * Get a scanner on the current table starting at the specified row.
   * Return the specified columns.
   *
   * @param columns columns to scan. If column name is a column family, all
   * columns of the specified column family are returned.  Its also possible
   * to pass a regex in the column qualifier. A column qualifier is judged to
   * be a regex if it contains at least one of the following characters:
   * <code>\+|^&*$[]]}{)(</code>.
   * @param startRow starting row in table to scan
   * @return scanner
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #getScanner(Scan)}
   */
  public Scanner getScanner(final String [] columns, final String startRow)
  throws IOException {
    return getScanner(Bytes.toByteArrays(columns), Bytes.toBytes(startRow));
  }

  /** 
   * Get a scanner on the current table starting at first row.
   * Return the specified columns.
   *
   * @param columns columns to scan. If column name is a column family, all
   * columns of the specified column family are returned.  Its also possible
   * to pass a regex in the column qualifier. A column qualifier is judged to
   * be a regex if it contains at least one of the following characters:
   * <code>\+|^&*$[]]}{)(</code>.
   * @return scanner
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #getScanner(Scan)}
   */
  public Scanner getScanner(final byte[][] columns)
  throws IOException {
    return getScanner(columns, HConstants.EMPTY_START_ROW,
      HConstants.LATEST_TIMESTAMP, null);
  }

  /** 
   * Get a scanner on the current table starting at the specified row.
   * Return the specified columns.
   *
   * @param columns columns to scan. If column name is a column family, all
   * columns of the specified column family are returned.  Its also possible
   * to pass a regex in the column qualifier. A column qualifier is judged to
   * be a regex if it contains at least one of the following characters:
   * <code>\+|^&*$[]]}{)(</code>.
   * @param startRow starting row in table to scan
   * @return scanner
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #getScanner(Scan)}
   */
  public Scanner getScanner(final byte[][] columns, final byte [] startRow)
  throws IOException {
    return getScanner(columns, startRow, HConstants.LATEST_TIMESTAMP, null);
  }
  
  /** 
   * Get a scanner on the current table starting at the specified row.
   * Return the specified columns.
   *
   * @param columns columns to scan. If column name is a column family, all
   * columns of the specified column family are returned.  Its also possible
   * to pass a regex in the column qualifier. A column qualifier is judged to
   * be a regex if it contains at least one of the following characters:
   * <code>\+|^&*$[]]}{)(</code>.
   * @param startRow starting row in table to scan
   * @param timestamp only return results whose timestamp <= this value
   * @return scanner
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #getScanner(Scan)}
   */
  public Scanner getScanner(final byte[][] columns, final byte [] startRow,
    long timestamp)
  throws IOException {
    return getScanner(columns, startRow, timestamp, null);
  }
  
  /** 
   * Get a scanner on the current table starting at the specified row.
   * Return the specified columns.
   *
   * @param columns columns to scan. If column name is a column family, all
   * columns of the specified column family are returned.  Its also possible
   * to pass a regex in the column qualifier. A column qualifier is judged to
   * be a regex if it contains at least one of the following characters:
   * <code>\+|^&*$[]]}{)(</code>.
   * @param startRow starting row in table to scan
   * @param filter a row filter using row-key regexp and/or column data filter.
   * @return scanner
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #getScanner(Scan)}
   */
  public Scanner getScanner(final byte[][] columns, final byte [] startRow,
    RowFilterInterface filter)
  throws IOException { 
    return getScanner(columns, startRow, HConstants.LATEST_TIMESTAMP, filter);
  }
  
  /** 
   * Get a scanner on the current table starting at the specified row and
   * ending just before <code>stopRow<code>.
   * Return the specified columns.
   *
   * @param columns columns to scan. If column name is a column family, all
   * columns of the specified column family are returned.  Its also possible
   * to pass a regex in the column qualifier. A column qualifier is judged to
   * be a regex if it contains at least one of the following characters:
   * <code>\+|^&*$[]]}{)(</code>.
   * @param startRow starting row in table to scan
   * @param stopRow Row to stop scanning on. Once we hit this row we stop
   * returning values; i.e. we return the row before this one but not the
   * <code>stopRow</code> itself.
   * @return scanner
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #getScanner(Scan)}
   */
  public Scanner getScanner(final byte [][] columns,
    final byte [] startRow, final byte [] stopRow)
  throws IOException {
    return getScanner(columns, startRow, stopRow, HConstants.LATEST_TIMESTAMP);
  }

  /** 
   * Get a scanner on the current table starting at the specified row and
   * ending just before <code>stopRow<code>.
   * Return the specified columns.
   *
   * @param columns columns to scan. If column name is a column family, all
   * columns of the specified column family are returned.  Its also possible
   * to pass a regex in the column qualifier. A column qualifier is judged to
   * be a regex if it contains at least one of the following characters:
   * <code>\+|^&*$[]]}{)(</code>.
   * @param startRow starting row in table to scan
   * @param stopRow Row to stop scanning on. Once we hit this row we stop
   * returning values; i.e. we return the row before this one but not the
   * <code>stopRow</code> itself.
   * @param timestamp only return results whose timestamp <= this value
   * @return scanner
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #getScanner(Scan)}
   */
  public Scanner getScanner(final String [] columns,
    final String startRow, final String stopRow, final long timestamp)
  throws IOException {
    return getScanner(Bytes.toByteArrays(columns), Bytes.toBytes(startRow),
      Bytes.toBytes(stopRow), timestamp);
  }

  /** 
   * Get a scanner on the current table starting at the specified row and
   * ending just before <code>stopRow<code>.
   * Return the specified columns.
   *
   * @param columns columns to scan. If column name is a column family, all
   * columns of the specified column family are returned.  Its also possible
   * to pass a regex in the column qualifier. A column qualifier is judged to
   * be a regex if it contains at least one of the following characters:
   * <code>\+|^&*$[]]}{)(</code>.
   * @param startRow starting row in table to scan
   * @param stopRow Row to stop scanning on. Once we hit this row we stop
   * returning values; i.e. we return the row before this one but not the
   * <code>stopRow</code> itself.
   * @param timestamp only return results whose timestamp <= this value
   * @return scanner
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #getScanner(Scan)}
   */
  public Scanner getScanner(final byte [][] columns,
    final byte [] startRow, final byte [] stopRow, final long timestamp)
  throws IOException {
    return getScanner(columns, startRow, timestamp,
      new WhileMatchRowFilter(new StopRowFilter(stopRow)));
  }

  /** 
   * Get a scanner on the current table starting at the specified row.
   * Return the specified columns.
   *
   * @param columns columns to scan. If column name is a column family, all
   * columns of the specified column family are returned.  Its also possible
   * to pass a regex in the column qualifier. A column qualifier is judged to
   * be a regex if it contains at least one of the following characters:
   * <code>\+|^&*$[]]}{)(</code>.
   * @param startRow starting row in table to scan
   * @param timestamp only return results whose timestamp <= this value
   * @param filter a row filter using row-key regexp and/or column data filter.
   * @return scanner
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #getScanner(Scan)}
   */
  public Scanner getScanner(String[] columns,
    String startRow, long timestamp, RowFilterInterface filter)
  throws IOException {
    return getScanner(Bytes.toByteArrays(columns), Bytes.toBytes(startRow),
      timestamp, filter);
  }

  /** 
   * Get a scanner on the current table starting at the specified row.
   * Return the specified columns.
   *
   * @param columns columns to scan. If column name is a column family, all
   * columns of the specified column family are returned.  Its also possible
   * to pass a regex in the column qualifier. A column qualifier is judged to
   * be a regex if it contains at least one of the following characters:
   * <code>\+|^&*$[]]}{)(</code>.
   * @param startRow starting row in table to scan
   * @param timestamp only return results whose timestamp <= this value
   * @param filter a row filter using row-key regexp and/or column data filter.
   * @return scanner
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #getScanner(Scan)}
   */
  public Scanner getScanner(final byte [][] columns,
    final byte [] startRow, long timestamp, RowFilterInterface filter)
  throws IOException {
    // Convert old-style filter to new.  We only do a few types at moment.
    // If a whilematchrowfilter and it has a stoprowfilter, handle that.
    Scan scan = filter == null? new Scan(startRow):
      filter instanceof WhileMatchRowFilter && ((WhileMatchRowFilter)filter).getInternalFilter() instanceof StopRowFilter?
          new Scan(startRow, ((StopRowFilter)((WhileMatchRowFilter)filter).getInternalFilter()).getStopRowKey()):
          null /*new UnsupportedOperationException("Not handled yet")*/;
    for (int i = 0; i < columns.length; i++) {
      byte [][] splits = KeyValue.parseColumn(columns[i]);
      if (splits[1].length == 0) {
        scan.addFamily(splits[0]);
      } else {
        scan.addColumn(splits[0], splits[1]);
      }
    }
    scan.setTimeRange(0,  
        timestamp == HConstants.LATEST_TIMESTAMP ? timestamp : timestamp+1);
    OldClientScanner s = new OldClientScanner(new ClientScanner(scan));
    s.initialize();
    return s;
  }

  /**
   * Completely delete the row's cells.
   *
   * @param row Key of the row you want to completely delete.
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #delete(Delete)}
   */
  public void deleteAll(final byte [] row) throws IOException {
    deleteAll(row, null);
  }

  /**
   * Completely delete the row's cells.
   *
   * @param row Key of the row you want to completely delete.
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #delete(Delete)}
   */
  public void deleteAll(final String row) throws IOException {
    deleteAll(row, null);
  }
  
  /**
   * Completely delete the row's cells.
   *
   * @param row Key of the row you want to completely delete.
   * @param column column to be deleted
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #delete(Delete)}
   */
  public void deleteAll(final byte [] row, final byte [] column)
  throws IOException {
    deleteAll(row, column, HConstants.LATEST_TIMESTAMP);
  }

  /**
   * Completely delete the row's cells.
   *
   * @param row Key of the row you want to completely delete.
   * @param ts Delete all cells of the same timestamp or older.
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #delete(Delete)}
   */
  public void deleteAll(final byte [] row, final long ts)
  throws IOException {
    deleteAll(row, null, ts);
  }

  /**
   * Completely delete the row's cells.
   *
   * @param row Key of the row you want to completely delete.
   * @param ts Delete all cells of the same timestamp or older.
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #delete(Delete)}
   */
  public void deleteAll(final String row, final long ts)
  throws IOException {
    deleteAll(row, null, ts);
  }

  /** 
   * Delete all cells that match the passed row and column.
   * @param row Row to update
   * @param column name of column whose value is to be deleted
   * @throws IOException 
   * @deprecated As of hbase 0.20.0, replaced by {@link #delete(Delete)}
   */
  public void deleteAll(final String row, final String column)
  throws IOException {
    deleteAll(row, column, HConstants.LATEST_TIMESTAMP);
  }

  /** 
   * Delete all cells that match the passed row and column and whose
   * timestamp is equal-to or older than the passed timestamp.
   * @param row Row to update
   * @param column name of column whose value is to be deleted
   * @param ts Delete all cells of the same timestamp or older.
   * @throws IOException 
   * @deprecated As of hbase 0.20.0, replaced by {@link #delete(Delete)}
   */
  public void deleteAll(final String row, final String column, final long ts)
  throws IOException {
    deleteAll(Bytes.toBytes(row),
      column != null? Bytes.toBytes(column): null, ts);
  }

  /** 
   * Delete all cells that match the passed row and column and whose
   * timestamp is equal-to or older than the passed timestamp.
   * @param row Row to update
   * @param column name of column whose value is to be deleted
   * @param ts Delete all cells of the same timestamp or older.
   * @throws IOException 
   * @deprecated As of hbase 0.20.0, replaced by {@link #delete(Delete)}
   */
  public void deleteAll(final byte [] row, final byte [] column, final long ts)
  throws IOException {
    deleteAll(row,column,ts,null);
  }

  /** 
   * Delete all cells that match the passed row and column and whose
   * timestamp is equal-to or older than the passed timestamp, using an
   * existing row lock.
   * @param row Row to update
   * @param column name of column whose value is to be deleted
   * @param ts Delete all cells of the same timestamp or older.
   * @param rl Existing row lock
   * @throws IOException 
   * @deprecated As of hbase 0.20.0, replaced by {@link #delete(Delete)}
   */
  public void deleteAll(final byte [] row, final byte [] column, final long ts,
      final RowLock rl)
  throws IOException {
    Delete d = new Delete(row, ts, rl);
    if(column != null) {
      d.deleteColumns(column, ts);
    }
    delete(d);
  }
  
  /** 
   * Delete all cells that match the passed row and column.
   * @param row Row to update
   * @param colRegex column regex expression
   * @throws IOException 
   * @deprecated As of hbase 0.20.0, replaced by {@link #delete(Delete)}
   */
  public void deleteAllByRegex(final String row, final String colRegex)
  throws IOException {
    deleteAllByRegex(row, colRegex, HConstants.LATEST_TIMESTAMP);
  }

  /** 
   * Delete all cells that match the passed row and column and whose
   * timestamp is equal-to or older than the passed timestamp.
   * @param row Row to update
   * @param colRegex Column Regex expression
   * @param ts Delete all cells of the same timestamp or older.
   * @throws IOException 
   * @deprecated As of hbase 0.20.0, replaced by {@link #delete(Delete)}
   */
  public void deleteAllByRegex(final String row, final String colRegex, 
      final long ts) throws IOException {
    deleteAllByRegex(Bytes.toBytes(row), colRegex, ts);
  }

  /** 
   * Delete all cells that match the passed row and column and whose
   * timestamp is equal-to or older than the passed timestamp.
   * @param row Row to update
   * @param colRegex Column Regex expression
   * @param ts Delete all cells of the same timestamp or older.
   * @throws IOException 
   * @deprecated As of hbase 0.20.0, replaced by {@link #delete(Delete)}
   */
  public void deleteAllByRegex(final byte [] row, final String colRegex, 
      final long ts) throws IOException {
    deleteAllByRegex(row, colRegex, ts, null);
  }
  
  /** 
   * Delete all cells that match the passed row and column and whose
   * timestamp is equal-to or older than the passed timestamp, using an
   * existing row lock.
   * @param row Row to update
   * @param colRegex Column regex expression
   * @param ts Delete all cells of the same timestamp or older.
   * @param rl Existing row lock
   * @throws IOException 
   * @deprecated As of hbase 0.20.0, replaced by {@link #delete(Delete)}
   */
  public void deleteAllByRegex(final byte [] row, final String colRegex, 
      final long ts, final RowLock rl)
  throws IOException {
    throw new UnsupportedOperationException("TODO: Not yet implemented");
  }

  /**
   * Delete all cells for a row with matching column family at all timestamps.
   *
   * @param row The row to operate on
   * @param family The column family to match
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #delete(Delete)}
   */
  public void deleteFamily(final String row, final String family) 
  throws IOException {
    deleteFamily(row, family, HConstants.LATEST_TIMESTAMP);
  }

  /**
   * Delete all cells for a row with matching column family at all timestamps.
   *
   * @param row The row to operate on
   * @param family The column family to match
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #delete(Delete)}
   */
  public void deleteFamily(final byte[] row, final byte[] family) 
  throws IOException {
    deleteFamily(row, family, HConstants.LATEST_TIMESTAMP);
  }

  /**
   * Delete all cells for a row with matching column family with timestamps
   * less than or equal to <i>timestamp</i>.
   *
   * @param row The row to operate on
   * @param family The column family to match
   * @param timestamp Timestamp to match
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #delete(Delete)}
   */  
  public void deleteFamily(final String row, final String family,
      final long timestamp)
  throws IOException{
    deleteFamily(Bytes.toBytes(row), Bytes.toBytes(family), timestamp);
  }

  /**
   * Delete all cells for a row with matching column family with timestamps
   * less than or equal to <i>timestamp</i>.
   *
   * @param row The row to operate on
   * @param family The column family to match
   * @param timestamp Timestamp to match
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #delete(Delete)}
   */
  public void deleteFamily(final byte [] row, final byte [] family, 
    final long timestamp)
  throws IOException {
    deleteFamily(row,family,timestamp,null);
  }

  /**
   * Delete all cells for a row with matching column family with timestamps
   * less than or equal to <i>timestamp</i>, using existing row lock.
   *
   * @param row The row to operate on
   * @param family The column family to match
   * @param timestamp Timestamp to match
   * @param rl Existing row lock
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #delete(Delete)}
   */
  public void deleteFamily(final byte [] row, final byte [] family, 
    final long timestamp, final RowLock rl)
  throws IOException {
    Delete d = new Delete(row, HConstants.LATEST_TIMESTAMP, rl);
    d.deleteFamily(stripColon(family), timestamp);
    delete(d);
  }
  
  /**
   * Delete all cells for a row with matching column family regex 
   * at all timestamps.
   *
   * @param row The row to operate on
   * @param familyRegex Column family regex
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #delete(Delete)}
   */
  public void deleteFamilyByRegex(final String row, final String familyRegex) 
  throws IOException {
    deleteFamilyByRegex(row, familyRegex, HConstants.LATEST_TIMESTAMP);
  }

  /**
   * Delete all cells for a row with matching column family regex 
   * at all timestamps.
   *
   * @param row The row to operate on
   * @param familyRegex Column family regex
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #delete(Delete)}
   */
  public void deleteFamilyByRegex(final byte[] row, final String familyRegex) 
  throws IOException {
    deleteFamilyByRegex(row, familyRegex, HConstants.LATEST_TIMESTAMP);
  }

  /**
   * Delete all cells for a row with matching column family regex
   * with timestamps less than or equal to <i>timestamp</i>.
   *
   * @param row The row to operate on
   * @param familyRegex Column family regex
   * @param timestamp Timestamp to match
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #delete(Delete)}
   */  
  public void deleteFamilyByRegex(final String row, final String familyRegex,
      final long timestamp)
  throws IOException{
    deleteFamilyByRegex(Bytes.toBytes(row), familyRegex, timestamp);
  }

  /**
   * Delete all cells for a row with matching column family regex
   * with timestamps less than or equal to <i>timestamp</i>.
   *
   * @param row The row to operate on
   * @param familyRegex Column family regex
   * @param timestamp Timestamp to match
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #delete(Delete)}
   */
  public void deleteFamilyByRegex(final byte [] row, final String familyRegex, 
    final long timestamp)
  throws IOException {
    deleteFamilyByRegex(row,familyRegex,timestamp,null);
  }
  
  /**
   * Delete all cells for a row with matching column family regex with
   * timestamps less than or equal to <i>timestamp</i>, using existing
   * row lock.
   * 
   * @param row The row to operate on
   * @param familyRegex Column Family Regex
   * @param timestamp Timestamp to match
   * @param r1 Existing row lock
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #delete(Delete)}
   */
  public void deleteFamilyByRegex(final byte[] row, final String familyRegex,
    final long timestamp, final RowLock r1)
  throws IOException {
    throw new UnsupportedOperationException("TODO: Not yet implemented");
  }

  /**
   * Test for the existence of a row in the table.
   * 
   * @param row The row
   * @return true if the row exists, false otherwise
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #exists(Get)}
   */
  public boolean exists(final byte [] row) throws IOException {
    return exists(row, null, HConstants.LATEST_TIMESTAMP, null);
  }

  /**
   * Test for the existence of a row and column in the table.
   * 
   * @param row The row
   * @param column The column
   * @return true if the row exists, false otherwise
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #exists(Get)}
   */
  public boolean exists(final byte [] row, final byte[] column)
  throws IOException {
    return exists(row, column, HConstants.LATEST_TIMESTAMP, null);
  }

  /**
   * Test for the existence of a coordinate in the table.
   * 
   * @param row The row
   * @param column The column
   * @param timestamp The timestamp
   * @return true if the specified coordinate exists
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #exists(Get)}
   */
  public boolean exists(final byte [] row, final byte [] column,
      long timestamp) throws IOException {
    return exists(row, column, timestamp, null);
  }

  /**
   * Test for the existence of a coordinate in the table.
   * 
   * @param row The row
   * @param column The column
   * @param timestamp The timestamp
   * @param rl Existing row lock
   * @return true if the specified coordinate exists
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #exists(Get)}
   */
  public boolean exists(final byte [] row, final byte [] column,
      final long timestamp, final RowLock rl) throws IOException {
    final Get g = new Get(row, rl);
    g.addColumn(column);
    g.setTimeRange(0,  
        timestamp == HConstants.LATEST_TIMESTAMP ? timestamp : timestamp+1);
    return exists(g);
  }

  /**
   * Commit a BatchUpdate to the table.
   * If autoFlush is false, the update is buffered
   * @param batchUpdate
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #delete(Delete)} or
   * {@link #put(Put)}
   */ 
  public synchronized void commit(final BatchUpdate batchUpdate) 
  throws IOException {
    commit(batchUpdate, null);
  }
  
  /**
   * Commit a BatchUpdate to the table using existing row lock.
   * If autoFlush is false, the update is buffered
   * @param batchUpdate
   * @param rl Existing row lock
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #delete(Delete)} or
   * {@link #put(Put)}
   */ 
  public synchronized void commit(final BatchUpdate batchUpdate,
      final RowLock rl) 
  throws IOException {
    for (BatchOperation bo: batchUpdate) {
      if (!bo.isPut()) throw new IOException("Only Puts in BU as of 0.20.0");
      Put p = new Put(batchUpdate.getRow(), rl);
      p.add(bo.getColumn(), batchUpdate.getTimestamp(), bo.getValue());
      put(p);
    }
  }

  /**
   * Commit a List of BatchUpdate to the table.
   * If autoFlush is false, the updates are buffered
   * @param batchUpdates
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #delete(Delete)} or
   * {@link #put(List)}
   */ 
  public synchronized void commit(final List<BatchUpdate> batchUpdates)
      throws IOException {
    // Am I breaking something here in old API by doing this?
    for (BatchUpdate bu : batchUpdates) {
      commit(bu);
    }
  }
  
  /**
   * Atomically checks if a row's values match the expectedValues. 
   * If it does, it uses the batchUpdate to update the row.<p>
   * 
   * This operation is not currently supported, use {@link #checkAndPut}
   * @param batchUpdate batchupdate to apply if check is successful
   * @param expectedValues values to check
   * @param rl rowlock
   * @throws IOException
   * @deprecated As of hbase 0.20.0, replaced by {@link #checkAndPut}
   */
  public synchronized boolean checkAndSave(final BatchUpdate batchUpdate,
    final HbaseMapWritable<byte[],byte[]> expectedValues, final RowLock rl)
  throws IOException {
    throw new UnsupportedOperationException("Replaced by checkAndPut");
  }

  /**
   * Implements the scanner interface for the HBase client.
   * If there are multiple regions in a table, this scanner will iterate
   * through them all.
   */
  protected class ClientScanner implements ResultScanner {
    private final Log CLIENT_LOG = LogFactory.getLog(this.getClass());
    // HEADSUP: The scan internal start row can change as we move through table.
    private Scan scan;
    private boolean closed = false;
    // Current region scanner is against.  Gets cleared if current region goes
    // wonky: e.g. if it splits on us.
    private HRegionInfo currentRegion = null;
    private ScannerCallable callable = null;
    private final LinkedList<Result> cache = new LinkedList<Result>();
    private final int caching;
    private long lastNext;
    // Keep lastResult returned successfully in case we have to reset scanner.
    private Result lastResult = null;
    
    protected ClientScanner(final Scan scan) {
      if (CLIENT_LOG.isDebugEnabled()) {
        CLIENT_LOG.debug("Creating scanner over " 
            + Bytes.toString(getTableName()) 
            + " starting at key '" + Bytes.toStringBinary(scan.getStartRow()) + "'");
      }
      this.scan = scan;
      this.lastNext = System.currentTimeMillis();

      // Use the caching from the Scan.  If not set, use the default cache setting for this table.
      if (this.scan.getCaching() > 0) {
        this.caching = this.scan.getCaching();
      } else {
        this.caching = HTable.this.scannerCaching;
      }

      // Removed filter validation.  We have a new format now, only one of all
      // the current filters has a validate() method.  We can add it back,
      // need to decide on what we're going to do re: filter redesign.
      // Need, at the least, to break up family from qualifier as separate
      // checks, I think it's important server-side filters are optimal in that
      // respect.
    }

    public void initialize() throws IOException {
      nextScanner(this.caching, false);
    }

    protected Scan getScan() {
      return scan;
    }
    
    protected long getTimestamp() {
      return lastNext;
    }

   /**
     * @param endKey
     * @return Returns true if the passed region endkey.
     */
    private boolean checkScanStopRow(final byte [] endKey) {
      if (this.scan.getStopRow().length > 0) {
        // there is a stop row, check to see if we are past it.
        byte [] stopRow = scan.getStopRow();
        int cmp = Bytes.compareTo(stopRow, 0, stopRow.length,
          endKey, 0, endKey.length);
        if (cmp <= 0) {
          // stopRow <= endKey (endKey is equals to or larger than stopRow)
          // This is a stop.
          return true;
        }
      }
      return false; //unlikely.
    }

    /*
     * Gets a scanner for the next region.  If this.currentRegion != null, then
     * we will move to the endrow of this.currentRegion.  Else we will get
     * scanner at the scan.getStartRow().  We will go no further, just tidy
     * up outstanding scanners, if <code>currentRegion != null</code> and
     * <code>done</code> is true.
     * @param nbRows
     * @param done Server-side says we're done scanning.
     */
    private boolean nextScanner(int nbRows, final boolean done)
    throws IOException {
      // Close the previous scanner if it's open
      if (this.callable != null) {
        this.callable.setClose();
        getConnection().getRegionServerWithRetries(callable);
        this.callable = null;
      }
      
      // Where to start the next scanner
      byte [] localStartKey = null;

      // if we're at end of table, close and return false to stop iterating
      if (this.currentRegion != null) {
        byte [] endKey = this.currentRegion.getEndKey();
        if (endKey == null ||
            Bytes.equals(endKey, HConstants.EMPTY_BYTE_ARRAY) ||
            checkScanStopRow(endKey) ||
            done) {
          close();
          if (CLIENT_LOG.isDebugEnabled()) {
            CLIENT_LOG.debug("Finished with scanning at " + this.currentRegion);
          }
          return false;
        }
        localStartKey = endKey;
        if (CLIENT_LOG.isDebugEnabled()) {
          CLIENT_LOG.debug("Finished with region " + this.currentRegion);
        }
      } else {
        localStartKey = this.scan.getStartRow();
      }

      if (CLIENT_LOG.isDebugEnabled()) {
        CLIENT_LOG.debug("Advancing internal scanner to startKey at '" +
          Bytes.toStringBinary(localStartKey) + "'");
      }            
      try {
        callable = getScannerCallable(localStartKey, nbRows);
        // Open a scanner on the region server starting at the 
        // beginning of the region
        getConnection().getRegionServerWithRetries(callable);
        this.currentRegion = callable.getHRegionInfo();
      } catch (IOException e) {
        close();
        throw e;
      }
      return true;
    }
    
    protected ScannerCallable getScannerCallable(byte [] localStartKey,
        int nbRows) {
      scan.setStartRow(localStartKey);
      ScannerCallable s = new ScannerCallable(getConnection(), 
        getTableName(), scan);
      s.setCaching(nbRows);
      return s;
    }

    public Result next() throws IOException {
      // If the scanner is closed but there is some rows left in the cache,
      // it will first empty it before returning null
      if (cache.size() == 0 && this.closed) {
        return null;
      }
      if (cache.size() == 0) {
        Result [] values = null;
        long remainingResultSize = maxScannerResultSize;
        int countdown = this.caching;
        // We need to reset it if it's a new callable that was created 
        // with a countdown in nextScanner
        callable.setCaching(this.caching);
        // This flag is set when we want to skip the result returned.  We do
        // this when we reset scanner because it split under us.
        boolean skipFirst = false;
        do {
          try {
            // Server returns a null values if scanning is to stop.  Else,
            // returns an empty array if scanning is to go on and we've just
            // exhausted current region.
            values = getConnection().getRegionServerWithRetries(callable);
            if (skipFirst) {
              skipFirst = false;
              // Reget.
              values = getConnection().getRegionServerWithRetries(callable);
            }
          } catch (DoNotRetryIOException e) {
            long timeout = lastNext + scannerTimeout;
            if (e instanceof UnknownScannerException &&
                timeout < System.currentTimeMillis()) {
              long elapsed = System.currentTimeMillis() - lastNext;
              ScannerTimeoutException ex = new ScannerTimeoutException(
                  elapsed + "ms passed since the last invocation, " +
                      "timeout is currently set to " + scannerTimeout);
              ex.initCause(e);
              throw ex;
            }
            Throwable cause = e.getCause();
            if (cause == null || !(cause instanceof NotServingRegionException)) {
              throw e;
            }
            // Else, its signal from depths of ScannerCallable that we got an
            // NSRE on a next and that we need to reset the scanner.
            if (this.lastResult != null) {
              this.scan.setStartRow(this.lastResult.getRow());
              // Skip first row returned.  We already let it out on previous
              // invocation.
              skipFirst = true;
            }
            // Clear region
            this.currentRegion = null;
            continue;
          }
          lastNext = System.currentTimeMillis();
          if (values != null && values.length > 0) {
            for (Result rs : values) {
              cache.add(rs);
              for (KeyValue kv : rs.raw()) {
                  remainingResultSize -= kv.heapSize();
              }
              countdown--;
              this.lastResult = rs;
            }
          }
          // Values == null means server-side filter has determined we must STOP
        } while (remainingResultSize > 0 && countdown > 0 && nextScanner(countdown, values == null));
      }

      if (cache.size() > 0) {
        return cache.poll();
      }
      return null;
    }

    /**
     * Get <param>nbRows</param> rows.
     * How many RPCs are made is determined by the {@link Scan#setCaching(int)}
     * setting (or hbase.client.scanner.caching in hbase-site.xml).
     * @param nbRows number of rows to return
     * @return Between zero and <param>nbRows</param> RowResults.  Scan is done
     * if returned array is of zero-length (We never return null).
     * @throws IOException
     */
    public Result [] next(int nbRows) throws IOException {
      // Collect values to be returned here
      ArrayList<Result> resultSets = new ArrayList<Result>(nbRows);
      for(int i = 0; i < nbRows; i++) {
        Result next = next();
        if (next != null) {
          resultSets.add(next);
        } else {
          break;
        }
      }
      return resultSets.toArray(new Result[resultSets.size()]);
    }

    public void close() {
      if (callable != null) {
        callable.setClose();
        try {
          getConnection().getRegionServerWithRetries(callable);
        } catch (IOException e) {
          // We used to catch this error, interpret, and rethrow. However, we
          // have since decided that it's not nice for a scanner's close to
          // throw exceptions. Chances are it was just an UnknownScanner
          // exception due to lease time out.
        }
        callable = null;
      }
      closed = true;
    }

    public Iterator<Result> iterator() {
      return new Iterator<Result>() {
        // The next RowResult, possibly pre-read
        Result next = null;
        
        // return true if there is another item pending, false if there isn't.
        // this method is where the actual advancing takes place, but you need
        // to call next() to consume it. hasNext() will only advance if there
        // isn't a pending next().
        public boolean hasNext() {
          if (next == null) {
            try {
              next = ClientScanner.this.next();
              return next != null;
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          }
          return true;
        }

        // get the pending next item and advance the iterator. returns null if
        // there is no next item.
        public Result next() {
          // since hasNext() does the real advancing, we call this to determine
          // if there is a next before proceeding.
          if (!hasNext()) {
            return null;
          }
          
          // if we get to here, then hasNext() has given us an item to return.
          // we want to return the item and then null out the next pointer, so
          // we use a temporary variable.
          Result temp = next;
          next = null;
          return temp;
        }

        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    }
  }

  /**
   * {@link Scanner} implementation made on top of a {@link ResultScanner}.
   */
  protected class OldClientScanner implements Scanner {
    private final ClientScanner cs;
 
    OldClientScanner(final ClientScanner cs) {
      this.cs = cs;
    }

    protected void initialize() throws IOException {
      this.cs.initialize();
    }

    @Override
    public void close() {
      this.cs.close();
    }

    @Override
    public RowResult next() throws IOException {
      Result r = this.cs.next();
      return r == null || r.isEmpty()? null: r.getRowResult();
    }

    @Override
    public RowResult [] next(int nbRows) throws IOException {
      Result [] rr = this.cs.next(nbRows);
      if (rr == null || rr.length == 0) return null;
      RowResult [] results = new RowResult[rr.length];
      for (int i = 0; i < rr.length; i++) {
        results[i] = rr[i].getRowResult();
      }
      return results;
    }

    @Override
    public Iterator<RowResult> iterator() {
      return new Iterator<RowResult>() {
        // The next RowResult, possibly pre-read
        RowResult next = null;
        
        // return true if there is another item pending, false if there isn't.
        // this method is where the actual advancing takes place, but you need
        // to call next() to consume it. hasNext() will only advance if there
        // isn't a pending next().
        public boolean hasNext() {
          if (next == null) {
            try {
              next = OldClientScanner.this.next();
              return next != null;
            } catch (IOException e) {
              throw new RuntimeException(e);
            }            
          }
          return true;
        }

        // get the pending next item and advance the iterator. returns null if
        // there is no next item.
        public RowResult next() {
          // since hasNext() does the real advancing, we call this to determine
          // if there is a next before proceeding.
          if (!hasNext()) {
            return null;
          }
          
          // if we get to here, then hasNext() has given us an item to return.
          // we want to return the item and then null out the next pointer, so
          // we use a temporary variable.
          RowResult temp = next;
          next = null;
          return temp;
        }

        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    }
  }
  
  private static byte [] stripColon(final byte [] n) {
    byte col = n[n.length-1];
    if (col == ':') {
      byte [] res = new byte[n.length-1];
      System.arraycopy(n, 0, res, 0, n.length-1);
      return res;
    }
    return n;
  }
}
