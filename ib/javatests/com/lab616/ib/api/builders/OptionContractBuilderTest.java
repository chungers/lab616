// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api.builders;

import org.joda.time.DateTime;

import com.ib.client.Contract;

import junit.framework.TestCase;

/**
 * @author david
 *
 */
public class OptionContractBuilderTest extends TestCase {

  public void testBuildContract() throws Exception {
    OptionContractBuilder b = new OptionContractBuilder("AAPL");
    Contract c = b.setExpiration(1).setStrike(240.0).forCall().build();
    
    assertEquals("AAPL", c.m_symbol);
    assertEquals("CALL", c.m_right);
    
    DateTime now = new DateTime();
    DateTime expire = now.plusMonths(1);
    assertEquals("" + expire.getYear() + expire.getMonthOfYear(), c.m_expiry);
    
    System.out.println("exp = " + c.m_expiry);
  }
}
