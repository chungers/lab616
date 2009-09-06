/**
 * 
 */
package com.lab616.util;

import org.apache.log4j.Logger;

import com.lab616.common.logging.Logging;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author david
 *
 */
public class TimeTest extends TestCase {

  Logger logger = Logger.getLogger(TimeTest.class);
  
  static {
    Logging.init();
  }
  
  public void testCurrentTimeMicros() throws Exception {
    int tests = 100;
    long t = 0, t1 = 0;
    
    t = Time.now();
    for (int i = 0; i < tests; i++) {
      Thread.currentThread().sleep(1);
      t1 = Time.now();
      assertTrue(String.format("%d(%d > %d)", i, t1, t),
          t1 > t);
      t = t1;
    }
  }
}
