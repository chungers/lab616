// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.http;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import com.lab616.common.flags.Flag;
import com.lab616.common.flags.Flags;
import com.lab616.omnibus.Main;

/**
 * Guice module
 *
 * @author david
 *
 */
public class HttpServerModule implements Module {

  // The convention is to put Flags inside the Module and leave explicitly
  // named dependencies in the constructor of the injected class.
  @Flag(name="http", defaultValue="8888")
  public static Integer HTTP_PORT;
  
  static {
    Flags.register(HttpServerModule.class);
  }
  
  
  public void configure(Binder binder) {

    binder.bindConstant().annotatedWith(Names.named("http"))
      .to(HTTP_PORT);
    
    binder.bind(Main.Shutdown.class).annotatedWith(Names.named("http-shutdown"))
      .toProvider(HttpServer.class).in(Scopes.SINGLETON);

    binder.bind(HttpServer.class).in(Scopes.SINGLETON);
  }
}
