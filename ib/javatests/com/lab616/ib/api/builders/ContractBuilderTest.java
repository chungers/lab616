// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api.builders;

import junit.framework.TestCase;

import com.ib.client.Contract;

/**
 * @author david
 *
 */
public class ContractBuilderTest extends TestCase {

  public void testBuilder() throws Exception {
    Contract contract = new ContractBuilder()
      .setProperty("m_symbol").to("GOOG")
      .setProperty("m_secType").to("STK")
      .setProperty("m_currency").to("USD")
      .setProperty("m_exchange").to("SMART").build();

    assertEquals("GOOG", contract.m_symbol);
    assertEquals("STK", contract.m_secType);
    assertEquals("USD", contract.m_currency);
    assertEquals("SMART", contract.m_exchange);
  }
}
