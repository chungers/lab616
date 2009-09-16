// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.http.servlets;

import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
public class SystemEventServlet extends HttpServlet {

	@Varz(name = "system-event-invocations")
	public static AtomicInteger calls = new AtomicInteger(0);
	
	@Varz(name = "system-event-sent")
	public static AtomicInteger sentEvents = new AtomicInteger(0);

	@Varz(name = "system-event-sent-errors")
	public static AtomicInteger errorSentEvents = new AtomicInteger(0);

	static {
		Varzs.export(SystemEventServlet.class);
	}

	private static final long serialVersionUID = 1L;
	
	static Logger logger = Logger.getLogger(SystemEventServlet.class);
	
	@Inject
	private EventEngine eventEngine;
	
	@Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
	  doGet(req, resp);
  }

	@SuppressWarnings("unchecked")
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		calls.incrementAndGet();
		try {
			SystemEvent event = new SystemEvent();

			Enumeration<String> params = req.getParameterNames();
			while (params.hasMoreElements()) {
				String p = params.nextElement();
				String v = req.getParameter(p);
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

			resp.setContentType("text/plain");
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().println("OK");
		} catch (Exception e) {
			logger.warn("Could not send system event:", e);
			errorSentEvents.incrementAndGet();
			resp.setContentType("text/plain");
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().println("SYSTEM-EVENT-ERROR");
		}
	}
}
