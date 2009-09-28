// 2009 lab616.com, All Rights Reserved.

package com.lab616.common.logging;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * Initializes log4j instead of properties file.
 *
 * @author david
 *
 */
public class Logging {

  /**
   * Default layout pattern when the log level is not DEBUG.
   */
  public static String PATTERN = "%d [%t] %-5p %c - %m%n";
  
  /**
   * Debug layout pattern which includes method and line number in the log.
   */
  public static String DEBUG_PATTERN = "%d [%t] %-5p %c.%M:%L - %m%n";

  /**
   * Initializes the logging system with the desired log level.
   * 
   * @param level The log level in string.
   */
  public final static void init(String level) {
    init(Level.toLevel(level));
  }
  
  /**
   * Initializes the logging system with the desired log level.
   * 
   * @param level The log level.
   */
  public final static void init(Level level) {
    // Initialize logging if log4j.properties file is not set up.
    if (System.getProperty("log4j.configuration") == null ||
        Thread.currentThread().getContextClassLoader()
          .getResource("log4j.properties") == null) {
      PatternLayout layout = (level == Level.DEBUG || level == Level.TRACE) ?
          new PatternLayout(DEBUG_PATTERN) : new PatternLayout(PATTERN);
      ConsoleAppender appender = new ConsoleAppender(layout);
      Logger.getRootLogger().addAppender(appender);
      Logger.getRootLogger().setLevel(level);
    } 
  }
}
