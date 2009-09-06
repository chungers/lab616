// 2009 lab616.com, All Rights Reserved.
package com.lab616.util;

import com.lab616.common.logging.Logging;

/**
 * @author david
 *
 */
public class TimeMain {
  
  public static void main(String[] args) throws Exception {
    Logging.init();
    
    for (int i=0; i<10; i++) {
      System.out.println("ct = " + Time.now());
    }
  }
}
