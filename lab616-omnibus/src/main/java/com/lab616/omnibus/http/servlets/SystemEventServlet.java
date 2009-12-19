// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.http.servlets;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.lab616.monitoring.Varz;
import com.lab616.monitoring.Varzs;
import com.lab616.omnibus.SystemEvent;
import com.lab616.omnibus.event.EventEngine;

/**
 * Sends a SystemEvent to the event engine, which then routes to interested
 * parties.
 * 
 * To send a message:
 * curl http://localhost:8888/se -d c=event -d m=test -d a=1 -d b=2
 * where c is the component, m is the method, and a and b are free parameters.
 * 
 * @author david
 *
 */
public class SystemEventServlet extends BasicServlet {

	@Varz(name = "system-event-invocations")
	public static AtomicInteger calls = new AtomicInteger(0);
	
	@Varz(name = "system-event-sent")
	public static AtomicInteger sentEvents = new AtomicInteger(0);

	static {
		Varzs.export(SystemEventServlet.class);
	}

	private static final long serialVersionUID = 1L;
	
	static Logger logger = Logger.getLogger(SystemEventServlet.class);
	
	@Inject
	private EventEngine eventEngine;
	
	@Override
  protected void processRequest(Map<String, String> params, 
      ResponseBuilder b) {
		calls.incrementAndGet();
		try {
			SystemEvent event = new SystemEvent();
			for (String p : params.keySet()) {
				String v = params.get(p);
				if (p.equals("c")) {
					event.setComponent(v);
				} else if (p.equals("m")) {
					event.setMethod(v);
				} else {
					event.setParam(p, v);
				}
			}
			eventEngine.post(event);
			sentEvents.incrementAndGet();
			b.println("OK");
		} catch (Exception e) {
			b.println("Exception: " + e);
		}
  }
}
