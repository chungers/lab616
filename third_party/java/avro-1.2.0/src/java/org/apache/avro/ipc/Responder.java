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
import java.nio.ByteBuffer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.avro.AvroRuntimeException;
import org.apache.avro.Protocol;
import org.apache.avro.Schema;
import org.apache.avro.Protocol.Message;
import org.apache.avro.util.Utf8;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.Encoder;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;

/** Base class for the server side of a protocol interaction. */
public abstract class Responder {
  private static final Logger LOG = LoggerFactory.getLogger(Responder.class);

  private static final Schema META =
    Schema.createMap(Schema.create(Schema.Type.BYTES));
  private static final GenericDatumReader<Map<Utf8,ByteBuffer>> META_READER =
    new GenericDatumReader<Map<Utf8,ByteBuffer>>(META);
  private static final GenericDatumWriter<Map<Utf8,ByteBuffer>> META_WRITER =
    new GenericDatumWriter<Map<Utf8,ByteBuffer>>(META);

  private Map<MD5,Protocol> protocols
    = Collections.synchronizedMap(new HashMap<MD5,Protocol>());

  private Protocol local;
  private MD5 localHash;
  protected List<RPCPlugin> rpcMetaPlugins;

  protected Responder(Protocol local) {
    this.local = local;
    this.localHash = new MD5();
    localHash.bytes(local.getMD5());
    protocols.put(localHash, local);
    this.rpcMetaPlugins =
      Collections.synchronizedList(new ArrayList<RPCPlugin>());
  }

  public Protocol getLocal() { return local; }
  
  /**
   * Adds a new plugin to manipulate per-call metadata.  Plugins
   * are executed in the order that they are added.
   * @param plugin a plugin that will manipulate RPC metadata
   */
  public void addRPCPlugin(RPCPlugin plugin) {
    rpcMetaPlugins.add(plugin);
  }

  /** Called by a server to deserialize a request, compute and serialize
   * a response or error. */
  public List<ByteBuffer> respond(List<ByteBuffer> buffers) throws IOException {
    Decoder in = new BinaryDecoder(new ByteBufferInputStream(buffers));
    ByteBufferOutputStream bbo = new ByteBufferOutputStream();
    Encoder out = new BinaryEncoder(bbo);
    AvroRemoteException error = null;
    RPCContext context = new RPCContext();
    try {
      Protocol remote = handshake(in, out);
      if (remote == null)                        // handshake failed
        return bbo.getBufferList();

      // read request using remote protocol specification
      context.setRequestCallMeta(META_READER.read(null, in));
      String messageName = in.readString(null).toString();
      Message m = remote.getMessages().get(messageName);
      if (m == null)
        throw new AvroRuntimeException("No such remote message: "+messageName);
      
      Object request = readRequest(m.getRequest(), in);
      
      for (RPCPlugin plugin : rpcMetaPlugins) {
        plugin.serverReceiveRequest(context);
      }

      // create response using local protocol specification
      m = getLocal().getMessages().get(messageName);
      if (m == null)
        throw new AvroRuntimeException("No such local message: "+messageName);
      Object response = null;
      try {
        response = respond(m, request);
        context.setResponse(response);
      } catch (AvroRemoteException e) {
        error = e;
        context.setError(error);
      } catch (Exception e) {
        LOG.warn("application error", e);
        error = new AvroRemoteException(new Utf8(e.toString()));
        context.setError(error);
      }
      
      for (RPCPlugin plugin : rpcMetaPlugins) {
        plugin.serverSendResponse(context);
      }
      
      META_WRITER.write(context.responseCallMeta(), out);
      out.writeBoolean(error != null);
      if (error == null)
        writeResponse(m.getResponse(), response, out);
      else
        writeError(m.getErrors(), error, out);

    } catch (AvroRuntimeException e) {            // system error
      LOG.warn("system error", e);
      error = new AvroRemoteException(e);
      context.setError(error);
      bbo = new ByteBufferOutputStream();
      out = new BinaryEncoder(bbo);
      META_WRITER.write(context.responseCallMeta(), out);
      out.writeBoolean(true);
      writeError(Protocol.SYSTEM_ERRORS, error, out);
    }
      
    return bbo.getBufferList();
  }

  private SpecificDatumWriter handshakeWriter =
    new SpecificDatumWriter(HandshakeResponse.class);
  private SpecificDatumReader handshakeReader =
    new SpecificDatumReader(HandshakeRequest.class);

  @SuppressWarnings("unchecked")
  private Protocol handshake(Decoder in, Encoder out)
    throws IOException {
    HandshakeRequest request = (HandshakeRequest)handshakeReader.read(null, in);
    Protocol remote = protocols.get(request.clientHash);
    if (remote == null && request.clientProtocol != null) {
      remote = Protocol.parse(request.clientProtocol.toString());
      protocols.put(request.clientHash, remote);
    }
    HandshakeResponse response = new HandshakeResponse();
    if (localHash.equals(request.serverHash)) {
      response.match =
        remote == null ? HandshakeMatch.NONE : HandshakeMatch.BOTH;
    } else {
      response.match =
        remote == null ? HandshakeMatch.NONE : HandshakeMatch.CLIENT;
    }
    if (response.match != HandshakeMatch.BOTH) {
      response.serverProtocol = new Utf8(local.toString());
      response.serverHash = localHash;
    }
    
    RPCContext context = new RPCContext();
    context.setRequestHandshakeMeta((Map<Utf8, ByteBuffer>) request.meta);
    
    for (RPCPlugin plugin : rpcMetaPlugins) {
      plugin.serverConnecting(context);
    }
    response.meta = context.responseHandshakeMeta();
    
    handshakeWriter.write(response, out);
    return remote;
  }

  /** Computes the response for a message. */
  public abstract Object respond(Message message, Object request)
    throws AvroRemoteException;

  /** Reads a request message. */
  public abstract Object readRequest(Schema schema, Decoder in)
    throws IOException;

  /** Writes a response message. */
  public abstract void writeResponse(Schema schema, Object response,
                                     Encoder out) throws IOException;

  /** Writes an error message. */
  public abstract void writeError(Schema schema, AvroRemoteException error,
                                  Encoder out) throws IOException;

}

