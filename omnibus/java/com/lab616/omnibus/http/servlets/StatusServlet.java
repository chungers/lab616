// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.http.servlets;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.lab616.monitoring.Varz;
import com.lab616.monitoring.Varzs;
import com.lab616.omnibus.SystemEvent;
import com.lab616.omnibus.event.EventEngine;

/**
 * Simple 'ping' servlet that also sends a SystemEvent into the EventEngine.
 * 
 * @author david
 *
 */
public class StatusServlet extends HttpServlet {

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
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		statuszCalls.incrementAndGet();
		try {
			SystemEvent event = new SystemEvent();
			event.setComponent("system");
			event.setMethod("ping");
			
			eventEngine.post(event);

			sentEvents.incrementAndGet();
			resp.setContentType("text/plain");
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().println("OK");
		} catch (Exception e) {
			errorSentEvents.incrementAndGet();
			resp.setContentType("text/plain");
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().println("SYSTEM-EVENT-ERROR");
		}
	}
}
