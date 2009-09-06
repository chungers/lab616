// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus;

import java.util.List;

import org.apache.log4j.Logger;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.internal.ImmutableList;
import com.google.inject.internal.Lists;
import com.google.inject.name.Names;
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
  
  public interface Shutdown<V> {
    public String getName();
    public V call() throws Exception;
  }
  
  static Logger logger = Logger.getLogger(Main.class);
  
  private Injector injector;
  
  public List<Module> getModules() {
    return ImmutableList.of();
  }

  public Shutdown<?>[] getShutdownRoutines() {
    return new Shutdown<?>[] { 
        getInstance(Shutdown.class, "http-shutdown") 
    };
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
    
    // Now the injector gets created and during this process, the flag values
    // are read and used by the module bindings.
    injector = Guice.createInjector(allModules);

    // Get any shutdown hooks for the server:
    Thread shutdown = new Thread(getShutdownHook());
    shutdown.setName(getClass().getName() + ":shutdown");
    Runtime.getRuntime().addShutdownHook(shutdown);
    
    getInstance(HttpServer.class).start();
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
}
