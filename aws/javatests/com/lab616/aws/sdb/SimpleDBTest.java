// 2009 lab616.com, All Rights Reserved.

package com.lab616.aws.sdb;

import org.apache.log4j.Logger;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.lab616.common.flags.Flag;
import com.lab616.common.flags.Flags;
import com.lab616.common.logging.Logging;

import junit.framework.TestCase;

/**
 * Test case for SimpleDB.
 *
 * @author david
 *
 */
public class SimpleDBTest extends TestCase {

  @Flag(name = "domainName")
  public static String domainName;
  
  static {
    Flags.register(SimpleDBTest.class);
  }
  
  static Logger logger = Logger.getLogger(SimpleDBTest.class);
  
  /* (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    super.setUp();
  }

  public static void main(String[] args) throws Exception {
    Module[] modules = new Module[] {
      new SimpleDBModule()  
    };
    
    Flags.parse(args);
    Logging.init();
    
    Injector injector = Guice.createInjector(modules);
    
    SimpleDB sdb = injector.getInstance(SimpleDB.class);
    
    logger.info("************* SimpleDB started.");
    
    if (domainName != null) {
      logger.info("Creating domain " + domainName);
      sdb.createDomain(domainName);
    }
      
    // List domains.
    for (Domain d : sdb.getDomains()) {
      logger.info("Listing domain = " + d.getName());
    }
    logger.info("log4j = " + System.getProperty("log4j.configuration"));
    logger.info("log4j.properties = " + Thread.currentThread().getContextClassLoader()
      .getResource("log4j.properties"));
    
    sdb.stop();
  }
}
