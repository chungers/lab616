// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.http.servlets;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.inject.Inject;
import com.lab616.monitoring.Varz;
import com.lab616.monitoring.Varzs;
import com.lab616.omnibus.SystemEvent;
import com.lab616.omnibus.event.EventEngine;
import com.lab616.omnibus.http.BasicServlet;

/**
 * Simple 'ping' servlet that also sends a SystemEvent into the EventEngine.
 * 
 * @author david
 *
 */
public class StatusServlet extends BasicServlet {

	@Varz(name = "statusz-invocations")
	public static AtomicInteger statuszCalls = new AtomicInteger(0);
	
	@Varz(name = "statusz-system-event-sent")
	public static AtomicInteger sentEvents = new AtomicInteger(0);

	@Varz(name = "statusz-system-event-sent-errors")
	public static AtomicInteger errorSentEvents = new AtomicInteger(0);

	static {
		Varzs.export(StatusServlet.class);
	}

	private static final long serialVersionUID = 1L;
	
	@Inject
	private EventEngine eventEngine;
	
	@Override
  protected void processRequest(Map<String, String> params, 
      ResponseBuilder b) {
  	statuszCalls.incrementAndGet();
  	try {
  		SystemEvent event = new SystemEvent();
  		event.setComponent("system");
  		event.setMethod("ping");
  		
  		eventEngine.post(event);

  		sentEvents.incrementAndGet();
  		for (Map.Entry<String, String> p : System.getenv().entrySet()) {
  		  b.println(String.format("%s=%s", p.getKey(), p.getValue()));
  		}
      for (Map.Entry<Object, Object> p : System.getProperties().entrySet()) {
        b.println(String.format("%s=%s", p.getKey(), p.getValue()));
      }
  	} catch (Exception e) {
  		b.println("Exception: " + e);
  	}
  }
}
