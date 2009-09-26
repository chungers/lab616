// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api.builders;

import com.lab616.ib.api.TickerId;

import junit.framework.TestCase;


/**
 *
 *
 * @author david
 *
 */
public class MarketDataRequestBuilderTest extends TestCase {

  public void testBuilder() throws Exception {
    MarketDataRequest m = 
      new MarketDataRequestBuilder()
        .withDefaultsForStocks()
        .forStock(new ContractBuilder("GOOG")).build();
    
    assertEquals("221,225,233", m.getGenericTickList());
    assertEquals(TickerId.toTickerId("GOOG"), m.getTickerId());
    assertEquals("GOOG", TickerId.fromTickerId(m.getTickerId()));
    assertEquals("STK", m.getContract().m_secType);
    assertEquals("SMART", m.getContract().m_exchange);
    assertEquals("USD", m.getContract().m_currency);
  }
}
