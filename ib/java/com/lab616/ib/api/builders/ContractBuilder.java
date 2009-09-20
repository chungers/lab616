// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api.builders;

import com.ib.client.Contract;
import com.lab616.common.builder.AbstractBuilder;

/**
 * @author david
 *
 */
public class ContractBuilder extends AbstractBuilder<Contract> {

  public ContractBuilder() {
    super(Contract.class);
    
    // Defaults
    setProperty("m_secType", "STK");
    setProperty("m_currency", "USD");
    setProperty("m_exchange", "SMART");

  }
}
