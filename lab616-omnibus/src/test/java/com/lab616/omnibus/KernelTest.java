package com.lab616.omnibus;

import junit.framework.TestCase;

import com.lab616.omnibus.event.EventEngine;

/**
 * @author david
 *
 */
public class KernelTest extends TestCase {

	static class Platform extends Kernel {
		
		private boolean initialized = false;
		@Override
		public void run() {
			initialized = true;
		}
	}

	public void testRunKernel() throws Exception {
		Platform p = new Platform();
		assertFalse(p.isRunning());
		
		p.run(new String[] {});
		
		while (!p.initialized) {
			Thread.sleep(10L);
		}
		
		EventEngine engine = p.getInstance(EventEngine.class);
		assertTrue(engine.running());
		
		// Stop everything
		assertTrue(p.shutdown());
		
		assertFalse(p.isRunning());
	}
}
