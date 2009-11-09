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

package org.apache.avro.ipc;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A simple socket-based {@link Transceiver} implementation. */
public class SocketTransceiver extends Transceiver {
  private static final Logger LOG
    = LoggerFactory.getLogger(SocketTransceiver.class);

  private SocketChannel channel;
  private ByteBuffer header = ByteBuffer.allocate(4);
  
  public SocketTransceiver(SocketAddress address) throws IOException {
    this(SocketChannel.open(address));
  }

  public SocketTransceiver(SocketChannel channel) {
    this.channel = channel;
    LOG.info("open to "+getRemoteName());
  }

  public String getRemoteName() {
    return channel.socket().getRemoteSocketAddress().toString();
  }

  public synchronized List<ByteBuffer> readBuffers() throws IOException {
    List<ByteBuffer> buffers = new ArrayList<ByteBuffer>();
    while (true) {
      header.clear();
      while (header.hasRemaining()) {
        channel.read(header);
      }
      header.flip();
      int length = header.getInt();
      if (length == 0) {                       // end of buffers
        return buffers;
      }
      ByteBuffer buffer = ByteBuffer.allocate(length);
      while (buffer.hasRemaining()) {
        channel.read(buffer);
      }
      buffer.flip();
      buffers.add(buffer);
    }
  }

  public synchronized void writeBuffers(List<ByteBuffer> buffers)
    throws IOException {
    for (ByteBuffer buffer : buffers) {
      writeLength(buffer.limit());                // length-prefix
      channel.write(buffer);
    }
    writeLength(0);                               // null-terminate
  }

  private void writeLength(int length) throws IOException {
    header.clear();
    header.putInt(length);
    header.flip();
    channel.write(header);
  }

  public void close() throws IOException {
    if (channel.isOpen()) {
      LOG.info("closing to "+getRemoteName());
      channel.close();
    }
  }

}

