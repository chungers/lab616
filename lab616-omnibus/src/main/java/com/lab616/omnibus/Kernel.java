// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.google.inject.Binder;
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
import com.lab616.common.scripting.AbstractScriptingModule;
import com.lab616.common.scripting.ScriptObject;
import com.lab616.common.scripting.ScriptObjects;
import com.lab616.common.scripting.ScriptObject.Parameter;
import com.lab616.common.scripting.ScriptObject.Script;
import com.lab616.common.scripting.ScriptObject.ScriptModule;
import com.lab616.monitoring.Varz;
import com.lab616.monitoring.Varzs;
import com.lab616.omnibus.event.EventEngine;
import com.lab616.omnibus.event.EventModule;
import com.lab616.omnibus.http.HttpServer;
import com.lab616.omnibus.http.HttpServerModule;
import com.lab616.omnibus.http.ServletScript;

/**
 * Core class for an Omnibus container/ application.  It contains a set of
 * basic services such as the http server and event engine.
 *
 * @author david
 *
 */
public class Kernel {
	
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
   * Interface for startable component.
   */
  public interface Startable {
  	public void start() throws Exception;
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
  private AtomicBoolean running = new AtomicBoolean(false);
  private Throwable kernelException = null;
  
	private final Set<Module> modules = Sets.newHashSet();
	private final Set<Shutdown<?>> shutdownHooks = Sets.newHashSet();
	private final Set<String> shutdownHooksLoadLater = Sets.newHashSet();
	private final Set<Class<? extends Throwable>> startUpExceptionsToIgnore = Sets.newHashSet();
  private Runnable runnable = null;
  
  protected Kernel() {
  	// Subclass only.
  }
  
  public Kernel(Runnable runnable, Module... modules) {
  	this.runnable = runnable;
  	for (Module m : modules) {
  		include(m);
  	}
  }
  
  /**
   * Useful for testing, ignore the following list of exception types during
   * start up.
   * @param clz First exception type.
   * @param more Additional exception types.
   * @return This kernel.
   */
  public final Kernel ignoreStartupExceptions(Class<? extends Throwable> clz,
  		Class<? extends Throwable>... more) {
  	startUpExceptionsToIgnore.add(clz);
  	startUpExceptionsToIgnore.addAll(Lists.newArrayList(more));
  	return this;
  }
  
  /**
   * Include this Guice module in the kernel.  Must be called before
   * the {@link #run(String[])} method.
   * 
   * @param module The module to include.
   * @param more Additional optional modules.
   * @return The kernel instance.
   */
  public final Kernel include(Module module, Module... more) {
  	modules.add(module);
  	for (Module m : more) {
  		modules.add(m);
  	}
  	return this;
  }

  /**
   * Include a shutdown hook or additional hooks (in sequence of execution)
   * to this kernel.  Must be called before the {@link #run(String[])} method
   * is called.
   * 
   * @param shutdown The shutdown hook.
   * @param more Additional hooks.
   * @return The kernel instance.
   */
  public final Kernel include(Shutdown<?> shutdown, Shutdown<?>... more) {
  	shutdownHooks.add(shutdown);
  	for (Shutdown<?> s : more) {
  		shutdownHooks.add(s);
  	}
  	return this;
  }
  
  /**
   * Includes a Named shutdown hook that is already bound in one of the included
   * modules.
   * 
   * @param named The name bound.
   * @return The kernel instance.
   */
  public final Kernel includeShutdownNamed(String named) {
  	shutdownHooksLoadLater.add(named);
  	return this;
  }

  
  /**
   * Returns the set of modules loaded.
   * 
   * @return The set.
   */
  public final Set<? extends Module> getModules() {
    return modules;
  }

  /**
   * List in order of shutdown sequence.
   * 
   * @return The list.
   */
  public final List<Shutdown<?>> getShutdownRoutines() {
  	// System-wide services.
  	List<Shutdown<?>> list = Lists.newArrayList();
    for (Shutdown<?> s : shutdownHooks) {
    	list.add(s);
    }
    for (String named : shutdownHooksLoadLater) {
    	list.add(getInstance(Shutdown.class, named));
    }
    list.add(getInstance(Shutdown.class, EventModule.SHUTDOWN_HOOK));
    list.add(getInstance(Shutdown.class, HttpServerModule.SHUTDOWN_HOOK));
    return list;
  }

  private Runnable getShutdownHook() {
    return new Runnable() {
      public void run() {
      	// Check to see if the kernel is running at all.  The only time running
      	// can be set to false is when the shutdown() method is called 
      	// programmatically.  If that's the case, we don't want to run the
      	// shutdown again during the actual jvm shutdown.
      	if (running.get()) {
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
      }
    };
  }
  
  /**
   * Programmatically shuts everything down.  Stops the kernel from running.
   * 
   * @throws Exception
   */
  public final boolean shutdown() {
  	try {
    	getShutdownHook().run();
    	running.set(false);
    	return !running.get();
  	} catch (Throwable th) {
  		logger.warn("Exception during programmatic shutdown:", th);
  	}
  	return running.get();
  }
  
  /**
   * Returns whether the kernel is running or not, up to the optional wait
   * time specified.  After that, it returns whatever state the kernel is in.
   * 
   * @param wait Wait time in millis.
   * @return True if running properly.
   */
  public final boolean isRunning(long... wait) {
  	if (wait.length > 0 && wait[0] > 0) {
  		long waited = 0;
  		long waitIncrement = 1;
  		boolean loop = !running.get();
  		while (loop) {
  			try {
  				Thread.sleep(waitIncrement);
  				waited += waitIncrement;
  				loop = (waited >= wait[0]) ? false : running.get();
  			} catch (Exception e) {
  				return running.get();
  			}
  		}
  	}
  	return running.get();
  }
  
  /**
   * Returns whether there are any exceptions thrown during the startup or
   * running of the kernel.
   * 
   * @return True if there are exceptions.
   */
  public final boolean hasExceptions() {
  	return kernelException != null;
  }
  
  /**
   * Subclass overrides this method to start running the application. 
   * The implementation can call methods such as {@link #getInstance} to
   * obtain injected objects and start the program sequence.
   * 
   * @throws Exception
   */
  protected void run() throws Exception {
  	// Do nothing. Just hang out.
  	do {
  		Thread.sleep(100L);
  	} while (this.running.get());
  }
  
  /**
   * Runs the kernel in a dedicated thread.
   * @param args The args.
   * @throws Exception Exception when starting the thread, if any.
   */
  public final Kernel runInThread(final String... args) throws Exception {
  	Thread th = new Thread(new Runnable(){
  		@Override
  		public void run() {
  			try {
    			Kernel.this.run(args);
  			} catch (Exception e) {
  				throw new RuntimeException(e);
  			}
  		}
  	}, "Kernel-" + serverId);
  	th.start();
  	return this;
  }
  
  @ServletScript(path = "/log")
  @ScriptModule(name = "KernelLog", doc = "Kernel logger.")
  public static class KernelLog extends ScriptObject {
    @ServletScript(path = "info")
    @Script(name = "info", doc = "log one line.")
    public void info(
            @Parameter(name="message", doc="Writes one line to info.") 
            String message) {
        logger.info(message);
    }
  }
  
  /**
   * Starts the program with command line arguments.  This method is called
   * in the main method of an application.  This runs in the main thread of the
   * application.  The services will be in other threads.  If the {@link #run()}
   * method isn't overriden, the default behavior is to enter a run loop in a 
   * dedicated thread (which loops) until {@link #shutdown()} is invoked on a
   * reference of the kernel from the main application thread.
   * 
   * @param argv Command line arguments.
   * @throws Exception Uncaught exception.
   */
  public final void run(String[] argv) throws Exception {
  	if (this.running.get()) throw new IllegalStateException("Currently running.");
  	
  	this.running.set(true);
  	
    // Load all the modules.  This will also force any Flag registration
    // to occur so that the flag parsing will apply to all the module class
    // where the flag values are used for injection.
    List<Module> allModules = Lists.newArrayList();
    allModules.add(new AbstractScriptingModule() { @Override public void configure() {
      bind(KernelLog.class);
    }});
    allModules.add(new HttpServerModule());
    allModules.add(new EventModule());
    allModules.add(new Module() {
    	public void configure(Binder binder) {
    		binder.bind(Kernel.class).toInstance(Kernel.this);
    	}
    });
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
    Exception ex = null;
    Set<Class<? extends Startable>> comps = Sets.newHashSet();
    comps.add(HttpServer.class);
    comps.add(EventEngine.class);
    
    for (Class<? extends Startable> comp : comps) {
  		ex = startComponent(comp);
  		if (ex != null) {
  			shutdown();
  			return;
  		}
    }
    
    if (this.running.get()) {
    	try {
        // Run the main.
        if (this.runnable != null) {
        	this.runnable.run();
        } else {
          run();
        }
    	} catch (Exception e) {
    		logger.warn("Exception during kernel run().", e);
    		kernelException = e;
    	}
    }
  }

  private Exception startComponent(Class<? extends Startable> clz) {
    try {
  		getInstance(clz).start();
  		return null;
    } catch (Exception e) {
    	if (this.startUpExceptionsToIgnore.contains(e.getClass())) {
    		logger.info("Ignoring exception " + e);
    		return null;
    	} else {
      	logger.warn("Exception during startup of key components. Shutting down.", e);
      	kernelException = e;
      	return e;
    	}
    }
  }

  /**
   * It's possible that {@link #getInstance(Class)} methods are called from a
   * separate thread from the kernel's wait loop.  This means that the injector
   * can be uninitialized.  So this is a convenient method to wait until the
   * injector is ready.
   * 
   * @param wait Wait time in milliseconds.
   * @return The injector.
   */
  protected Injector getInjector(long... wait) {
  	if (wait.length > 0 && wait[0] > 0) {
  		while (injector == null) {
  			try {
  				Thread.sleep(wait[0] / 10 + 1);
  			} catch (Exception e) {
  				return injector;
  			}
  		}
  	}
  	return injector;
  }
  
  /**
   * Retrieves a reference to an object from the injector, waiting if the
   * injection framework isn't ready.
   * 
   * @param <T>  Type of object to get.
   * @param clz The class.
   * @param name Reference name in guice modules.
   * @param wait Optional wait time in millis.
   * @return The object reference.
   */
  public final <T> T getInstance(Class<T> clz, String name, long... wait) {
    return getInstance(Key.get(clz, Names.named(name)), wait);
  }

  /**
   * Retrieves a reference to an object from the injector, waiting if the
   * injection framework isn't ready.
   * 
   * @param <T>  Type of object to get.
   * @param clz The class.
   * @param wait Optional wait time in millis.
   * @return The object reference.
   */
  public final <T> T getInstance(Class<T> clz, long... wait) {
  	Injector i = getInjector(wait);
  	return (i != null) ? i.getInstance(clz) : null;
  }
  
  /**
   * Retrieves a reference to an object from the injector, waiting if the
   * injection framework isn't ready.
   * 
   * @param <T>  Type of object to get.
   * @param key The Guicke key.
   * @param wait Optional wait time in millis.
   * @return The object reference.
   */
  public final <T> T getInstance(Key<T> key, long... wait) {
  	Injector i = getInjector(wait);
    return (i != null) ? i.getInstance(key) : null;
  }

  /**
   * Loads a script object.
   * @param name The name of the script or the module name.
   * @param wait Optional wait.
   * @return The script object.
   */
  public final ScriptObject getScript(String name, long... wait) {
    return getInstance(ScriptObjects.class, wait).load(name);
  }

  /**
   * Loads a script object and casts to the appropriate type.
   * @param <T> The type
   * @param name Script name or module name.
   * @param t The type of the script.
   * @return The instance.
   */
  public final <T extends ScriptObject> T getScript(String name, Class<T> t) {
    return getScript(name).asInstanceOf(t);
  }

  /**
   * Simple main for testing, with only the platform components running.
   * 
   * @param argv Command line args
   * @throws Exception
   */
	public static void main(String[] argv) throws Exception {
		// Trivial example of running a bare kernel in a separate thread.
  	Kernel main = new Kernel().runInThread(argv);
  	System.out.println(">>>> Kernel is running now: " + main.isRunning());
  }
}
