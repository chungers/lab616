// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryProvider;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.lab616.common.flags.Flag;
import com.lab616.common.flags.Flags;
import com.lab616.omnibus.Main;
import com.lab616.omnibus.event.AbstractEventModule;
import com.lab616.omnibus.event.ObjectEventDefinition;

/**
 * Guice module for IB TWS API.
 *
 * @author david
 *
 */
public class IBClientModule extends AbstractEventModule {

  @Flag(name = "ib-api-host")
  public static String API_HOST = "localhost";

  @Flag(name = "ib-api-port")
  public static Integer API_PORT = 7496;
  
  @Flag(name = "ib-api-max-connections")
  public static Integer API_MAX_CONNECTIONS = 10;
  
  @Flag(name = "ib-api-max-retries")
  public static Integer API_MAX_RETRIES = 120;
  
  static {
    Flags.register(IBClientModule.class);
  }

  @Provides @Singleton @Named("ib-api-executor")
  public ExecutorService getExecutorService() {
    return new ScheduledThreadPoolExecutor(API_MAX_CONNECTIONS);
  }
  
  public void configure() {
    // Config constants.
    bindConstant().annotatedWith(Names.named("ib-api-host"))
      .to(API_HOST);
    bindConstant().annotatedWith(Names.named("ib-api-port"))
      .to(API_PORT);
    bindConstant().annotatedWith(Names.named("ib-api-max-retries"))
      .to(API_MAX_RETRIES);
    
    // Event definitions.
    bindEventDefinition(new ObjectEventDefinition<IBEvent>(
        IBEvent.EVENT_NAME, IBEvent.class));
    // Event watchers.
    bindEventWatcher(IBSystemEventWatcher.class);

    // Client factory
    bind(IBClient.Factory.class).toProvider(
        FactoryProvider.newFactory(IBClient.Factory.class, IBClient.class))
        .in(Scopes.SINGLETON);
    
    bind(IBService.class).in(Scopes.SINGLETON);
    
    bind(Main.Shutdown.class).annotatedWith(Names.named("ib-api-shutdown"))
    .to(IBService.class).in(Scopes.SINGLETON);
  }
}
