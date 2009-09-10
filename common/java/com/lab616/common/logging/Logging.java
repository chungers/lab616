// 2009 lab616.com, All Rights Reserved.

package com.lab616.common.logging;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.lab616.common.flags.Flag;
import com.lab616.common.flags.Flags;

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
  
  @Flag(name="log", 
        doc="Logging level", 
        required = false)
  public static String LEVEL = "INFO";
  
  static {
    Flags.register(Logging.class);
  }
  
  public static void init() {
    // Initialize logging if log4j.properties file is not set up.
    if (System.getProperty("log4j.configuration") == null ||
        Thread.currentThread().getContextClassLoader()
          .getResource("log4j.properties") == null) {
      Level level = Level.toLevel(LEVEL);
      PatternLayout layout = (level == Level.DEBUG || level == Level.TRACE) ?
          new PatternLayout(DEBUG_PATTERN) : new PatternLayout(PATTERN);
      ConsoleAppender appender = new ConsoleAppender(layout);
      Logger.getRootLogger().addAppender(appender);
      Logger.getRootLogger().setLevel(Level.toLevel(LEVEL));
    } 
  }
  
  // For testing only.
  public static void main(String[] argv) throws Exception {
    Flags.parse(argv);
    Logging.init();
   
    Logger logger = Logger.getLogger(Logging.class);
    
    logger.info("This is info.");
    logger.debug("This is debug.");
    logger.trace("This is trace.");
    logger.warn("This is warning.");
  }
}
