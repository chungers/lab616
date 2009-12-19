// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.http.servlets;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.internal.Maps;
import com.lab616.monitoring.Varz;
import com.lab616.monitoring.VarzMap;
import com.lab616.monitoring.Varzs;

/**
 *
 *
 * @author david
 *
 */
public class BasicServlet extends HttpServlet {

  @Varz(name = "servlet-invocations")
  public static Map<String, AtomicLong> servletCalls = 
    VarzMap.create(AtomicLong.class);
  
  static {
    Varzs.export(BasicServlet.class);
  }

  private static final long serialVersionUID = 1L;

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    doGet(req, resp);
  }

  public static class ResponseBuilder {
    HttpServletResponse response;
    boolean error = false;
    boolean built = false;
    ResponseBuilder(HttpServletResponse resp) {
      response = resp;
      response.setContentType("text/plain");
    }
    
    public ResponseBuilder setError() {
      error = true;
      return this;
    }
    
    public ResponseBuilder println(String line) {
      try {
        response.getWriter().println(line);
        response.getWriter().flush();
        return this;
      } catch (IOException e) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        throw new RuntimeException(e);
      }
    }
    public boolean isBuilt() {
    	return built;
    }
    
    public HttpServletResponse build() {
      if (!error) {
        response.setStatus(HttpServletResponse.SC_OK);
      } else {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
      built = true;
      return response;
    }
  }
  
  @SuppressWarnings("unchecked")
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
  throws ServletException, IOException {
    // Take the request parameters
    ResponseBuilder builder = new ResponseBuilder(resp);
    Map<String, String> params = Maps.newHashMap();
    Enumeration<String> names = req.getParameterNames();
    while (names.hasMoreElements()) {
      String p = names.nextElement();
      params.put(p, req.getParameter(p).trim());
    }
    processRequest(params, builder);
    if (!builder.isBuilt()) {
      builder.build();
    }
    servletCalls.get(getClass().getSimpleName()).incrementAndGet();
  }
  
  
  protected void processRequest(Map<String, String> params, 
      ResponseBuilder b) {
    
  }
}
