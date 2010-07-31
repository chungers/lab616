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
package org.apache.hadoop.hbase.mapred;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Random;

import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.regionserver.HRegion;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.io.Cell;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MultiRegionTable;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MiniMRCluster;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;

/**
 * Test Map/Reduce job to build index over HBase table
 */
public class TestTableIndex extends MultiRegionTable {
  private static final Log LOG = LogFactory.getLog(TestTableIndex.class);

  static final String TABLE_NAME = "moretest";
  static final String INPUT_COLUMN = "contents:";
  static final byte [] TEXT_INPUT_COLUMN = Bytes.toBytes(INPUT_COLUMN);
  static final String OUTPUT_COLUMN = "text:";
  static final byte [] TEXT_OUTPUT_COLUMN = Bytes.toBytes(OUTPUT_COLUMN);
  static final String ROWKEY_NAME = "key";
  static final String INDEX_DIR = "testindex";
  private static final byte [][] columns = new byte [][] {
    TEXT_INPUT_COLUMN,
    TEXT_OUTPUT_COLUMN
  };

  private JobConf jobConf = null;

  /** default constructor */
  public TestTableIndex() {
    super(INPUT_COLUMN);
    desc = new HTableDescriptor(TABLE_NAME);
    desc.addFamily(new HColumnDescriptor(INPUT_COLUMN));
    desc.addFamily(new HColumnDescriptor(OUTPUT_COLUMN));
  }

    @Override
  public void tearDown() throws Exception {
    if (jobConf != null) {
      FileUtil.fullyDelete(new File(jobConf.get("hadoop.tmp.dir")));
    }
  }

  /**
   * Test HBase map/reduce
   * 
   * @throws IOException
   */
  public void testTableIndex() throws IOException {
    boolean printResults = false;
    if (printResults) {
      LOG.info("Print table contents before map/reduce");
    }
    scanTable(printResults);

    MiniMRCluster mrCluster = new MiniMRCluster(2, fs.getUri().toString(), 1);

    // set configuration parameter for index build
    conf.set("hbase.index.conf", createIndexConfContent());

    try {
      jobConf = new JobConf(conf, TestTableIndex.class);
      jobConf.setJobName("index column contents");
      jobConf.setNumMapTasks(2);
      // number of indexes to partition into
      jobConf.setNumReduceTasks(1);

      // use identity map (a waste, but just as an example)
      IdentityTableMap.initJob(TABLE_NAME, INPUT_COLUMN,
          IdentityTableMap.class, jobConf);

      // use IndexTableReduce to build a Lucene index
      jobConf.setReducerClass(IndexTableReduce.class);
      FileOutputFormat.setOutputPath(jobConf, new Path(INDEX_DIR));
      jobConf.setOutputFormat(IndexOutputFormat.class);

      JobClient.runJob(jobConf);

    } finally {
      mrCluster.shutdown();
    }

    if (printResults) {
      LOG.info("Print table contents after map/reduce");
    }
    scanTable(printResults);

    // verify index results
    verify();
  }

  private String createIndexConfContent() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("<configuration><column><property>" +
      "<name>hbase.column.name</name><value>" + INPUT_COLUMN +
      "</value></property>");
    buffer.append("<property><name>hbase.column.store</name> " +
      "<value>true</value></property>");
    buffer.append("<property><name>hbase.column.index</name>" +
      "<value>true</value></property>");
    buffer.append("<property><name>hbase.column.tokenize</name>" +
      "<value>false</value></property>");
    buffer.append("<property><name>hbase.column.boost</name>" +
      "<value>3</value></property>");
    buffer.append("<property><name>hbase.column.omit.norms</name>" +
      "<value>false</value></property></column>");
    buffer.append("<property><name>hbase.index.rowkey.name</name><value>" +
      ROWKEY_NAME + "</value></property>");
    buffer.append("<property><name>hbase.index.max.buffered.docs</name>" +
      "<value>500</value></property>");
    buffer.append("<property><name>hbase.index.max.field.length</name>" +
      "<value>10000</value></property>");
    buffer.append("<property><name>hbase.index.merge.factor</name>" +
      "<value>10</value></property>");
    buffer.append("<property><name>hbase.index.use.compound.file</name>" +
      "<value>true</value></property>");
    buffer.append("<property><name>hbase.index.optimize</name>" +
      "<value>true</value></property></configuration>");

    IndexConfiguration c = new IndexConfiguration();
    c.addFromXML(buffer.toString());
    return c.toString();
  }

  private void scanTable(boolean printResults)
  throws IOException {
    HTable table = new HTable(conf, TABLE_NAME);
    Scan scan = new Scan();
    scan.addColumns(columns);
    ResultScanner scanner = table.getScanner(scan);
    try {
      for (Result r : scanner) {
        if (printResults) {
          LOG.info("row: " + r.getRow());
        }
        for (Map.Entry<byte [], Cell> e : r.getRowResult().entrySet()) {
          if (printResults) {
            LOG.info(" column: " + e.getKey() + " value: "
                + new String(e.getValue().getValue(), HConstants.UTF8_ENCODING));
          }
        }
      }
    } finally {
      scanner.close();
    }
  }

  private void verify() throws IOException {
    // Force a cache flush for every online region to ensure that when the
    // scanner takes its snapshot, all the updates have made it into the cache.
    for (HRegion r : cluster.getRegionThreads().get(0).getRegionServer().
        getOnlineRegions()) {
      HRegionIncommon region = new HRegionIncommon(r);
      region.flushcache();
    }

    Path localDir = new Path(getUnitTestdir(getName()), "index_" +
      Integer.toString(new Random().nextInt()));
    this.fs.copyToLocalFile(new Path(INDEX_DIR), localDir);
    FileSystem localfs = FileSystem.getLocal(conf);
    FileStatus [] indexDirs = localfs.listStatus(localDir);
    Searcher searcher = null;
    ResultScanner scanner = null;
    try {
      if (indexDirs.length == 1) {
        searcher = new IndexSearcher((new File(indexDirs[0].getPath().
          toUri())).getAbsolutePath());
      } else if (indexDirs.length > 1) {
        Searchable[] searchers = new Searchable[indexDirs.length];
        for (int i = 0; i < indexDirs.length; i++) {
          searchers[i] = new IndexSearcher((new File(indexDirs[i].getPath().
            toUri()).getAbsolutePath()));
        }
        searcher = new MultiSearcher(searchers);
      } else {
        throw new IOException("no index directory found");
      }

      HTable table = new HTable(conf, TABLE_NAME);
      Scan scan = new Scan();
      scan.addColumns(columns);
      scanner = table.getScanner(scan);

      IndexConfiguration indexConf = new IndexConfiguration();
      String content = conf.get("hbase.index.conf");
      if (content != null) {
        indexConf.addFromXML(content);
      }
      String rowkeyName = indexConf.getRowkeyName();

      int count = 0;
      for (Result r : scanner) {
        String value = Bytes.toString(r.getRow());
        Term term = new Term(rowkeyName, value);
        int hitCount = searcher.search(new TermQuery(term)).length();
        assertEquals("check row " + value, 1, hitCount);
        count++;
      }
      LOG.debug("Searcher.maxDoc: " + searcher.maxDoc());
      LOG.debug("IndexReader.numDocs: " + ((IndexSearcher)searcher).getIndexReader().numDocs());      
      int maxDoc = ((IndexSearcher)searcher).getIndexReader().numDocs();
      assertEquals("check number of rows", maxDoc, count);
    } finally {
      if (null != searcher)
        searcher.close();
      if (null != scanner)
        scanner.close();
    }
  }
  /**
   * @param args unused
   */
  public static void main(String[] args) {
    TestRunner.run(new TestSuite(TestTableIndex.class));
  }
}