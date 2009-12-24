// 2009 lab616.com, All Rights Reserved.

package com.lab616.trading.platform;

import org.apache.log4j.Logger;

import com.lab616.ib.api.TWSClientModule;
import com.lab616.ib.api.servlets.TWSControllerModule;
import com.lab616.ib.commands.CommandModule;
import com.lab616.omnibus.Kernel;
import com.lab616.omnibus.event.EventEngine;

/**
 * Trivial platform class by extending the Kernel to include additional
 * modules.
 * 
 * @author david
 *
 */
public class Platform extends Kernel {

	static Logger logger = Logger.getLogger(Platform.class);

	public Platform() {
		// Default constructor with default modules:
		include(new TWSClientModule());
		include(new TWSControllerModule());
		include(new CommandModule());
	}

	@Override
	protected void run() throws Exception {
		logger.info("Kernel started.");
	}

	public static void main(String[] args) throws Exception {
		// Sample usage
		Platform k = new Platform();

		// Add shutdown hooks and modules and start the basic services running:
		//k.add(shutdown).add(module).run(args);

		// Get instance of services
		EventEngine engine = k.getInstance(EventEngine.class);

		// Add new event watchers, etc.
		logger.info("Event engine in state = " + engine.running());

	}
}
