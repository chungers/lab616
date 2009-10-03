// 2009 lab616.com, All Rights Reserved.

package com.lab616.util;

import java.util.concurrent.TimeUnit;

import org.apache.tomcat.jni.Pool;
import org.apache.tomcat.jni.Shm;
import org.apache.tomcat.jni.Status;

/**
 *
 *
 * @author david
 *
 */
public class ShmMain {
  
  public static void main(String[] args) throws Exception {
    if (TimeUnit.MICROSECONDS != Time.getTimeUnit()) {
      System.out.println("Time not in micros.  Apache Native Lib not loaded.");
    }
    
    long pool = Pool.create(0L);
    System.err.println("Created pool = " + pool);

    long size = 1024L * 1000 * 1000;
    long shm = Shm.create(size, null, pool);
    
    System.err.println("Created shm " + shm + " of size = " + Shm.size(shm));
    
    Pool.dataSet(pool, "test", "hello");
    System.err.println("Found " + Pool.dataGet(pool, "test"));
    
    for (int i = 0; i < 100000; i++) {
      int status = Pool.dataSet(pool, "key-" + i, "This is object " + i);
      if (status != Status.APR_SUCCESS) {
        System.err.println("Failed on i = " + i);
      }
    }
    
    int read = 0;
    for (int i = 0; i < 1000; i++) {
      try {
        Object obj = Pool.dataGet(pool, "key-" + i);
        if (obj != null) {
          read++;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    System.err.println("Read " + read + " objects");
  }
  

}
