// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.http.servlets;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lab616.monitoring.Varz;
import com.lab616.monitoring.Varzs;

/**
 * @author david
 *
 */
public class StatusServlet extends HttpServlet {

	@Varz(name = "statusz-invocations")
	public static AtomicInteger statuszCalls = new AtomicInteger(0);
	
	static {
		Varzs.export(StatusServlet.class);
	}

	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		resp.setContentType("text/plain");
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.getWriter().println("OK");
		statuszCalls.incrementAndGet();
	}
}
