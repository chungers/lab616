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
import com.ib.client.EClientSocket;
import com.ib.client.EWrapper;
import com.lab616.common.flags.Flag;
import com.lab616.common.flags.Flags;
import com.lab616.ib.api.simulator.EClientSocketSimulator;
import com.lab616.omnibus.Main;
import com.lab616.omnibus.event.AbstractEventModule;
import com.lab616.omnibus.event.ObjectEventDefinition;

/**
 * Guice module for IB TWS API.
 *
 * @author david
 *
 */
public class TWSClientModule extends AbstractEventModule {

  @Flag(name = "tws-host")
  public static String API_HOST = "localhost";

  @Flag(name = "tws-port")
  public static Integer API_PORT = 7496;
  
  @Flag(name = "tws-max-connections")
  public static Integer API_MAX_CONNECTIONS = 10;
  
  @Flag(name = "tws-max-retries")
  public static Integer API_MAX_RETRIES = 120;
  
  @Flag(name = "tws-use-simulator")
  public static Boolean USE_SIMULATOR = false;
  
  static {
    Flags.register(TWSClientModule.class);
  }

  @Provides @Singleton @Named("tws-executor")
  public ExecutorService getExecutorService() {
    return new ScheduledThreadPoolExecutor(API_MAX_CONNECTIONS);
  }
  
  public void configure() {
    // Config constants.
    bindConstant().annotatedWith(Names.named("tws-host"))
      .to(API_HOST);
    bindConstant().annotatedWith(Names.named("tws-port"))
      .to(API_PORT);
    bindConstant().annotatedWith(Names.named("tws-max-retries"))
      .to(API_MAX_RETRIES);
    
    // Event definitions.
    bindEventDefinition(new ObjectEventDefinition<TWSEvent>(
        TWSEvent.EVENT_NAME, TWSEvent.class));
    // Event watchers.
    bindEventWatcher(SystemEventProcessor.class);

    // Client factory
    bind(TWSClient.Factory.class).toProvider(
        FactoryProvider.newFactory(TWSClient.Factory.class, TWSClient.class))
        .in(Scopes.SINGLETON);
    
    bind(TWSClientManager.class).in(Scopes.SINGLETON);
  
    if (USE_SIMULATOR) {
      bind(EClientSocketFactory.class).toInstance(new EClientSocketFactory() {
        public EClientSocket create(String name, EWrapper wrapper) {
          EClientSocketSimulator sim = new EClientSocketSimulator(name);
          EClientSocket client = sim.create(wrapper);
          sim.start();
          return client;
        }
      });
    } else {
      bind(EClientSocketFactory.class).toInstance(new EClientSocketFactory() {
        public EClientSocket create(String name, EWrapper wrapper) {
          return new ManagedEClientSocket(wrapper);
        }
      });
    }
    
    bind(Main.Shutdown.class).annotatedWith(Names.named("tws-shutdown"))
    .to(TWSClientManager.class).in(Scopes.SINGLETON);
  }
}
