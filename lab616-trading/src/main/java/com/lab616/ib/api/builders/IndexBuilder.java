// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api.builders;


/**
 *
 *
 * @author david
 *
 */
public class IndexBuilder extends ContractBuilder {

  public enum Exchange {
    SMART,
    NYSE,  // For INDU (Dow)
    CBOE; // For VIX, SPX
  }
  
  public IndexBuilder(String symbol) {
    super(symbol);
    // Defaults
    set("m_secType", "IND");
  }
  
  public final IndexBuilder setExchange(Exchange ex) {
    setProperty("m_exchange").to(ex.name());
    return this;
  }
}