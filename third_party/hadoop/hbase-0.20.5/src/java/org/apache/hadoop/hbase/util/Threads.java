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
package org.apache.hadoop.hbase.util;

import java.lang.Thread.UncaughtExceptionHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Thread Utility
 */
public class Threads {
  protected static final Log LOG = LogFactory.getLog(Threads.class);
  
  /**
   * Utility method that sets name, daemon status and starts passed thread.
   * @param t
   * @param name
   * @return Returns the passed Thread <code>t</code>.
   */
  public static Thread setDaemonThreadRunning(final Thread t,
    final String name) {
    return setDaemonThreadRunning(t, name, null);
  }
    
  /**
   * Utility method that sets name, daemon status and starts passed thread.
   * @param t
   * @param name
   * @param handler A handler to set on the thread.  Pass null if want to
   * use default handler.
   * @return Returns the passed Thread <code>t</code>.
   */
  public static Thread setDaemonThreadRunning(final Thread t,
    final String name, final UncaughtExceptionHandler handler) {
    t.setName(name);
    if (handler != null) {
      t.setUncaughtExceptionHandler(handler);
    }
    t.setDaemon(true);
    t.start();
    return t;
  }

  /**
   * Shutdown passed thread using isAlive and join.
   * @param t Thread to shutdown
   */
  public static void shutdown(final Thread t) {
    shutdown(t, 0);
  }

  /**
   * Shutdown passed thread using isAlive and join.
   * @param joinwait Pass 0 if we're to wait forever.
   * @param t Thread to shutdown
   */
  public static void shutdown(final Thread t, final long joinwait) {
    while (t.isAlive()) {
      try {
        t.join(joinwait);
      } catch (InterruptedException e) {
        LOG.warn(t.getName() + "; joinwait=" + joinwait, e);
      }
    }
  }
}