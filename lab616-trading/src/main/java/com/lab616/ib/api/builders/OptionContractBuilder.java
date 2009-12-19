// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api.builders;

import java.math.BigDecimal;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import com.ib.client.Contract;
import com.lab616.common.builder.Builder;
import com.lab616.ib.api.OptionSymbol;
import com.lab616.ib.api.TickerId;

/**
 *
 *
 * @author david
 *
 */
public class OptionContractBuilder extends ContractBuilder {

  private String base;
  private DateTime expiration;
  private double strike;
  private boolean call = true;
  
  public OptionContractBuilder(String symbol) {
    super(symbol);
    base = symbol;
    set("m_secType", "OPT");
  }

  public final Builder<Contract> forCall() {
    return setProperty("m_right").to("CALL");
  }
  
  public final Builder<Contract> forPut() {
    call = false;
    return setProperty("m_right").to("PUT");
  }

  public final OptionContractBuilder setStrike(double strike) {
    this.strike = strike;
    setProperty("m_strike").to(strike);
    return this;
  }
  
  public final OptionContractBuilder setStrike(BigDecimal strike) {
    return setStrike(strike.doubleValue());
  }

  public final OptionContractBuilder setExpiration(int monthsFromToday) {
    DateTime exp = new DateTime().plusMonths(monthsFromToday);
    setExpiration(exp);
    return this;
  }
  
  public final Builder<Contract> setExpiration(DateTime expiration) {
    this.expiration = expiration;
    setProperty("m_expiry").to(
        DateTimeFormat.forPattern("YYYYMM").print(expiration));
    return this;
  }

  public String getOptionSymbol() {
    if (call) {
      return OptionSymbol.getCallOptionSymbol(base, expiration, strike);
    } else {
      return OptionSymbol.getPutOptionSymbol(base, expiration, strike);
    }
  }
  
  @Override
  public int getTickerId() {
    return TickerId.toTickerId(getOptionSymbol());
  }
}
