// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api;

import com.google.inject.Scopes;
import com.google.inject.assistedinject.FactoryProvider;
import com.google.inject.name.Names;
import com.lab616.common.flags.Flag;
import com.lab616.common.flags.Flags;
import com.lab616.omnibus.Main;
import com.lab616.omnibus.event.AbstractEventModule;

/**
 * Guice module for IB TWS API.
 *
 * @author david
 *
 */
public class ApiClientModule extends AbstractEventModule {

  @Flag(name = "ib-api-host")
  public static String API_HOST = "localhost";

  @Flag(name = "ib-api-port")
  public static Integer API_PORT = 7496;
  
  static {
    Flags.register(ApiClientModule.class);
  }

  public void configure() {
    bindConstant().annotatedWith(Names.named("ib-api-host"))
      .to(API_HOST);
    bindConstant().annotatedWith(Names.named("ib-api-port"))
      .to(API_PORT);
    
    bind(ApiClient.Factory.class).toProvider(
        FactoryProvider.newFactory(ApiClient.Factory.class, ApiClient.class))
        .in(Scopes.SINGLETON);
    
    bind(Main.Shutdown.class).annotatedWith(Names.named("ib-api-shutdown"))
    .to(ApiService.class).in(Scopes.SINGLETON);

    bindEventWatcher(ApiService.class);
  }
}
