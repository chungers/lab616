// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api.builders;

import java.math.BigDecimal;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import com.ib.client.Contract;
import com.lab616.common.builder.Builder;

/**
 *
 *
 * @author david
 *
 */
public class OptionContractBuilder extends ContractBuilder {

  
  public OptionContractBuilder(String symbol) {
    super(symbol);
    set("m_secType", "OPT");
  }

  public final Builder<Contract> forCall() {
    return setProperty("m_right").to("CALL");
  }
  
  public final Builder<Contract> forPut() {
    return setProperty("m_right").to("PUT");
  }

  public final OptionContractBuilder setStrike(double strike) {
    setProperty("m_strike").to(strike);
    return this;
  }
  
  public final OptionContractBuilder setStrike(BigDecimal strike) {
    setProperty("m_strike").to(strike.doubleValue());
    return this;
  }

  public final OptionContractBuilder setExpiration(int monthsFromToday) {
    DateTime exp = new DateTime().plusMonths(monthsFromToday);
    setExpiration(exp);
    return this;
  }
  
  public final Builder<Contract> setExpiration(DateTime expiration) {
    setProperty("m_expiry").to(
        DateTimeFormat.forPattern("YYYYMM").print(expiration));
    return this;
  }
  
}
