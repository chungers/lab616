// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api;

import org.apache.log4j.Logger;

import com.lab616.ib.api.servlets.TWSControllerModule;
import com.lab616.omnibus.Kernel;

/**
 * Test main
 *
 * @author david
 *
 */
public class ApiClientMain extends Kernel {

  static Logger logger = Logger.getLogger(ApiClientMain.class);

  public ApiClientMain() {
  	include(new TWSClientModule(), 
        new TWSControllerModule());
  	includeShutdownNamed(TWSClientModule.SHUTDOWN_HOOK);
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
