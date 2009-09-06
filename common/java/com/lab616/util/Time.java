// 2009 lab616.com, All Rights Reserved.
package com.lab616.util;

import org.apache.log4j.Logger;
import org.apache.tomcat.jni.Library;


/**
 * Utility / wrapper for time/ timing related methods. 
 * 
 * @author david
 */
public class Time {

  static Logger logger = Logger.getLogger(Time.class);
  
  /**
   * Implementation class. Either native or in java.
   */
  private static interface Impl {
    long now();
  }
  
  private static class JavaImpl implements Impl {
    public long now() {
      return System.currentTimeMillis() * 1000L;
    }
  }
  
  private static class NativeImpl implements Impl {
    public long now() {
      return org.apache.tomcat.jni.Time.now();
    }
  }
  
  final private static Impl impl;
  
  static {
    boolean nativeImpl = false;
    try {
      Library.initialize(null);
      logger.info("Native library loaded. Time in useconds.");
      nativeImpl = true;
    } catch (Throwable e) {
      logger.warn("Exception with native library. Time resolution to milliseconds.", e);
      nativeImpl = false;
    }
    impl = (nativeImpl) ? new NativeImpl() : new JavaImpl(); 
  }
  
  /**
   * Current time in microseconds.
   * @return Micros.
   */
  public static long now() {
    return impl.now();
  }
}
