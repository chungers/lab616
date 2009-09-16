// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.http.servlets;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lab616.monitoring.Varz;
import com.lab616.monitoring.Varzs;
import com.lab616.util.Time;

/**
 * @author david
 *
 */
public class VarzServlet extends HttpServlet {

	@Varz(name = "varz-invocations")
	public static AtomicInteger varzCalls = new AtomicInteger(0);

	@Varz(name = "varz-last-sample-ts-usec")
	public static AtomicLong lastSampleTSusec = new AtomicLong(
			Time.now());
	
	@Varz(name = "varz-last-sample-elapsed-usec")
	public static AtomicLong lastSampleDTusec = new AtomicLong(0L);
	
	static {
		Varzs.export(VarzServlet.class);
	}

	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    varzCalls.incrementAndGet();
    long ctUSec = Time.now();
    lastSampleDTusec.set(ctUSec - lastSampleTSusec.get());
    lastSampleTSusec.set(Time.now());
    resp.setContentType("text/plain");
    resp.setStatus(HttpServletResponse.SC_OK);
    for (String varz : Varzs.getValues()) {
    	resp.getWriter().println(varz);
    }
  }
}
