// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api.builders;

import java.util.Currency;

import com.ib.client.Contract;
import com.lab616.common.builder.AbstractBuilder;
import com.lab616.common.builder.Builder;
import com.lab616.ib.api.TickerId;

/**
 *
 *
 * @author david
 *
 */
public class IndexBuilder extends AbstractBuilder<Contract> {

  public enum Exchange {
    SMART,
    NYSE,
    CBOE;
  }
  
  public IndexBuilder(String symbol) {
    super(Contract.class);
    setSymbol().to(symbol.toUpperCase());
    // Defaults
    set("m_secType", "IND");
    set("m_exchange", "SMART");
    setCurrency(Currency.getInstance("USD"));
    
    // This may not be necessary...
    set("m_conId", getTickerId());
  }
  
  public final Property<Contract> setSymbol() {
    return setProperty("m_symbol");
  }

  public final Builder<Contract> setCurrency(Currency cur) {
    return setProperty("m_currency").to(cur.getCurrencyCode());
  }

  public final Builder<Contract> setExchange(Exchange ex) {
    return setProperty("m_exchange").to(ex.name());
  }
  
  public final int getTickerId() {
    return TickerId.toTickerId(getProperty("m_symbol").toString());
  }
  
}