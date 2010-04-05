// 2009-2010 lab616.com, All Rights Reserved.
package com.lab616.trading.tax;

import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.Level;

import com.lab616.common.logging.Logging;
import com.lab616.trading.tax.TradeHistory.Visitor;
import com.lab616.trading.tax.etrade.TradeLoader;
import com.lab616.trading.tax.etrade.TradeLoaderTest;
import com.lab616.trading.trades.proto.Trades.OrderType;
import com.lab616.trading.trades.proto.Trades.Trade;

/**
 * @author david
 *
 */
public class TradeHistoryTest extends TestCase {

  static {
    Logging.init(Level.INFO);
  }

  @SuppressWarnings("unchecked")
  public void testLoad() throws Exception {
    TradeLoader loader = new TradeLoader(TradeLoaderTest.path);
    TradeHistory y2009 = new TradeHistory();
    y2009.load(loader);
    
    // Get all the SELLS
    List<Trade> sells = y2009.getByOrderType(OrderType.SELL);
    assertEquals((Integer)sells.size(), y2009.getCount((OrderType.SELL)));
    assertEquals((Integer)TradeLoaderTest.SELLS, y2009.getCount(OrderType.SELL));
    // Get all the BUYS
    List<Trade> buys = y2009.getByOrderType(OrderType.BUY);
    assertEquals((Integer)buys.size(), y2009.getCount((OrderType.BUY)));
    
    List<Trade> fazTrades = y2009.getBySecurity("FAZ");
    for (Trade t : fazTrades) {
      assertEquals("FAZ", t.getSecurity());
    }
    
    
    Visitor<Trade, Float> sumSell = new Visitor<Trade, Float>(0.f) {
      @Override
      public void visit(Trade t) {
        if (t.getOrderType() == OrderType.SELL) {
          set(get() + t.getNet());
        }
      }
    };
    Visitor<Trade, Float> sumBuy = new Visitor<Trade, Float>(0.f) {
      @Override
      public void visit(Trade t) {
        if (t.getOrderType() == OrderType.BUY) {
          set(get() + t.getNet());
        }
      }
    };
    
    y2009.visit(sumSell, sumBuy);
    System.out.println("Sell = " + sumSell.get() + ", Buy = " + sumBuy.get());
    System.out.println("Trades = " + y2009.getTrades());
    System.out.println("Gain/Loss = " + (sumSell.get() - sumBuy.get()));
   
    for (Trade t : sells) {
      System.out.println(String.format("%s,%s,%s,%s", 
          t.getDate(), t.getTradeId(), t.getSecurity(), t.getNet()));
    }
    
  }
  
  
}
