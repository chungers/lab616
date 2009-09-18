// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib;

import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.lab616.common.flags.Flag;
import com.lab616.common.flags.Flags;
import com.lab616.ib.TwsWindowHandlers.STATE;
import com.lab616.omnibus.Main;
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

    @Flag(name = "login", required = true, privacy = true)
    public static String LOGIN_USER;
    
    @Flag(name = "password", required = true, privacy = true)
    public static String PASSWORD;

    @Flag(name = "tws_dir", required = true)
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
      .to(TWS_DIR);
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
    (new Main() {

      long startCt;
      
      //@Override //JDK1.5
      public Module[] getModules() {
        return new Module[] {
            new GuiceModule(),
            new TwsModule()
        };
      }

      //@Override //JDK1.5
      public void run() throws Exception {
        startCt = Time.now();
        logger.info("CurrentTime in microsecond = " + startCt);
        getInstance(TwsContainer.class);
      }

      //@Override
      public Shutdown<String> getShutdown() {
        return new Shutdown<String>() {

          public String call() throws Exception {
            return String.format("Elapsed %d microseconds.", 
                Time.now() - startCt);
          }
          public String getName() {
            return "TwsContainer/main";
          }
        };
      }
      
    }).run(argv);
  }
}
