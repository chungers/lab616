// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import com.google.inject.internal.Lists;
import com.lab616.ib.api.servlets.TWSControllerModule;
import com.lab616.omnibus.Kernel;
import com.lab616.omnibus.Kernel.Shutdown;

/**
 * Test main
 *
 * @author david
 *
 */
public class ApiClientMain extends Kernel {

  static Logger logger = Logger.getLogger(ApiClientMain.class);

  @Override
  public Set<? extends Module> getModules() {
    return ImmutableSet.of( 
        new TWSClientModule(), 
        new TWSControllerModule());
  }

  @Override
  public void addShutdown(List<Shutdown<?>> list) {
    list.add(getInstance(Shutdown.class, TWSClientModule.SHUTDOWN_HOOK));
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
