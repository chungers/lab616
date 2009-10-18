package com.lab616.omnibus;
// 2009 lab616.com, All Rights Reserved.



import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 *
 * @author david
 *
 */
public class AllTests extends TestSuite {
  public static Test suite() {
    TestSuite suite = new TestSuite(AllTests.class.getPackage().getName());
    suite.addTest(com.lab616.omnibus.event.AllTests.suite());
    return suite;
  }
}
