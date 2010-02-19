// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.http.servlets;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lab616.monitoring.Varz;
import com.lab616.monitoring.Varzs;
import com.lab616.omnibus.http.BasicServlet;
import com.lab616.omnibus.http.BasicServlet.ResponseBuilder;
import com.lab616.util.Time;

/**
 * @author david
 *
 */
public class VarzServlet extends BasicServlet {

	@Varz(name = "varz-invocations")
	public static AtomicInteger calls = new AtomicInteger(0);

	@Varz(name = "varz-last-sample-ts-usec")
	public static AtomicLong lastSampleTSusec = new AtomicLong(
			Time.now());
	
	@Varz(name = "varz-last-sample-elapsed-usec")
	public static AtomicLong lastSampleDTusec = new AtomicLong(0L);
	
	static {
		Varzs.export(VarzServlet.class);
	}

	private static final long serialVersionUID = 1L;

	@Override
  protected void processRequest(Map<String, String> params, 
      ResponseBuilder b) {
    calls.incrementAndGet();
    long ctUSec = Time.now();
    lastSampleDTusec.set(ctUSec - lastSampleTSusec.get());
    lastSampleTSusec.set(Time.now());
    for (String varz : Varzs.getValues()) {
    	b.println(varz);
    }
  }
}
