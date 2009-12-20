// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api.builders;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import com.ib.client.Contract;

import junit.framework.TestCase;

/**
 * @author david
 *
 */
public class OptionContractBuilderTest extends TestCase {

	static Logger logger = Logger.getLogger(OptionContractBuilderTest.class);
	
  public void testBuildContract() throws Exception {
    OptionContractBuilder b = new OptionContractBuilder("AAPL");
    Contract c = b.setExpiration(1).setStrike(240.0).forCall().build();
    
    assertEquals("AAPL", c.m_symbol);
    assertEquals("CALL", c.m_right);
    
    DateTime now = new DateTime();
    DateTime expire = now.plusMonths(1);
    assertEquals(DateTimeFormat.forPattern("YYYYMM").print(expire), c.m_expiry);
    
    logger.info("exp = " + c.m_expiry);
  }
}
