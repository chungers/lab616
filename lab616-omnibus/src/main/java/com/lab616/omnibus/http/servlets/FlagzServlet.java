// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.http.servlets;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.lab616.common.flags.Flags;
import com.lab616.common.flags.Flags.Printable;
import com.lab616.monitoring.Varz;
import com.lab616.monitoring.Varzs;
import com.lab616.omnibus.http.BasicServlet;
import com.lab616.util.Time;

/**
 * Simple 'ping' servlet that also sends a SystemEvent into the EventEngine.
 * 
 * @author david
 *
 */
public class FlagzServlet extends BasicServlet {

  @Varz(name = "flagz-invocations")
  public static AtomicInteger calls = new AtomicInteger(0);

  @Varz(name = "flagz-last-sample-ts-usec")
  public static AtomicLong lastSampleTSusec = new AtomicLong(
      Time.now());
  
  @Varz(name = "flagz-last-sample-elapsed-usec")
  public static AtomicLong lastSampleDTusec = new AtomicLong(0L);
  
  static {
    Varzs.export(FlagzServlet.class);
  }

  private static final long serialVersionUID = 1L;

  @Override
  protected void processRequest(Map<String, String> params, 
      ResponseBuilder b) {
  	calls.incrementAndGet();
    long ctUSec = Time.now();
    lastSampleDTusec.set(ctUSec - lastSampleTSusec.get());
    lastSampleTSusec.set(Time.now());
    for (Printable flagz : Flags.listAll()) {
      b.println(
          String.format("%s(%s)=%s", flagz.getFlagName(), 
              flagz.getCodeLocation(),
              flagz.getCurrentValue()));
    }
  }

}
