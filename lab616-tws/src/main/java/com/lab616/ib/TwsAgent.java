// 2010 lab616.com, All Rights Reserved.

package com.lab616.ib;

import java.io.File;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;
import org.xeustechnologies.jcl.JarClassLoader;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.lab616.common.flags.Flag;
import com.lab616.common.flags.Flags;
import com.lab616.ib.TwsWindowHandlers.STATE;
import com.lab616.omnibus.Kernel;
import com.lab616.ui.AWTWindowEventReceiver;
import com.lab616.util.Time;

/**
 * Application for managing the Interactive Brokers TWS application.
 * This agent is responsible for starting up the TWS Java application,
 * as well as handling some simple UI scripting to facilitate automation.
 * 
 * @author david
 */
public class TwsAgent {

  static Logger logger = Logger.getLogger(TwsAgent.class);
  
  @Flag(name = "tws_ui_scripting", doc = "True to intercept UI events.")
  public static Boolean TWS_UI_SCRIPTING = null;

  @Flag(name = "tws_account_login", privacy = true, doc = "TWS login.")
  public static String TWS_ACCOUNT_LOGIN;
  
  @Flag(name = "tws_account_password", privacy = true, doc = "TWS password.")
  public static String TWS_ACCOUNT_PASSWORD;

  @Flag(name = "tws_install_dir", doc = "Directory where TWS is installed.")
  public static String TWS_INSTALL_DIR = null;

  @Flag(name = "tws_working_dir", doc = "Working directory.")
  public static String TWS_WORKING_DIR = null;

  static {
    Flags.register(TwsAgent.class);
  }

  public static class GuiceModule implements Module {

    @Override
    public void configure(Binder binder) {
      
      binder.bindConstant().annotatedWith(Names.named("username"))
        .to(TWS_ACCOUNT_LOGIN);
      binder.bindConstant().annotatedWith(Names.named("password"))
        .to(TWS_ACCOUNT_PASSWORD);
      binder.bindConstant().annotatedWith(Names.named("tws_install_dir"))
      	.to(TWS_INSTALL_DIR);
      binder.bindConstant().annotatedWith(Names.named("tws_working_dir"))
      	.to(TWS_WORKING_DIR);
      binder.bindConstant().annotatedWith(Names.named("initial"))
      	.to(STATE.START);
      binder.bind(TwsAgent.class).in(Scopes.SINGLETON);
    }    
  }
  
  private final AWTWindowEventReceiver<STATE> eventReceiver;
  
  @Inject
  public TwsAgent(AWTWindowEventReceiver<STATE> eventReceiver, 
      @Named("initial") STATE initial,
      @Named("tws_install_dir") String twsInstallDir,
      @Named("tws_working_dir") String twsWorkingDir) {
    this.eventReceiver = eventReceiver;
    this.eventReceiver.setState(initial);

    // Create the directory if it doesn't exist.
    File dir = new File(twsWorkingDir);
    if (!dir.exists()) {
      dir.mkdir();
    }
    
    JarClassLoader jcl = new JarClassLoader();
    jcl.add(TWS_INSTALL_DIR + "/");
    logger.info("Classloader for jars in dir=" + TWS_INSTALL_DIR);
    
    // Start IB TWS application in a separate thread.
    try {
      Class<?> clientClz = jcl.loadClass("jclient.LoginFrame");

      Method method = clientClz.getMethod("main", String[].class);
      
      logger.info("Found client class: " + clientClz + ", method = " + method);
      method.invoke(null, new Object[] {new String[] {twsWorkingDir}});

    } catch (ClassNotFoundException e) {
    	logger.fatal("IB TWS client class not found. Exiting.");
    	System.exit(-1);
    } catch (NoSuchMethodException e) {
    	logger.fatal("IB TWS client class has no main(). Exiting.");
    	System.exit(-1);
    } catch (Exception e) {
    	e.printStackTrace();
    	throw new RuntimeException(e);
    }
  }

  
  public static void main(String[] argv) throws Exception {
		final AtomicLong startCt = new AtomicLong();
  	
		Kernel kernel = new Kernel() {
			@Override
			public void run() throws Exception {
				startCt.set(Time.now());
				// Starts the main awt app.
				getInstance(TwsAgent.class);
			}
		};

		Kernel.Shutdown<String> shutdown = 
			new Kernel.Shutdown<String>() {
			public String call() throws Exception {
				return String.format("Elapsed %d microseconds.", 
						Time.now() - startCt.get());
			}
			public String getName() {
				return "TwsAgent/main";
			}
		};
		
		kernel.include(new GuiceModule(), new TwsModule());
		kernel.include(shutdown);
		kernel.run(argv);
  }
}
