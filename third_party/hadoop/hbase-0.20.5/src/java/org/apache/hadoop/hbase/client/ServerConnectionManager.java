/**
 * Copyright 2008 The Apache Software Foundation
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

import org.apache.hadoop.hbase.HBaseConfiguration;

/**
 * Used by server processes to expose HServerConnection method
 * do can call HConnectionManager#setRootRegionLocation
 */
public class ServerConnectionManager extends HConnectionManager {
  /*
   * Not instantiable
   */
  private ServerConnectionManager() {}

  /**
   * Get the connection object for the instance specified by the configuration
   * If no current connection exists, create a new connection for that instance
   * @param conf
   * @return HConnection object for the instance specified by the configuration
   */
  public static ServerConnection getConnection(HBaseConfiguration conf) {
    return (ServerConnection) HConnectionManager.getConnection(conf);
  }
}
