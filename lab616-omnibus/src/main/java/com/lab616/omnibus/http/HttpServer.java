// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.http;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.BindException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.lab616.common.Converters;
import com.lab616.common.Pair;
import com.lab616.common.scripting.ScriptObject;
import com.lab616.common.scripting.ScriptObjects;
import com.lab616.common.scripting.ScriptObject.Parameter;
import com.lab616.common.scripting.ScriptObject.Script;
import com.lab616.common.scripting.ScriptObject.ScriptModule;
import com.lab616.common.scripting.ScriptObjects.Descriptor;
import com.lab616.omnibus.Kernel;

/**
 * HTTP server based on jetty.
 *
 * @author david
 *
 */
@Singleton
public class HttpServer implements Kernel.Startable, Provider<Kernel.Shutdown<Void>>{

	public static final String HELP_PARAM = ".help";
	
  static Logger logger = Logger.getLogger(HttpServer.class);
  
  private int port;
  private Server server;
  
  final private Map<String, HttpServlet> servletMap;
  final private ScriptObjects scriptObjects;
  
  private boolean retryWithDifferentPort = false;
  
  @Inject
  public HttpServer(@Named("http")int port, Map<String, HttpServlet> mapping,
      Map<String, ScriptObject> scriptObjectMap, ScriptObjects scriptObjects) 
  	throws Exception {
    logger.info("Servlet bindings: " + mapping);
    this.port = port;
    this.servletMap = mapping;
    this.scriptObjects = scriptObjects;
    
    logger.info("HttpServer @ port = " + this.port);
    this.server = new Server(this.port);

    ContextHandlerCollection contexts = new ContextHandlerCollection();
    server.setHandler(contexts);
    Context root = new Context(contexts, "/", Context.SESSIONS);

    addServlets(root, this.servletMap);
    addScriptObjects(root, this.scriptObjects.getScriptObjects());
  }

  protected void addServlets(Context root, Map<String, HttpServlet> mapping) {
    for (Entry<String, HttpServlet> map : mapping.entrySet()) {
      root.addServlet(new ServletHolder(map.getValue()), map.getKey());
    }
  }
  
  static String paramList(List<Pair<Parameter, Type>> list) {
    StringBuffer buff = new StringBuffer();
    StringBuffer help = new StringBuffer();
    int i = 0;
    for (Pair<Parameter, Type> p : list) {
      buff.append(p.first.name());
      help.append("#\t" + p.first.name() + ",\t" + p.second + ",\t" + p.first.doc());
      if (p.first.defaultValue().length() > 0) {
        buff.append("=" + p.first.defaultValue());
      }
      if (++i < list.size()) {
        buff.append(", ");
        help.append("\n");
      }
    }
    return String.format("%s\n%s\n", buff, help);
  }
  
  @SuppressWarnings("serial")
  protected void addScriptObjects(Context root, Iterable<ScriptObject> sobjects) {
    final Set<String> registered = Sets.newHashSet();

    for (ScriptObject s : sobjects) {
      final ScriptModule moduleAnnotation = s.getClass().getAnnotation(ScriptModule.class);
      final ServletScript servletAnnotation = s.getClass().getAnnotation(ServletScript.class);
      if (!(moduleAnnotation != null && servletAnnotation != null)) continue;
      if (registered.contains(servletAnnotation.path())) {
        continue;
      }
      
      registered.add(servletAnnotation.path());
      
      final List<Pair<String, Descriptor>> moduleSpec = Lists.newArrayList();
      // Look for any Scripts that also have ServletScript annotation
      for (Method m : s.getClass().getMethods()) {
        if (m.getAnnotation(Script.class) != null &&
            m.getAnnotation(ServletScript.class) != null) {
          
          final ScriptObject target = s;
          // Get the path + parameters
          final String path = servletAnnotation.path() + "/" + 
            m.getAnnotation(ServletScript.class).path();
          final Descriptor desc = ScriptObjects.getDescriptor(m);
          moduleSpec.add(Pair.of(path, desc));
          logger.info("Adding script servlet: " + path + " for " + desc.annotation.name());
          root.addServlet(new ServletHolder(new BasicServlet() {
            @Override
            protected void processRequest(Map<String, String> params, 
                ResponseBuilder b) {
              if (params.containsKey(HELP_PARAM)) {
                // Print help
                b.println(path);
                b.println(paramList(desc.params));
                b.println(desc.annotation.doc());
                b.build();
              } else {
                try {
                  Object[] args = new Object[desc.params.size()];
                  int i = 0;
                  for (Pair<Parameter, Type> p : desc.params) {
                    String key = p.first.name();
                    String defaultValue = p.first.defaultValue();
                    String value = params.get(key);
                    if (value == null && defaultValue != null && defaultValue.length() > 0) {
                      value = defaultValue;
                    }
                    // Peform type conversion.
                    args[i++] = Converters.fromString(value, p.second);
                  }
                  Object result = desc.method.invoke(target, args);
                  b.println("%s", result);
                } catch (Throwable e) {
                  b.setError().exception(e, "Exception: %s", e);
                }
                b.build();
              }
            }            
          }), path);
        }
      }
      
      logger.info("Adding script module: " + servletAnnotation.path() + 
          " for " + moduleAnnotation.name());
      // Add a special servlet for the top level module
      root.addServlet(new ServletHolder(
          new RScriptServlet(moduleAnnotation, servletAnnotation, moduleSpec)), servletAnnotation.path());
    }
    
    // Add a global servlet that generates the link to each module:
    root.addServlet(new ServletHolder(
        new BasicServlet() {
          @Override
          public void processRequest(Map<String, String> params, ResponseBuilder b) {
            for (String mpath : registered) {
              b.println("http://%s:%s%s", getHostName(), port, mpath);
            }
            b.build();
          }
        }), "/scripts");
  }

  String getHostName() {
    try {
      return InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
     // Nothing. 
    }
    return "localhost";
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
