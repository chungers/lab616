package com.lab616.trading.quote

import junit.framework._;
import Assert._;

class TickDataTest extends TestCase("TickData") {

  def testMatching = {
    val bid = Bid(1, "GOOG", 400.0, "test-account")
    matchTick(bid)
  }
  
  def matchTick(tick: TickData) = {
    tick match {
      case Bid(ts, "GOOG", price, source) => 
        print("Got bid for GOOG " + price)
      case Bid(ts, symbol, price, source) => 
        print("Got bid " + price)
      case Ask(ts, symbol, price, source) => 
        print("Got ask " + price)
    }
  }
}
