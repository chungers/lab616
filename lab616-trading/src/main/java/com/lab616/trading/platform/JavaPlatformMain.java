// 2009 lab616.com, All Rights Reserved.

package com.lab616.trading.platform;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import com.lab616.ib.api.TWSClientManager;
import com.lab616.ib.api.TWSClientModule;
import com.lab616.ib.api.servlets.TWSControllerModule;
import com.lab616.omnibus.Kernel;
import com.lab616.omnibus.event.EventEngine;

/**
 * Main for the trading platform that hosts a set of trader agents.
 *
 * @author david
 *
 */
public class JavaPlatformMain extends Kernel {

  static Logger logger = Logger.getLogger(JavaPlatformMain.class);

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
    logger.info("Trading platform starting.");
    
    // Some configurations
    EventEngine engine = getInstance(EventEngine.class);
    logger.info("Loaded event engine: " + engine);
    
    TWSClientManager twsClientManager = getInstance(TWSClientManager.class);
    logger.info("Loaded TWS client manager: " + twsClientManager);
    
    // Get various agents and start them.
  }

  public static void main(String[] args) throws Exception {
    JavaPlatformMain main = new JavaPlatformMain();
    main.run(args);
  }
}
