// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

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
 * Basic container for the IB TWS application.  This is the host/ launcher
 * for the TWS application and it intercepts some UI events in order to automate
 * the application suitable for api connections.
 *
 * @author david
 *
 */
public class TwsContainer {

  static Logger logger = Logger.getLogger(TwsContainer.class);
  
  public static class GuiceModule implements Module {

    @Flag(name = "login", privacy = true)
    public static String LOGIN_USER;
    
    @Flag(name = "password", privacy = true)
    public static String PASSWORD;

    @Flag(name = "tws_dir")
    public static String TWS_DIR;

    static {
      Flags.register(GuiceModule.class);
    }

    //@Override //JDK1.5
    public void configure(Binder binder) {
      
      binder.bindConstant().annotatedWith(Names.named("username"))
        .to(LOGIN_USER);
      binder.bindConstant().annotatedWith(Names.named("password"))
        .to(PASSWORD);
      binder.bindConstant().annotatedWith(Names.named("tws-dir"))
      .to(System.getProperty("user.home") + "/" + TWS_DIR);
      binder.bindConstant().annotatedWith(Names.named("initial"))
      .to(STATE.START);
      binder.bind(TwsContainer.class).in(Scopes.SINGLETON);
    }    
  }
  
  private final AWTWindowEventReceiver<STATE> eventReceiver;
  
  @Inject
  public TwsContainer(AWTWindowEventReceiver<STATE> eventReceiver, 
      @Named("initial") STATE initial,
      @Named("tws-dir") String twsDir) {
    this.eventReceiver = eventReceiver;
    this.eventReceiver.setState(initial);

    // Start IB TWS application in a separate thread.
    jclient.LoginFrame.main(new String[] { twsDir });
  }

  
  public static void main(String[] argv) throws Exception {
		final AtomicLong startCt = new AtomicLong();
  	
		Kernel kernel = new Kernel() {
			@Override
			public void run() throws Exception {
				startCt.set(Time.now());
				// Starts the main awt app.
				getInstance(TwsContainer.class);
			}
		};

		Kernel.Shutdown<String> shutdown = 
			new Kernel.Shutdown<String>() {
			public String call() throws Exception {
				return String.format("Elapsed %d microseconds.", 
						Time.now() - startCt.get());
			}
			public String getName() {
				return "TwsContainer/main";
			}
		};
		
		kernel
			.include(new GuiceModule(), new TwsModule())
			.include(shutdown)
			.run(argv);
  }
}
