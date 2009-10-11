// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api;

import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import com.lab616.ib.api.servlets.TWSControllerModule;
import com.lab616.omnibus.Main;

/**
 * Test main
 *
 * @author david
 *
 */
public class ApiClientMain extends Main {

  static Logger logger = Logger.getLogger(ApiClientMain.class);

  @Override
  public Set<? extends Module> getModules() {
    return ImmutableSet.of( 
        new TWSClientModule(), 
        new TWSControllerModule());
  }

  @Override
  public Shutdown<?> getShutdown() {
    return getInstance(Shutdown.class, "tws-shutdown");
  }

  @Override
  public void run() throws Exception {
    logger.info("Api client container started.");
  }

  public static void main(String[] args) throws Exception {
    ApiClientMain main = new ApiClientMain();
    main.run(args);
  }
}
