// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.http;

import java.util.Random;

import com.google.inject.Scopes;
import com.google.inject.name.Names;
import com.lab616.common.flags.Flag;
import com.lab616.common.flags.Flags;
import com.lab616.omnibus.Kernel;
import com.lab616.omnibus.http.servlets.FlagzServlet;
import com.lab616.omnibus.http.servlets.QuitServlet;
import com.lab616.omnibus.http.servlets.StatusServlet;
import com.lab616.omnibus.http.servlets.SystemEventServlet;
import com.lab616.omnibus.http.servlets.VarzServlet;

/**
 * Guice module
 *
 * @author david
 *
 */
public class HttpServerModule extends AbstractHttpServletModule {

  // The convention is to put Flags inside the Module and leave explicitly
  // named dependencies in the constructor of the injected class.
  @Flag(name="http")
  public static Integer HTTP_PORT = generateHttpPortNumber();
  
  static {
    Flags.register(HttpServerModule.class);
  }
  
  public static final String SHUTDOWN_HOOK = "http-shutdown";

  public static int generateHttpPortNumber() {
  	Random rnd = new Random(System.currentTimeMillis());
  	HTTP_PORT = rnd.nextInt(1000) + 5000;
  	return HTTP_PORT;
  }
  
  public void configure() {
    bindConstant().annotatedWith(Names.named("http"))
      .to(HTTP_PORT);
    
    bind(Kernel.Shutdown.class).annotatedWith(Names.named(SHUTDOWN_HOOK))
      .toProvider(HttpServer.class).in(Scopes.SINGLETON);

    bind(HttpServer.class).in(Scopes.SINGLETON);
    
    // System servlets
    bind("/flagz", FlagzServlet.class);
    bind("/statusz", StatusServlet.class);
    bind("/varz", VarzServlet.class);
    bind("/quitquitquit", QuitServlet.class);
    bind("/se", SystemEventServlet.class);
  }
}
