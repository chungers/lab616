/**
 * Copyright 2007 The Apache Software Foundation
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
package org.apache.hadoop.hbase.regionserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseTestCase;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Reader;


/** JUnit test case for HLog */
public class TestHLog extends HBaseTestCase implements HConstants {
  private Path dir;
  private MiniDFSCluster cluster;

  @Override
  public void setUp() throws Exception {
    cluster = new MiniDFSCluster(conf, 2, true, (String[])null);
    // Set the hbase.rootdir to be the home directory in mini dfs.
    this.conf.set(HConstants.HBASE_DIR,
      this.cluster.getFileSystem().getHomeDirectory().toString());
    super.setUp();
    this.dir = new Path("/hbase", getName());
    if (fs.exists(dir)) {
      fs.delete(dir, true);
    }
  }

  @Override
  public void tearDown() throws Exception {
    if (this.fs.exists(this.dir)) {
      this.fs.delete(this.dir, true);
    }
    shutdownDfs(cluster);
    super.tearDown();
  }

  /**
   * Test the findMemstoresWithEditsOlderThan method.
   * @throws IOException
   */
  public void testFindMemstoresWithEditsOlderThan() throws IOException {
    Map<byte [], Long> regionsToSeqids = new HashMap<byte [], Long>();
    for (int i = 0; i < 10; i++) {
      Long l = new Long(i);
      regionsToSeqids.put(l.toString().getBytes(), l);
    }
    byte [][] regions =
      HLog.findMemstoresWithEditsOlderThan(1, regionsToSeqids);
    assertEquals(1, regions.length);
    assertTrue(Bytes.equals(regions[0], "0".getBytes()));
    regions = HLog.findMemstoresWithEditsOlderThan(3, regionsToSeqids);
    int count = 3;
    assertEquals(count, regions.length);
    // Regions returned are not ordered.
    for (int i = 0; i < count; i++) {
      assertTrue(Bytes.equals(regions[i], "0".getBytes()) ||
        Bytes.equals(regions[i], "1".getBytes()) ||
        Bytes.equals(regions[i], "2".getBytes()));
    }
  }
 
  /**
   * Just write multiple logs then split.  Before fix for HADOOP-2283, this
   * would fail.
   * @throws IOException
   */
  public void testSplit() throws IOException {
    final byte [] tableName = Bytes.toBytes(getName());
    final byte [] rowName = tableName;
    HLog log = new HLog(this.fs, this.dir, this.conf, null);
    final int howmany = 3;
    // Add edits for three regions.
    try {
      for (int ii = 0; ii < howmany; ii++) {
        for (int i = 0; i < howmany; i++) {
          for (int j = 0; j < howmany; j++) {
            List<KeyValue> edit = new ArrayList<KeyValue>();
            byte [] column = Bytes.toBytes("column:" + Integer.toString(j));
            edit.add(new KeyValue(rowName, column, System.currentTimeMillis(),
              column));
            System.out.println("Region " + i + ": " + edit);
            log.append(Bytes.toBytes("" + i), tableName, edit,
              false, System.currentTimeMillis());
          }
        }
        log.rollWriter();
      }
      List<Path> splits =
        HLog.splitLog(this.testDir, this.dir, this.fs, this.conf);
      verifySplits(splits, howmany);
      log = null;
    } finally {
      if (log != null) {
        log.closeAndDelete();
      }
    }
  }

  private void verifySplits(List<Path> splits, final int howmany)
  throws IOException {
    assertEquals(howmany, splits.size());
    for (int i = 0; i < splits.size(); i++) {
      SequenceFile.Reader r =
        new SequenceFile.Reader(this.fs, splits.get(i), this.conf);
      try {
        HLogKey key = new HLogKey();
        KeyValue kv = new KeyValue();
        int count = 0;
        String previousRegion = null;
        long seqno = -1;
        while(r.next(key, kv)) {
          String region = Bytes.toString(key.getRegionName());
          // Assert that all edits are for same region.
          if (previousRegion != null) {
            assertEquals(previousRegion, region);
          }
          assertTrue(seqno < key.getLogSeqNum());
          seqno = key.getLogSeqNum();
          previousRegion = region;
          System.out.println(key + " " + kv);
          count++;
        }
        assertEquals(howmany * howmany, count);
      } finally {
        r.close();
      }
    }
  }

  /**
   * @throws IOException
   */
  public void testAppend() throws IOException {
    final int COL_COUNT = 10;
    final byte [] regionName = Bytes.toBytes("regionname");
    final byte [] tableName = Bytes.toBytes("tablename");
    final byte [] row = Bytes.toBytes("row");
    Reader reader = null;
    HLog log = new HLog(fs, dir, this.conf, null);
    try {
      // Write columns named 1, 2, 3, etc. and then values of single byte
      // 1, 2, 3...
      long timestamp = System.currentTimeMillis();
      List<KeyValue> cols = new ArrayList<KeyValue>();
      for (int i = 0; i < COL_COUNT; i++) {
        cols.add(new KeyValue(row, Bytes.toBytes("column:" + Integer.toString(i)),
          timestamp, new byte[] { (byte)(i + '0') }));
      }
      log.append(regionName, tableName, cols, false, System.currentTimeMillis());
      long logSeqId = log.startCacheFlush();
      log.completeCacheFlush(regionName, tableName, logSeqId);
      log.close();
      Path filename = log.computeFilename(log.getFilenum());
      log = null;
      // Now open a reader on the log and assert append worked.
      reader = new SequenceFile.Reader(fs, filename, conf);
      HLogKey key = new HLogKey();
      KeyValue val = new KeyValue();
      for (int i = 0; i < COL_COUNT; i++) {
        reader.next(key, val);
        assertTrue(Bytes.equals(regionName, key.getRegionName()));
        assertTrue(Bytes.equals(tableName, key.getTablename()));
        assertTrue(Bytes.equals(row, val.getRow()));
        assertEquals((byte)(i + '0'), val.getValue()[0]);
        System.out.println(key + " " + val);
      }
      while (reader.next(key, val)) {
        // Assert only one more row... the meta flushed row.
        assertTrue(Bytes.equals(regionName, key.getRegionName()));
        assertTrue(Bytes.equals(tableName, key.getTablename()));
        assertTrue(Bytes.equals(HLog.METAROW, val.getRow()));
        assertTrue(Bytes.equals(HLog.METAFAMILY, val.getFamily()));
        assertEquals(0, Bytes.compareTo(HLog.COMPLETE_CACHE_FLUSH,
          val.getValue()));
        System.out.println(key + " " + val);
      }
    } finally {
      if (log != null) {
        log.closeAndDelete();
      }
      if (reader != null) {
        reader.close();
      }
    }
  }
}