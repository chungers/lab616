// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.lab616.ib.TwsWindowHandlers.LoginHandler;
import com.lab616.ib.TwsWindowHandlers.STATE;
import com.lab616.ui.AWTWindowEventReceiver;

/**
 * Guice module for TWS application.
 *
 * @author david
 *
 */
public class TwsModule implements Module {

  public static TypeLiteral<AWTWindowEventReceiver<STATE>> TYPE =
    new TypeLiteral<AWTWindowEventReceiver<STATE>>() {};

  
  @Override
  public void configure(Binder binder) {
    binder.bind(TYPE).toProvider(
        new Provider<AWTWindowEventReceiver<STATE>>() {

          @Inject
          LoginHandler login;
          
          @SuppressWarnings("unchecked")
          @Override
          public AWTWindowEventReceiver<STATE> get() {
            return new AWTWindowEventReceiver<STATE>(
                login,
                TwsWindowHandlers.TIP_OF_THE_DAY_HANDLER,
                TwsWindowHandlers.LOGIN_FAILED_HANDLER,
                TwsWindowHandlers.NEWER_VERSION_NOTICE_HANDLER,
                TwsWindowHandlers.WELCOME_SCREEN_HANDLER,
                TwsWindowHandlers.MAIN_SCREEN_HANDLER,
                TwsWindowHandlers.API_CONFIGURATION_HANDLER,
                TwsWindowHandlers.ACCEPT_API_CONNECTION_HANDLER);
          };
        }).in(Scopes.SINGLETON);
  }
}
