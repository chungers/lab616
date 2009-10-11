// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api.servlets;

import com.lab616.omnibus.http.AbstractHttpServletModule;

/**
 * Guice module
 *
 * @author david
 *
 */
public class TWSControllerModule extends AbstractHttpServletModule {

  public void configure() {
    bind("/tws", TWSController.class);
  }
}
