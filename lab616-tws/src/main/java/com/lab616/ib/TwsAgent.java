// 2010 lab616.com, All Rights Reserved.

package com.lab616.ib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
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
import com.google.inject.util.Providers;
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
public class TwsAgent implements Runnable {

  static Logger logger = Logger.getLogger(TwsAgent.class);
  
  @Flag(name = "tws_ui_scripting", doc = "True to intercept UI events.")
  public static Boolean TWS_UI_SCRIPTING = null;

  @Flag(name = "tws_account_login", privacy = true, required = true, 
  		doc = "TWS login.")
  public static String TWS_ACCOUNT_LOGIN;
  
  @Flag(name = "tws_account_password", privacy = true, required = true,
  		doc = "TWS password.")
  public static String TWS_ACCOUNT_PASSWORD;

  @Flag(name = "tws_install_dir", required = false,
  		doc = "Directory where TWS is installed.")
  public static String TWS_INSTALL_DIR = "";

  @Flag(name = "tws_working_dir", required = true,
  		doc = "Working directory.")
  public static String TWS_WORKING_DIR = null;

  @Flag(name = "tws_by_classloader", required = false,
  		doc = "True to run by classloader.")
  public static Boolean RUN_BY_CLASSLOADER = false;
  
  @Flag(name = "tws_gateway", required = false,
  		doc = "True to run TWS Gateway Client instead of full Platform.")
  public static Boolean RUN_GATEWAY = true;

  static {
    Flags.register(TwsAgent.class);
  }

  public static class GuiceModule implements Module {

    @Override
    public void configure(Binder binder) {
      
      binder.bindConstant().annotatedWith(Names.named("tws_account"))
      .to(TWS_ACCOUNT_LOGIN);
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
  
  private final String account;
  private final AWTWindowEventReceiver<STATE> eventReceiver;
  private final File workingDir;
  private final File installDir;
  protected final JarClassLoader classLoader ;
  
  @Inject
  public TwsAgent(AWTWindowEventReceiver<STATE> eventReceiver, 
  		@Named("tws_account") String account,
      @Named("initial") STATE initial,
      @Named("tws_install_dir") String twsInstallDir,
      @Named("tws_working_dir") String twsWorkingDir) {
  	this.account = account;
    this.eventReceiver = eventReceiver;
    this.eventReceiver.setState(initial);
    this.classLoader = new JarClassLoader();
    
    // Create the directory if it doesn't exist.
    this.workingDir = new File(twsWorkingDir);
    if (!this.workingDir.exists()) {
      this.workingDir.mkdir();
      logger.info(getAccountName() + ": Created working dir=" + getWorkingDir());
    }

    if (twsInstallDir != null && twsInstallDir.length() > 0) {
    	File installDir = new File(twsInstallDir);
    	if (installDir.exists()) {
    		File[] jars = installDir.listFiles(new FilenameFilter() {
    			@Override
    			public boolean accept(File dir, String name) {
    				return name.endsWith(".jar");
    			}
    		});
    		for (File jar : jars) {
    			try {
    				this.classLoader.add(new FileInputStream(jar));
    				logger.info(getAccountName() + ": loading from " + jar);
    			} catch (IOException e) {
    				logger.warn(getAccountName() + ": Failed to load " + jar, e); 
    			}
    		}
        this.installDir = installDir;
        Thread.currentThread().setContextClassLoader(this.classLoader);
    	} else {
    		this.installDir = null;
    	}
    } else {
    	this.installDir = null;
    }
  }

  /**
   * Starts up the TWS client application.
   */
  @Override
  public void run() {
  	if (RUN_BY_CLASSLOADER) {
  		logger.info(getAccountName() + ": Running by classloader.");
  		runByClassLoader();
  	} else {
  		if (RUN_GATEWAY) {
  			ibgateway.GWClient.main(new String[] { getWorkingDir().getAbsolutePath() });
  		} else {
    		// Start IB TWS application in a separate thread.
        jclient.LoginFrame.main(new String[] { getWorkingDir().getAbsolutePath() });
  		}
  	}
  }

  protected void runByClassLoader() {
    try {
    	String className = (RUN_GATEWAY) ?
    			"ibgateway.GWClient" : "jclient.LoginFrame";
    	
    	Class<?> clientClz = this.classLoader.loadClass(className);

      Method method = clientClz.getMethod("main", String[].class);
      
      logger.info("Found client class: " + clientClz + ", method = " + method);
      method.invoke(null, new Object[] {new String[] {
      		getWorkingDir().getAbsolutePath()
      		}});
    } catch (ClassNotFoundException e) {
    	logger.fatal(getAccountName() + 
    			": IB TWS client class not found. Exiting.");
    } catch (NoSuchMethodException e) {
    	logger.fatal(getAccountName() + 
    			":IB TWS client class has no main(). Exiting.");
    	System.exit(-1);
    } catch (Exception e) {
    	logger.fatal(getAccountName() + 
			":IB TWS client exception = ", e);
    }
  }
  
  public String getAccountName() {
  	return this.account;
  }
  
  public File getInstallDir() {
  	return this.installDir;
  }
  
  public File getWorkingDir() {
  	return this.workingDir;
  }
  
  public static void main(String[] argv) throws Exception {
		final AtomicLong startCt = new AtomicLong();
  	
		Kernel kernel = new Kernel() {
			@Override
			public void run() throws Exception {
				startCt.set(Time.now());
				// Starts the main awt app.
				TwsAgent agent = getInstance(TwsAgent.class);
				logger.info("Starting TwsAgent: account = " + agent.getAccountName());
				
				agent.run(); // This starts another thread.
				logger.info("Finishing TwsAgent: account = " + agent.getAccountName());
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
