// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib;

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
    suite.addTest(com.lab616.ib.api.AllTests.suite());
    suite.addTest(com.lab616.ib.api.builders.AllTests.suite());
    suite.addTest(com.lab616.ib.api.util.AllTests.suite());
    
    return suite;
  }
}
