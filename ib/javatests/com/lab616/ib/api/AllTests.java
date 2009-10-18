// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api;

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
    suite.addTestSuite(ApiBuilderTest.class);
    suite.addTestSuite(ApiTest.class);    
    return suite;
  }
}
