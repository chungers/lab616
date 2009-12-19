// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api.builders;

import java.util.List;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.inject.internal.Lists;
import com.ib.client.Contract;

/**
 * Class that encapsulates the information required by the IP API's 
 * EClientSocket.reqMktData() method for getting market data.
 *
 * @author david
 *
 */
public class MarketDataRequest {

  DateTimeFormatter dateTimeFormatter = 
    DateTimeFormat.forPattern("yyyyMMdd HH:mm:ss");
  
  private Contract contract;
  private int tickerId;
  private List<String> genericTickList = Lists.newArrayList();
  private boolean isSnapShot = false;
  private int barSize = 5; // For realtime bars.
  private String barType = "TRADES";  // TODO: change to enum later.
  private boolean regularTradingHours = false;
  
  public boolean getSnapShot() {
    return isSnapShot;
  }
  public void setSnapShot(boolean b) {
    this.isSnapShot = b;
  }
  public Contract getContract() {
    return contract;
  }
  public void setContract(Contract contract) {
    this.contract = contract;
  }
  public int getTickerId() {
    return tickerId;
  }
  public void setTickerId(int tickerId) {
    this.tickerId = tickerId;
  }
  public int getBarSize() {
    return barSize;
  }
  public void setBarSize(int barSize) {
    this.barSize = barSize;
  }
  public String getBarType() {
    return this.barType;
  }
  public void setBarType(String b) {
    this.barType = b;
  }
  public boolean getRegularTradingHours() {
    return regularTradingHours;
  }
  public void setRegularTradingHours(boolean b) {
    this.regularTradingHours = b;
  }
  
  public String getGenericTickList() {
    StringBuffer b = new StringBuffer(genericTickList.get(0));
    for (String s : genericTickList.subList(1, genericTickList.size())) {
      b.append(",");
      b.append(s);
    }
    return b.toString();
  }
  
  public void setGenericTickList(List<String> tickList) {
    this.genericTickList = tickList;
  }
}
