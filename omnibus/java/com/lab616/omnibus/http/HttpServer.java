// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.http;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * HTTP server based on jetty.
 *
 * @author david
 *
 */
public class HttpServer {

  static Logger logger = Logger.getLogger(HttpServer.class);
  
  final private int port;
  
  final private Server server;
  
  @Inject
  public HttpServer(@Named("http")int port) throws Exception {
    this.port = port;
    
    logger.info("HttpServer @ port = " + this.port);
    this.server = new Server(this.port);

    ContextHandlerCollection contexts = new ContextHandlerCollection();
    server.setHandler(contexts);
    
    Context root = new Context(contexts, "/", Context.SESSIONS);
    addStatusz(root);
  }
  
  protected void addStatusz(Context context) {
    context.addServlet(new ServletHolder(new HttpServlet() {

      private static final long serialVersionUID = 4358999418538102949L;

      @Override
      protected void doGet(HttpServletRequest req, HttpServletResponse resp)
          throws ServletException, IOException {
        resp.setContentType("text/plain");
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().println("OK");
      }
    }), "/statusz");
    
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
}
