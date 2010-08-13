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
package org.apache.hadoop.hbase.client.transactional;

import org.apache.hadoop.hbase.ipc.HBaseRPC;
import org.apache.hadoop.hbase.ipc.TransactionalRegionInterface;

/** Simple class for registering the transactional RPC codes. 
 *  
 */
public final class TransactionalRPC {
  
  private static final byte RPC_CODE = 100;

  private static boolean initialized = false;
  
  public synchronized static void initialize() {
    if (initialized) {
      return;
    }
    HBaseRPC.addToMap(TransactionalRegionInterface.class, RPC_CODE);
    initialized = true;
  }
  
  private TransactionalRPC() {
    // Static helper class;
  }

}
