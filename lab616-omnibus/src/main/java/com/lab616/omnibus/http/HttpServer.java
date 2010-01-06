// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.http;

import java.net.BindException;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.lab616.omnibus.Kernel;

/**
 * HTTP server based on jetty.
 *
 * @author david
 *
 */
public class HttpServer implements Kernel.Startable, Provider<Kernel.Shutdown<Void>>{

	
  static Logger logger = Logger.getLogger(HttpServer.class);
  
  private int port;
  private Server server;
  
  final private Map<String, HttpServlet> servletMap;
  private boolean retryWithDifferentPort = false;
  
  @Inject
  public HttpServer(@Named("http")int port, Map<String, HttpServlet> mapping) 
  	throws Exception {
    logger.info("Servlet bindings: " + mapping);
    this.port = port;
    this.servletMap = mapping;
    logger.info("HttpServer @ port = " + this.port);
    this.server = new Server(this.port);

    ContextHandlerCollection contexts = new ContextHandlerCollection();
    server.setHandler(contexts);
    
    Context root = new Context(contexts, "/", Context.SESSIONS);
    for (Entry<String, HttpServlet> map : mapping.entrySet()) {
    	root.addServlet(new ServletHolder(map.getValue()), map.getKey());
    }
  }

  protected final Map<String, HttpServlet> getServletMap() {
  	return this.servletMap;
  }
  
  public final int getPort() {
    return port;
  }

  public final HttpServer setRetryWithDifferentPort(boolean t) {
  	this.retryWithDifferentPort = t;
  	return this;
  }
  
  public void start() throws Exception {
    logger.info("Starting http server.");
    boolean started = false;
    do {
      try {
        this.server.start();
        started = true;
      } catch (BindException e) {
      	if (retryWithDifferentPort) {
      		this.port = HttpServerModule.generateHttpPortNumber();
      		this.server = new Server(this.port);
      		logger.info("Retrying http server with port = " + this.port);
      	} else {
      		throw e;
      	}
      }
    } while (!started);
  }
  
  public void stop() {
    logger.info("Stopping http server.");
    try {
    	this.server.setStopAtShutdown(true);
      logger.info("Http server stopped.");
    } catch (Exception e) {
      logger.error("Exception while stopping http server:", e);
    }
  }

  /**
   * Implements the Provider<Shutdown> interface.
   */
  public Kernel.Shutdown<Void> get() {
    return new Kernel.Shutdown<Void>() {
      public String getName() {
        return "http-shutdown";  // by convention, use the annotation name.
      }
      public Void call() throws Exception {
        stop();
        return null;
      }
    };
  }
}