// 2009 lab616.com, All Rights Reserved.

package com.lab616.monitoring;

import java.util.Random;

import junit.framework.TestCase;

/**
 *
 *
 * @author david
 *
 */
public class MinMaxAverageTest extends TestCase {

  public void testValue() throws Exception {
    MinMaxAverage mma = new MinMaxAverage();
    Random rand = new Random(System.currentTimeMillis());
    for (int i = 0; i < 17; i++) {
      mma.set(rand.nextInt(19));
    }
    
    System.out.println("mma = " + mma);
  }
}
