/**
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
package org.apache.avro;

import org.apache.avro.ipc.AvroRemoteException;
import org.apache.avro.ipc.SocketServer;
import org.apache.avro.ipc.SocketTransceiver;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.specific.SpecificRequestor;
import org.apache.avro.specific.SpecificResponder;
import org.apache.avro.test.namespace.TestNamespace;
import org.apache.avro.test.util.MD5;
import org.apache.avro.test.errors.TestError;
import org.apache.avro.test.namespace.TestRecord;
import org.apache.avro.util.Utf8;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Random;


public class TestNamespaceSpecific {
  private static final Logger LOG
    = LoggerFactory.getLogger(TestNamespaceSpecific.class);

  public static class TestImpl implements TestNamespace {
    public TestRecord echo(TestRecord record) { return record; }
    public Void error() throws AvroRemoteException {
      TestError error = new TestError();
      error.message = new Utf8("an error");
      throw error;
    }
  }

  protected static SocketServer server;
  protected static Transceiver client;
  protected static TestNamespace proxy;

  @Before
  public void testStartServer() throws Exception {
    server = new SocketServer(new SpecificResponder(TestNamespace.class, new TestImpl()),
                              new InetSocketAddress(0));
    client = new SocketTransceiver(new InetSocketAddress(server.getPort()));
    proxy = (TestNamespace)SpecificRequestor.getClient(TestNamespace.class, client);
  }

  @Test
  public void testEcho() throws IOException {
    TestRecord record = new TestRecord();
    record.hash = new MD5();
    System.arraycopy(new byte[]{0,1,2,3,4,5,6,7,8,9,0,1,2,3,4,5}, 0,
                     record.hash.bytes(), 0, 16);
    TestRecord echoed = proxy.echo(record);
    assertEquals(record, echoed);
    assertEquals(record.hashCode(), echoed.hashCode());
  }

  @Test
  public void testError() throws IOException {
    TestError error = null;
    try {
      proxy.error();
    } catch (TestError e) {
      error = e;
    }
    assertNotNull(error);
    assertEquals("an error", error.message.toString());
  }

  @After
  public void testStopServer() throws IOException {
    client.close();
    server.close();
  }
}
