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
package org.apache.hadoop.hbase.util;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Sleeper for current thread.
 * Sleeps for passed period.  Also checks passed boolean and if interrupted,
 * will return if the flag is set (rather than go back to sleep until its 
 * sleep time is up).
 */
public class Sleeper {
  private final Log LOG = LogFactory.getLog(this.getClass().getName());
  private final int period;
  private AtomicBoolean stop;
  private final Object sleepLock = new Object();
  private boolean triggerWake = false;
  
  /**
   * @param sleep
   * @param stop
   */
  public Sleeper(final int sleep, final AtomicBoolean stop) {
    this.period = sleep;
    this.stop = stop;
  }
  
  /**
   * Sleep for period.
   */
  public void sleep() {
    sleep(System.currentTimeMillis());
  }

  /**
   * If currently asleep, stops sleeping; if not asleep, will skip the next
   * sleep cycle.
   */
  public void skipSleepCycle() {
    synchronized (sleepLock) {
      triggerWake = true;
      sleepLock.notify();
    }
  }
  
  /**
   * Sleep for period adjusted by passed <code>startTime<code>
   * @param startTime Time some task started previous to now.  Time to sleep
   * will be docked current time minus passed <code>startTime<code>.
   */
  public void sleep(final long startTime) {
    if (this.stop.get()) {
      return;
    }
    long now = System.currentTimeMillis();
    long waitTime = this.period - (now - startTime);
    if (waitTime > this.period) {
      LOG.warn("Calculated wait time > " + this.period +
        "; setting to this.period: " + System.currentTimeMillis() + ", " +
        startTime);
      waitTime = this.period;
    }
    while (waitTime > 0) {
      long woke = -1;
      try {
        synchronized (sleepLock) {
          if (triggerWake) break;
          sleepLock.wait(waitTime);
        }
        woke = System.currentTimeMillis();
        long slept = woke - now;
        if (slept > (10 * this.period)) {
          LOG.warn("We slept " + slept + "ms, ten times longer than scheduled: " +
            this.period);
        }
      } catch(InterruptedException iex) {
        // We we interrupted because we're meant to stop?  If not, just
        // continue ignoring the interruption
        if (this.stop.get()) {
          return;
        }
      }
      // Recalculate waitTime.
      woke = (woke == -1)? System.currentTimeMillis(): woke;
      waitTime = this.period - (woke - startTime);
    }
    triggerWake = false;
  }
}
