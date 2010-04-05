// 2009-2010 lab616.com, All Rights Reserved.
package com.lab616.trading.tax.etrade;

import org.apache.log4j.Level;

import com.lab616.common.logging.Logging;
import com.lab616.trading.tax.etrade.TradeLoader;
import com.lab616.trading.trades.proto.Trades.OrderType;
import com.lab616.trading.trades.proto.Trades.Trade;

import junit.framework.TestCase;

/**
 * @author david
 *
 */
public class TradeLoaderTest extends TestCase {

  public static String path = System.getProperty("user.home") + 
  "/lab616/lab616-trading/testing/etrade-2009-trades.csv";

  public static int SELLS = 153;
  
  static {
    Logging.init(Level.INFO);
  }
  
  public void testParse() throws Exception {
    TradeLoader loader = new TradeLoader(path);
    String line = "1/15/2009,Buy,FAZ,25459W607,\"DIREXION SHS ETF TR           FINANCIAL BEAR 3X SHARES\",20,55.5,12.99,1122.99";
    Trade trade = loader.parse(line);
    assertEquals("1/15/2009", trade.getDate());
    assertEquals(OrderType.BUY, trade.getOrderType());
    assertEquals("FAZ", trade.getSecurity());
    assertEquals(20, trade.getQuantity());
    assertEquals(55.5f, trade.getPrice());
    assertEquals(12.99f, trade.getCommission());
    assertEquals(1122.99f, trade.getNet());
    assertEquals("25459W607", trade.getCusip());
  }
  
  public void testLoad() throws Exception {
    int sells = 0;
    float gross = 0.f;
    TradeLoader loader = new TradeLoader(path);
    for (Trade trade : loader) {
      if (trade.getOrderType() == OrderType.SELL) {
        sells++;
        gross += trade.getNet();
      }
      assertNotNull(trade);
      assertTrue(trade.isInitialized());
      System.out.println(String.format("d=%s,ts=%s,id=%s,net=%s", 
          trade.getDate(), trade.getTimestamp(), 
          trade.getTradeId(), trade.getNet()));
    }
    System.out.println("Sells = " + sells + ", total = " + gross);
    assertEquals(SELLS, sells);
  }
}
