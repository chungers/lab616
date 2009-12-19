// 2009 lab616.com, All Rights Reserved.

package com.lab616.trading.platform;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.Sets;
import com.google.inject.Module;
import com.lab616.ib.api.TWSClientModule;
import com.lab616.ib.api.servlets.TWSControllerModule;
import com.lab616.omnibus.Kernel;
import com.lab616.omnibus.event.EventEngine;

/**
 * @author david
 *
 */
public class Platform extends Kernel {


  static Logger logger = Logger.getLogger(Platform.class);

  private final Set<Module> modules = Sets.newHashSet();
	private final Set<Shutdown<?>> shutdownHooks = Sets.newHashSet();
	
	public Platform() {
		// Default constructor with default modules:
		add(new TWSClientModule());
		add(new TWSControllerModule());
	}
	
	public Platform add(Module module) {
		modules.add(module);
		return this;
	}
	
	public Platform add(Shutdown<?> shutdown) {
		shutdownHooks.add(shutdown);
		return this;
	}
	
	@Override
  protected void addShutdown(List<Shutdown<?>> list) {
		for (Shutdown<?> s : shutdownHooks) {
			list.add(s);
		}
  }

	@Override
  public Set<? extends Module> getModules() {
		return modules;
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
		logger.info("Event engine in state = " + engine.getState());
		
	}
}
