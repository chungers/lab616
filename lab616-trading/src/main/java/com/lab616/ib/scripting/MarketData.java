package com.lab616.ib.scripting;

import com.google.common.base.Function;
import com.google.inject.Inject;
import com.lab616.common.scripting.ScriptObject;
import com.lab616.common.scripting.ScriptObject.Script;
import com.lab616.common.scripting.ScriptObject.ScriptModule;
import com.lab616.ib.api.TWSClient;
import com.lab616.ib.api.TWSClientManager;
import com.lab616.ib.api.builders.ContractBuilder;
import com.lab616.ib.api.builders.IndexBuilder;
import com.lab616.ib.api.builders.IndexBuilder.Exchange;
import com.lab616.ib.api.builders.MarketDataRequestBuilder;
import com.lab616.ib.api.builders.OptionContractBuilder;
import org.apache.log4j.Logger;

/**
 *
 * @author dchung
 */
@ScriptModule(name = "MarketData",
doc = "Basic scripts for market data.  These are only tick data.")
public class MarketData extends ScriptObject {

  private static Logger logger = Logger.getLogger(MarketData.class);
  private final TWSClientManager clientManager;

  @Inject
  public MarketData(TWSClientManager clientManager) {
    this.clientManager = clientManager;
  }

  @Script(name = "requestIndexTicks",
  doc = "Requests index tick data.")
  public void requestIndexTicks(
    @Parameter(name="p") final String profile,
    @Parameter(name="idx", doc="The symbol of the Index.") final String index,
    @Parameter(name="ex", doc="Name of exchange.") final String exchange) {
    logger.info(String.format(
      "START: requestIndexData profile=%s, index=%s, exchange=%s",
      profile, index, exchange));
    this.clientManager.enqueue(profile,
      new Function<TWSClient, Boolean>() {

        @Override
        public Boolean apply(TWSClient client) {
          client.requestTickData(
            new MarketDataRequestBuilder().withDefaultsForIndex().forIndex(new IndexBuilder(index).setExchange(
            Exchange.valueOf(exchange))));
          return true;
        }
      });
    logger.info(String.format(
      "OK: requestIndexData profile=%s, index=%s, exchange=%s",
      profile, index, exchange));
  }

  @Script(name = "requestEquityTicks",
  doc = "Requests equity tick data.")
  public void requestEquityTicks(
    @Parameter(name="p") final String profile,
    @Parameter(name="s") final String symbol) {
    // Equity
    logger.info("Requesting TICKS data for " + symbol + " on " + profile);
    this.clientManager.enqueue(profile, new Function<TWSClient, Boolean>() {
      @Override
      public Boolean apply(TWSClient client) {
        client.requestTickData(
          new MarketDataRequestBuilder().withDefaultsForStocks()
          .forStock(new ContractBuilder(symbol)));
        return true;
      }
    });
  }

  @Script(name = "requestCallOptionTicks",
  doc = "Requests call option tick data.")
  public void requestCallOptionTicks(
    @Parameter(name="p") final String profile,
    @Parameter(name="s") final String symbol,
    double strike, int monthsFromNow) {
    requestOptionTicks(Option.CALL, profile, symbol, strike, monthsFromNow);
  }

  @Script(name = "requestPutOptionTicks",
  doc = "Requests put option tick data.")
  public void requestPutOptionTicks(
    @Parameter(name="p") final String profile,
    @Parameter(name="s") final String symbol,
    double strike, int monthsFromNow) {
    requestOptionTicks(Option.PUT, profile, symbol, strike, monthsFromNow);
  }

  enum Option {
    CALL,
    PUT;
  }

  void requestOptionTicks(Option option, String profile, final String symbol,
    double strike, int monthsFromNow) {
    // Option
    final OptionContractBuilder ocb = new OptionContractBuilder(symbol);
    switch (option) {
      case CALL:
        ocb.forCall(); break;
      case PUT:
        ocb.forPut(); break;
    }
    ocb.setExpiration(monthsFromNow);
    ocb.setStrike(strike);
    logger.info("Requesting TICKS data for " + option + " option on "
      + symbol + " on " + profile + " strike = " + strike + " expiry = "
      + monthsFromNow + " symbol = " + ocb.getOptionSymbol());
    this.clientManager.enqueue(profile, new Function<TWSClient, Boolean>() {
      @Override
      public Boolean apply(TWSClient client) {
        client.requestTickData(
          new MarketDataRequestBuilder().withDefaultsForOptions()
          .forOption(ocb, true));
        return true;
      }
    });
  }

}
