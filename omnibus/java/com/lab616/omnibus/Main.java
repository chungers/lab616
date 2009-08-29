// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus;

import java.util.List;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.internal.ImmutableList;
import com.google.inject.internal.Lists;
import com.lab616.common.flags.Flags;
import com.lab616.common.logging.Logging;
import com.lab616.omnibus.http.HttpServer;
import com.lab616.omnibus.http.HttpServerModule;

/**
 * Main class for an Omnibus container/ application.
 *
 * @author david
 *
 */
public abstract class Main {
  
  static Logger logger = Logger.getLogger(Main.class);
  
  private Injector injector;
  
  public List<Module> getModules() {
    return ImmutableList.of();
  }

  public List<Callable<?>> getShutdownRoutines() {
    return ImmutableList.of();
  }
  
  private Runnable getShutdownHook() {
    return new Runnable() {
      @Override
      public void run() {
        logger.info("Starting shutdown sequence:");
        for (Callable<?> callable : getShutdownRoutines()) {
          try {
            logger.info("Completed shutdown step: " + callable.call());
          } catch (Exception e) {
            logger.error("Failure during shutdown:", e);
          }
        }
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
  public abstract void run() throws Exception;
  
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
    allModules.addAll(getModules());

    // By now all flags are registered as the module classes were loaded.
    // It's now safe to parse and set all the flags.
    Flags.parse(argv);
    Logging.init();
    
    Thread shutdown = new Thread(getShutdownHook());
    shutdown.setName("shutdown");
    Runtime.getRuntime().addShutdownHook(shutdown);
    
    // Now the injector gets created and during this process, the flag values
    // are read and used by the module bindings.
    injector = Guice.createInjector(allModules);
    
    getInstance(HttpServer.class).start();
    run();
  }
  
  public final <T> T getInstance(Class<T> clz) {
    return injector.getInstance(clz);
  }
  
  public final <T> T getInstance(Key<T> key) {
    return injector.getInstance(key);
  }
}
