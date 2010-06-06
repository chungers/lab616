// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.lab616.ui.UIControl;
import com.lab616.ui.UIHandler;

/**
 * Handlers for various TWS window events.
 *
 * @author david
 *
 */
public class TwsWindowHandlers {

  /**
   * States of the TWS application on launch and sign-on, etc.
   */
  public static enum STATE {
    START,
    STOP,
    LOGIN,
    LOGIN_FAILED,
    API_CONNECTED,
    RUNNING,
  }
  
  
  static UIControl mainScreen = null;
  
  static int LOGIN  = 0;
  static int PASSWD = 1;

  static Logger logger = Logger.getLogger(TwsWindowHandlers.class);

  
  public static class LoginHandler implements UIHandler<STATE> {

    @Inject @Named("username")
    private String username;
    
    @Inject @Named("password")
    private String password;

    @Override
    public STATE handleUI(UIControl control, STATE state) throws Exception {
      switch (state) {
        case START :
          control.getField(LOGIN).setValue(username);
          control.getField(PASSWD).setValue(password);
          control.getSubmit("Login", true).submit();
          logger.info("Logging in.");
          return STATE.LOGIN;
        case LOGIN_FAILED :
          control.getSubmit("Cancel").submit();
          logger.warn("Login failed.");
          return STATE.STOP;
        default :
          return STATE.STOP;
      }
    }

    @Override
    public boolean match(String name, UIControl control, STATE state) {
      return name.matches(".*Login");
    }
  }
  
  public static UIHandler<STATE> LOGIN_FAILED_HANDLER = new UIHandler<STATE>() {
  	@Override
  	public STATE handleUI(UIControl control, STATE state) throws Exception {
      control.getSubmit("Yes|YES|Ok|OK").submit();
      return STATE.LOGIN_FAILED;
    }

  	@Override
    public boolean match(String name, UIControl control, STATE state) {
      return control.hasMessage("Login failed", ".*failed.*");
    }
  };
  
  public static UIHandler<STATE> TIP_OF_THE_DAY_HANDLER = new UIHandler<STATE>() {

  	@Override
  	public STATE handleUI(UIControl control, STATE state) throws Exception {
      control.getSubmit("Close").submit();
      return STATE.LOGIN;
    }

  	@Override
    public boolean match(String name, UIControl control, STATE state) {
      return name.matches(".*Tip of the Day.*") ||
        control.hasMessage("Tip of the Day");
    }
  };
  
  public static UIHandler<STATE> NEWER_VERSION_NOTICE_HANDLER = 
    new UIHandler<STATE>() {
    @Override
  	public STATE handleUI(UIControl control, STATE state) throws Exception {
      logger.info("Declining newer version.");
      control.getSubmit("No").submit();
      return STATE.LOGIN;
    }

    @Override
    public boolean match(String name, UIControl control, STATE state) {
      return (name.matches(".*Trader Workstation.*")) &&
        control.hasMessage("Newer Version");
    }
  };
  
  public static UIHandler<STATE> ACCEPT_API_CONNECTION_HANDLER = 
    new UIHandler<STATE>() {
    @Override
    public STATE handleUI(UIControl control, STATE state) throws Exception {
      logger.info("Accepting incoming connection.");
      control.getSubmit("Yes|YES|Ok|OK").submit();
      return STATE.API_CONNECTED;
    }

    @Override
    public boolean match(String name, UIControl control, STATE state) {
      return (name.matches(".*Trader Workstation.*")) &&
        control.hasMessage("Accept incoming connection attempt");
    }
  };

  public static UIHandler<STATE> WELCOME_SCREEN_HANDLER = 
    new UIHandler<STATE>() {
    @Override
    public STATE handleUI(UIControl control, STATE state) throws Exception {
      logger.info("Disposing Welcome screen.");
      control.getWindow().dispose();
      return STATE.LOGIN;
    }

    @Override
    public boolean match(String name, UIControl control, STATE state) {
      return (name.matches("Welcome"));
    }
  };

  public static UIHandler<STATE> API_CONFIGURATION_HANDLER = 
    new UIHandler<STATE>() {
    @Override
    public STATE handleUI(UIControl control, STATE state) throws Exception {
      logger.info("Configuring API access.");
      // Not implemented.  Use configuration xml in darykq directory instead.
      return STATE.RUNNING;
    }

    @Override
    public boolean match(String name, UIControl control, STATE state) {
      return (name.matches(".*Trader Workstation Configuration.*"));
    }
  };

  public static UIHandler<STATE> MAIN_SCREEN_HANDLER = new UIHandler<STATE>() {
    @Override
    public STATE handleUI(UIControl control, STATE state) throws Exception {
      logger.info("Running.");
      
      // Save this for later use.
      if (state == STATE.LOGIN) {
        mainScreen = control;
      }
      
      UIControl.Window window = control.getWindow();
      if (window != null) {
        window.resize(720, 360);
      }

      // Open up the API settings configuration panel.
      //control.getSubmit("Configure>API>All API Settings...").submit();
        
      return STATE.RUNNING;
    }

    @Override
    public boolean match(String name, UIControl control, STATE state) {
      return (name.matches(".*Trader Workstation")) && state == STATE.LOGIN;
    }
  };
}
