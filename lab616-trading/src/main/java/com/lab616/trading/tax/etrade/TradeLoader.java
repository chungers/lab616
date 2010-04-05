// 2009-2010 lab616.com, All Rights Reserved.
package com.lab616.trading.tax.etrade;

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.inject.internal.ImmutableMap;
import com.lab616.common.Pair;
import com.lab616.trading.tax.CsvLoader;
import com.lab616.trading.trades.proto.Trades.OrderType;
import com.lab616.trading.trades.proto.Trades.Trade;

/**
 * Class that load a CSV file of trades from ETrade.
 * @author david
 *
 */
public class TradeLoader extends CsvLoader<Trade> {

  private static Logger logger = Logger.getLogger(TradeLoader.class);
  
  public TradeLoader(String path) throws IOException {
    super(path);
  }
  
  private static Map<String, OrderType> orderTypeMapping = ImmutableMap.of(
      "Buy", OrderType.BUY, 
      "Sell", OrderType.SELL,
      "Buy Open", OrderType.BUY_OPEN, 
      "Sell To Close", OrderType.SELL_CLOSE,
      "Option Expire", OrderType.OPTION_EXPIRE);
  
  @Override
  protected Trade parse(String line) {
    logger.debug(getLineNumber() + ":loading " + line);
    String[] parts = line.split(",");

    try {
      Pair<Long, Long> t = getTimeStampTradeId(parts[0]);
      Trade.Builder trade = Trade.newBuilder()
        .setDate(parts[0])
        .setOrderType(orderTypeMapping.get(parts[1]))
        .setSecurity(parts[2])
        .setDescription(parts[4])
        .setQuantity(Integer.parseInt(parts[5]))
        .setPrice(Float.parseFloat(parts[6]))
        .setNet(Float.parseFloat(parts[8]))
        .setTimestamp(t.first)
        .setTradeId(t.second);
      
      // Optional fields
      try {
        trade.setCommission(Float.parseFloat(parts[7]));
      } catch (NumberFormatException e) {
        // Do nothing -- it's in the case of no commission (N/A)
      }
      
      if (parts[3].length() > 0) {
        trade.setCusip(parts[3]);
      }
      if (!trade.isInitialized()) {
        return null;
      }
      return trade.build();
    } catch (IllegalArgumentException e) {
      // parse problem: not a date.
      return null;
    } catch (Exception e) {
      logger.debug("Parse error:", e);
      // parse problem
      return null;
    }
  }
}
