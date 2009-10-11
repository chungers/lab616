// 2009 lab616.com, All Rights Reserved.

package com.lab616.trading.platform;

import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import com.lab616.ib.api.TWSClientModule;
import com.lab616.ib.api.servlets.TWSControllerModule;
import com.lab616.omnibus.Main;
import com.lab616.omnibus.event.EventEngine;

/**
 * Main for the trading platform that hosts a set of trader agents.
 *
 * @author david
 *
 */
public class PlatformMain extends Main {

  static Logger logger = Logger.getLogger(PlatformMain.class);

  @Override
  public Set<? extends Module> getModules() {
    return ImmutableSet.of( 
        new TWSClientModule(), 
        new TWSControllerModule());
  }

  @Override
  public Shutdown<?> getShutdown() {
    return getInstance(Shutdown.class, "ib-api-shutdown");
  }

  @Override
  public void run() throws Exception {
    logger.info("Trading platform started.");
    
    // Some configurations
    EventEngine engine = getInstance(EventEngine.class);
    
    // Get various agents and start them.
  }

  public static void main(String[] args) throws Exception {
    PlatformMain main = new PlatformMain();
    main.run(args);
  }
}
