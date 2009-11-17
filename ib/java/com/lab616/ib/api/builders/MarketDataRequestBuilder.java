// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api.builders;

import java.util.List;

import com.google.inject.internal.Lists;
import com.lab616.common.builder.AbstractBuilder;

/**
 *
 *
 * @author david
 *
 */
public class MarketDataRequestBuilder 
  extends AbstractBuilder<MarketDataRequest> {

  
  private ContractBuilder contractBuilder = null;
  private List<String> genericTickList = Lists.newArrayList();
  
  public MarketDataRequestBuilder() {
    super(MarketDataRequest.class);
    setIsSnapShot(false);
  }
  
  public Property<MarketDataRequest> setContract() {
    return setProperty("contract");
  }
  
  public MarketDataRequestBuilder setBarSize(Integer size) {
    setProperty("barSize", int.class).to(size);
    return this;
  }
  
  public MarketDataRequestBuilder setIsSnapShot(Boolean b) {
    setProperty("snapShot", boolean.class).to(b);
    return this;
  }
  
  public MarketDataRequestBuilder setIsRegularTradingHours(Boolean b) {
    setProperty("regularTradingHours", boolean.class).to(b);
    return this;
  }
  
  public MarketDataRequestBuilder setBarType(String bt) {
    setProperty("barType", String.class).to(bt);
    return this;
  }
  
  public MarketDataRequestBuilder forIndex(IndexBuilder builder, 
      boolean... useDefaults) {
    contractBuilder = builder;
    return this;
  }
  
  public MarketDataRequestBuilder forStock(ContractBuilder builder, 
      boolean... useDefaults) {
    contractBuilder = builder;
    if (useDefaults.length > 0 && useDefaults[0]) {
      withDefaultsForStocks();
    }
    return this;
  }
  
  public MarketDataRequestBuilder forOption(OptionContractBuilder builder, 
      boolean... useDefaults) {
    contractBuilder = builder;
    if (useDefaults.length > 0 && useDefaults[0]) {
      withDefaultsForOptions();
    }
    return this;
  }

  public MarketDataRequest build() {
    MarketDataRequest m = super.build();
    if (m.getContract() == null && contractBuilder != null) {
      m.setContract(contractBuilder.build());
    }
    m.setTickerId(contractBuilder.getTickerId());
    m.setGenericTickList(this.genericTickList);
    return m;
  }

  public MarketDataRequestBuilder withDefaultsForIndex() {
    getMarketPrice();
    getRealTimeVolume();
    return this;
  }

  public MarketDataRequestBuilder withDefaultsForStocks() {
    getMarketPrice();
    getVolumePriceAndImbalance();
    getRealTimeVolume();
    return this;
  }

  public MarketDataRequestBuilder withDefaultsForOptions() {
    getOptionVolume();
    getOptionOpenInterest();
    getHistoricalVolatility();
    return this;
  }

  private void addGenericTickList(String s) {
    if (!this.genericTickList.contains(s)) {
      this.genericTickList.add(s);
    }
  }
  
  public MarketDataRequestBuilder getOptionVolume() {
    addGenericTickList("100"); // 29,30
    return this;
  }
  
  public MarketDataRequestBuilder getOptionOpenInterest() {
    addGenericTickList("101"); // 27,28
    return this;
  }

  public MarketDataRequestBuilder getHistoricalVolatility() {
    addGenericTickList("104"); // 23
    return this;
  }

  public MarketDataRequestBuilder getOptionImpliedVolatility() {
    addGenericTickList("106"); // 24
    return this;
  }

  public MarketDataRequestBuilder getIndexFuturePremium() {
    addGenericTickList("162"); // 31
    return this;
  }

  public MarketDataRequestBuilder getMiscellaneousStats() {
    addGenericTickList("165"); // 15,16,17,18,19,20,21
    return this;
  }

  public MarketDataRequestBuilder getMarketPrice() {
    addGenericTickList("221"); // 37
    return this;
  }

  public MarketDataRequestBuilder getVolumePriceAndImbalance() {
    addGenericTickList("225"); // 34,35,36
    return this;
  }

  public MarketDataRequestBuilder getRealTimeVolume() {
    addGenericTickList("233"); // 48
    return this;
  }

  public MarketDataRequestBuilder getShortable() {
    addGenericTickList("236"); // 46
    return this;
  }

  public MarketDataRequestBuilder getInventory() {
    addGenericTickList("256"); 
    return this;
  }

  public MarketDataRequestBuilder getFundamentalRatios() {
    addGenericTickList("258"); // 47
    return this;
  }
}
