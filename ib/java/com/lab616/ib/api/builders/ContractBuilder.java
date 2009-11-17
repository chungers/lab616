// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api.builders;

import java.util.Currency;

import com.ib.client.Contract;
import com.lab616.common.builder.AbstractBuilder;
import com.lab616.common.builder.Builder;
import com.lab616.ib.api.TickerId;

/**
 * Builder for IB's Contract object.
 * 
 * @author david
 *
 */
public class ContractBuilder extends AbstractBuilder<Contract> {

  public ContractBuilder(String symbol) {
    super(Contract.class);
    setSymbol().to(symbol.toUpperCase());
    // Defaults
    set("m_secType", "STK");
    set("m_exchange", "SMART");
    setCurrency(Currency.getInstance("USD"));
  }
  
  public final Property<Contract> setSymbol() {
    return setProperty("m_symbol");
  }

  public final Builder<Contract> setCurrency(Currency cur) {
    return setProperty("m_currency").to(cur.getCurrencyCode());
  }

  public int getTickerId() {
    return TickerId.toTickerId(getProperty("m_symbol").toString());
  }

  @Override
  public Contract build() {
    // This may not be necessary...
    set("m_conId", getTickerId());
    return super.build();
  }
}
