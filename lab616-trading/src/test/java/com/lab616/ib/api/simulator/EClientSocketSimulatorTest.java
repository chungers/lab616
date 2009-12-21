// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api.simulator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Module;
import com.ib.client.EClientSocket;
import com.ib.client.EWrapper;
import com.lab616.ib.api.ApiBuilder;
import com.lab616.ib.api.ApiMethods;
import com.lab616.ib.api.EClientSocketFactory;
import com.lab616.ib.api.TWSClientModule;
import com.lab616.ib.api.TWSConnectionProfileManager.HostPort;
import com.lab616.ib.api.proto.TWSProto;
import com.lab616.ib.api.proto.TWSProto.Field;
import com.lab616.ib.api.servlets.TWSControllerModule;
import com.lab616.omnibus.Kernel;
import com.lab616.util.Time;

/**
 * @author david
 *
 */
public class EClientSocketSimulatorTest extends TestCase {

	static Logger logger = Logger.getLogger(EClientSocketSimulatorTest.class);
	
	static class Platform extends Kernel {
		@Override
		public Set<? extends Module> getModules() {
			return Sets.newHashSet(new TWSClientModule(), new TWSControllerModule());
		}
		
		boolean initialized = false;
		@Override
		public void run() {
			initialized = true;
			
			Kernel k = getInstance(Kernel.class);
			assertEquals(this, k);
		}
	}
	
	interface Func {
		public Object call(Object proxy, Method m, Object[] args);
	}
	
  private EWrapper getEWrapper(final Func func) {
    return (EWrapper)Proxy.newProxyInstance(
         EWrapper.class.getClassLoader(), 
         new Class[] { EWrapper.class }, 
         new InvocationHandler() {
           public Object invoke(Object proxy, Method m, Object[] args) 
             throws Throwable {
          	 return func.call(proxy, m, args);
           }
         });
   }

  private EWrapper protoBuilder(final List<TWSProto.Event> output) {
  	return getEWrapper(new Func() {
			public Object call(Object proxy, Method m, Object[] args) {
				ApiBuilder builder = ApiMethods.get(m.getName());
				output.add(builder.buildProto("test", Time.now(), args));
				return null;
			}
		});
  }
  
	public void testSimulator() throws Exception {
		Platform p = new Platform();
		p.run(new String[] {});
		
		while (!p.isRunning()) {
			Thread.sleep(10L);
		}
		
		EClientSocketFactory factory = p.getInstance(EClientSocketFactory.class);
		
		final List<TWSProto.Event> eventsReceived = Lists.newArrayList();
		EClientSocket client = factory.create("simulate", getEWrapper(new Func() {
			public Object call(Object proxy, Method m, Object[] args) {
				eventsReceived.add(
						ApiMethods.get(m.getName()).buildProto("received", Time.now(), args));
				return null;
			}
		}), true);

		assertFalse(client.isConnected());
		
		HostPort hp = new HostPort();
		client.eConnect(hp.host, hp.port, 0);
		assertTrue(client.isConnected());
		
		EClientSocketSimulator sim = EClientSocketSimulator.getSimulator("simulate");
		assertNotNull(sim);

		// A special hack to use an EWrapper proxy to generate the protos easily.
		// In practice, the protos are read from a file of historical data.
		final List<TWSProto.Event> eventsToSend = Lists.newArrayList();
		EWrapper w = protoBuilder(eventsToSend);
		
		// Set up the data source
		DataSource ds = new DataSource() {
			@Override
			void source(BlockingQueue<TWSProto.Event> sink) throws Exception {
				for (TWSProto.Event event : eventsToSend) {
					sink.put(event);
					Thread.sleep(5L);
				}
		  }
		};

		sim.addDataSource(ds);
		
		// Generate the data -- the eventsToSend list gets populated.
		for (int i = 0; i < 500; i++) {
			w.tickPrice(1000, 3, i, 1);
		}
		
		ds.run();
		
		// Wait for everything to drain.
		while (!sim.isEventQueueEmpty()) {
			Thread.sleep(10L);
		}
		
		// Makes sure that the client receives the same data.
		assertEquals(eventsToSend.size(), eventsReceived.size());
		
		for (int i = 0; i < eventsToSend.size(); i++) {
			assertEquals(eventsToSend.get(i).getMethod(), eventsReceived.get(i).getMethod());
			assertTrue(eventsReceived.get(i).isInitialized());
			assertTrue(checkSame(eventsToSend.get(i).getFieldList(), 
					eventsReceived.get(i).getFieldList()));
		}
	}
	
	private boolean checkSame(List<Field> a, List<Field> b) {
		if (a.size() != b.size()) {
			return false;
		}
		for (int i = 0; i < a.size(); i++) {
			assertTrue(a.get(i).isInitialized());
			assertTrue(b.get(i).isInitialized());
			if (!a.get(i).toByteString().equals(b.get(i).toByteString())) {
				logger.warn("Not same: " + a.get(i) + " vs. " + b.get(i));
				return false;
			}
		}
		return true;
	}
}
