// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.http;

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
import com.lab616.omnibus.Main;

/**
 * HTTP server based on jetty.
 *
 * @author david
 *
 */
public class HttpServer implements Provider<Main.Shutdown<Void>>{

	
  static Logger logger = Logger.getLogger(HttpServer.class);
  
  final private int port;
  
  final private Server server;
  final private Map<String, HttpServlet> servletMap;
  
  @Inject
  public HttpServer(@Named("http")int port, Map<String, HttpServlet> mapping) 
  	throws Exception {
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

  public void start() throws Exception {
    logger.info("Starting http server.");
    server.start();
  }
  
  public void stop() {
    logger.info("Stopping http server.");
    try {
      this.server.stop();
      logger.info("Http server stopped.");
    } catch (Exception e) {
      logger.error("Exception while stopping http server:", e);
    }
  }

  /**
   * Implements the Provider<Shutdown> interface.
   */
  public Main.Shutdown<Void> get() {
    return new Main.Shutdown<Void>() {
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
