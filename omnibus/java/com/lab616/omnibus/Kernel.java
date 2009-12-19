// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.internal.Lists;
import com.google.inject.internal.Sets;
import com.google.inject.name.Names;
import com.lab616.common.flags.Flag;
import com.lab616.common.flags.Flags;
import com.lab616.common.logging.Logging;
import com.lab616.monitoring.Varz;
import com.lab616.monitoring.Varzs;
import com.lab616.omnibus.event.EventEngine;
import com.lab616.omnibus.event.EventModule;
import com.lab616.omnibus.http.HttpServer;
import com.lab616.omnibus.http.HttpServerModule;

/**
 * Core class for an Omnibus container/ application.  It contains a set of
 * basic services such as the http server and event engine.
 *
 * @author david
 *
 */
public abstract class Kernel {
	
  @Flag(name = "server-id")
  @Varz(name = "server-id")
  public static String serverId = "obus-" + System.currentTimeMillis();
	
  @Flag(name = "log-level")
  public static String logLevel = "INFO";
	
  @Flag(name = "profile")
  public static String profile = null;

  static {
  	Varzs.export(Kernel.class);
  	Flags.register(Kernel.class);
  }
  
  /**
   * Interface for shutdown handler.
   *
   * @param <V> The return value.
   */
  public interface Shutdown<V> {
    public String getName();
    public V call() throws Exception;
  }
  
  static Logger logger = Logger.getLogger(Kernel.class);
  
  private Injector injector;

  public Set<? extends Module> getModules() {
    return Sets.newHashSet();
  }

  // TODO(david):  Implement priority based shutdown sequence.
  public final List<Shutdown<?>> getShutdownRoutines() {
  	// System-wide services.
  	List<Shutdown<?>> list = Lists.newArrayList();
    list.add(getInstance(Shutdown.class, EventModule.SHUTDOWN_HOOK));
    addShutdown(list);
    list.add(getInstance(Shutdown.class, HttpServerModule.SHUTDOWN_HOOK));
    return list;
  }

  protected void addShutdown(List<Shutdown<?>> list) {
  	// No-op.
  }
  
  private Runnable getShutdownHook() {
    return new Runnable() {
      public void run() {
        logger.info("Starting shutdown sequence:");
        for (Shutdown<?> callable : getShutdownRoutines()) {
          try {
            logger.info("Completed shutdown step (" + 
                callable.getName() + ") => " + callable.call());
          } catch (Exception e) {
            logger.error("Failure during shutdown (" + callable.getName() + 
                "):", e);
          }
        }
        logger.info("Shutdown sequence completed.");
      }
    };
  }
  
  /**
   * Subclass overrides this method to start running the application. 
   * The implementation can call methods such as {@link #getInstance} to
   * obtain injected objects and start the program sequence.
   * 
   * @throws Exception
   */
  protected abstract void run() throws Exception;
  
  /**
   * Starts the program with command line arguments.  This method is called
   * in the main method of an application which instantiates a derived class.
   * 
   * @param argv Command line arguments.
   * @throws Exception Uncaught exception.
   */
  public final void run(String[] argv) throws Exception {
    // Load all the modules.  This will also force any Flag registration
    // to occur so that the flag parsing will apply to all the module class
    // where the flag values are used for injection.
    List<Module> allModules = Lists.newArrayList();
    allModules.add(new HttpServerModule());
    allModules.add(new EventModule());
    allModules.addAll(Lists.newArrayList(getModules()));

    logger.info("Included modules: " + allModules);
    
    // By now all flags are registered as the module classes were loaded.
    // It's now safe to parse and set all the flags.
    Flags.parse(argv);
    
    // Check to see if profile is specified.
    if (profile != null) {
      Properties props = new Properties();
      File pf = new File(profile);
      if (pf.exists()) {
        props.load(new FileInputStream(profile));
        Flags.parse(props);
      }
    }

    Logging.init(logLevel);
    
    // Now the injector gets created and during this process, the flag values
    // are read and used by the module bindings.
    injector = Guice.createInjector(allModules);
    
    // Get any shutdown hooks for the server:
    Thread shutdown = new Thread(getShutdownHook());
    shutdown.setName(getClass().getName() + ":shutdown");
    Runtime.getRuntime().addShutdownHook(shutdown);

    // Start all services.
    try {
    	getInstance(HttpServer.class).start();
    	getInstance(EventEngine.class).start();
    } catch (Exception e) {
    	logger.warn("Exception during startup of key components. Shutting down.", e);
    	System.exit(-1);
    }
    // Run the main.
    run();
  }
  
  public final <T> T getInstance(Class<T> clz, String name) {
    return getInstance(Key.get(clz, Names.named(name)));
  }
  
  public final <T> T getInstance(Class<T> clz) {
    return injector.getInstance(clz);
  }
  
  public final <T> T getInstance(Key<T> key) {
    return injector.getInstance(key);
  }

  /**
   * Simple main for testing, with only the platform components running.
   * 
   * @param argv Command line args
   * @throws Exception
   */
	public static void main(String[] argv) throws Exception {
  	Kernel main = new Kernel() {
			@Override
      public void run() throws Exception {
				logger.info("Running.");
			}
  	};
  	main.run(argv);
  }
}
