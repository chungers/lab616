// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api.servlets;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.lab616.common.Pair;
import com.lab616.ib.api.TWSClient;
import com.lab616.ib.api.TWSClientManager;
import com.lab616.ib.api.builders.ContractBuilder;
import com.lab616.ib.api.builders.MarketDataRequestBuilder;
import com.lab616.monitoring.Varz;
import com.lab616.monitoring.Varzs;
import com.lab616.omnibus.http.servlets.BasicServlet;

/**
 *
 *
 * @author david
 *
 */
public class TWSController extends BasicServlet {

  private static final long serialVersionUID = 1L;

  @Varz(name = "tws-invocations")
  public static AtomicInteger calls = new AtomicInteger(0);
  
  @Varz(name = "tws-invocation-errors")
  public static AtomicInteger errors = new AtomicInteger(0);

  static {
    Varzs.export(TWSController.class);
  }

  static Logger logger = Logger.getLogger(TWSController.class);

  private TWSClientManager ibService;

  @Inject
  public TWSController(TWSClientManager s) {
    ibService = s;
    logger.info("Got IBService: " + s);
  }
  
  @Override
  protected void processRequest(Map<String, String> params, ResponseBuilder b) {
    logger.info("TWSController invoked: " + params);
    calls.incrementAndGet();
    
    String cmd = params.get("m");
    
    if ("stats".equals(cmd)) {
      for (Pair<String, TWSClient.State> stat : ibService.getStats()) {
        b.println(stat.toString());
      }
      return;
    }

    String name = params.get("client");
    String id = params.get("id");
    TWSClient client = ibService.getClient(name, Integer.parseInt(id));
    if (client == null) {
      b.setError().println("No client:" + name);
      return;
    }
    if ("ping".equals(cmd)) {
      String timeout = params.get("timeout");
      Long tout = 5000L; 
      if (timeout != null) {
        try {
          tout = Long.decode(timeout);
        } catch (Exception e) {
          
        }
      }
      b.println("now=" + client.ping(tout, TimeUnit.MILLISECONDS));
    }
    if ("hist".equals(cmd)) {
      String timeout = params.get("timeout");
      Long tout = 5000L; 
      if (timeout != null) {
        try {
          tout = Long.decode(timeout);
        } catch (Exception e) {
          
        }
      }
      final String symbol = params.get("symbol");

      for (String s : client.requestHistoricalData(
          new MarketDataRequestBuilder().withDefaultsForStocks()
          .forStock(new ContractBuilder(symbol)), tout, TimeUnit.MILLISECONDS)) {
        b.println(s);
      }
    }
  }
}
