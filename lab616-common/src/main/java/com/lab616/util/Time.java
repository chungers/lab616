// 2009 lab616.com, All Rights Reserved.
package com.lab616.util;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.apache.tomcat.jni.Library;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.lab616.monitoring.Varz;
import com.lab616.monitoring.Varzs;


/**
 * Utility / wrapper for time/ timing related methods. 
 * 
 * @author david
 */
public class Time {

	@Varz(name = "time-resolution-microseconds")
	public static final AtomicInteger resolution = new AtomicInteger();

	static {
		Varzs.export(Time.class);
	}
	
  static Logger logger = Logger.getLogger(Time.class);

  private final static DateTimeFormatter formatter = 
    ISODateTimeFormat.dateHourMinuteSecondMillis();
  
  private Time() {
    // Do not instantiate.
  }
  /**
   * Implementation class. Either native or in java.
   */
  private static interface TimeSource {
    long now();
    TimeUnit getTimeUnit();
  }
  
  private static class JavaTimeSource implements TimeSource {
    public long now() {
      return System.currentTimeMillis() * 1000L;
    }
    public TimeUnit getTimeUnit() {
      return TimeUnit.MILLISECONDS;
    }
  }
  
  private static class NativeTimeSource implements TimeSource {
    public long now() {
      return org.apache.tomcat.jni.Time.now();
    }
    public TimeUnit getTimeUnit() {
      return TimeUnit.MICROSECONDS;
    }
  }
  
  final private static TimeSource timeSource;
  
  static {
    boolean nativeImpl = false;
    try {
      Library.initialize(null);
      logger.info(
      		"Native library loaded. Time accurate to microseconds.");
      nativeImpl = true;
      resolution.set(1);
    } catch (Throwable e) {
      logger.info(
      		"Exception loading native library. Time accurate to milliseconds: " +
      		e.getMessage());
      nativeImpl = false;
      resolution.set(1000);
    }
    timeSource = (nativeImpl) ? new NativeTimeSource() : new JavaTimeSource(); 
  }
  
  public static TimeUnit getTimeUnit() {
    return timeSource.getTimeUnit();
  }
  
  /**
   * Current time in microseconds.
   * @return Micros.
   */
  public final static long now() {
    return timeSource.now();
  }
  
  public final static  DateTimeFormatter getDefaultFormatter() {
    return formatter;
  }
  
  /**
   * Given a timestamp that may or may not be written in micros,
   * determine the proper scale and return in milliseconds.
   * 
   * @param timestamp The timestamp.
   * @return In milliseconds.
   */
  public final static long scaleToMillis(long timestamp) {
    long now = Time.now();
    TimeUnit recordUnit;
    if (Time.getTimeUnit().equals(TimeUnit.MICROSECONDS)) {
      // Check to see if we are not off by a factor of 1000
      recordUnit = (now / timestamp >= 1000) ? 
          TimeUnit.MILLISECONDS : TimeUnit.MICROSECONDS;
    } else {
      recordUnit = (timestamp / now >= 1000) ? 
          TimeUnit.MICROSECONDS : TimeUnit.MILLISECONDS;
    }
    return (recordUnit == TimeUnit.MILLISECONDS) ? timestamp :
      timestamp / 1000;
  }
  
  /**
   * Prints the timestamp in ISO format.
   * @param timestamp The timestamp.
   * @return String in ISO format.
   */
  public final static String printISO(long timestamp) {
    return formatter.print(scaleToMillis(timestamp));
  }
  
}
