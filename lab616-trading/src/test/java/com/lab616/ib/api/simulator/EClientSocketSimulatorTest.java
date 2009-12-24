// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api.simulator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.ib.client.EClientSocket;
import com.ib.client.EWrapper;
import com.lab616.concurrent.QueueProcessor;
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
		Platform() {
			include(new TWSClientModule(), new TWSControllerModule());
		}
	}
	
  private EWrapper getEWrapper(final List<TWSProto.Event> output) {
    return (EWrapper)Proxy.newProxyInstance(
         EWrapper.class.getClassLoader(), 
         new Class[] { EWrapper.class }, 
         new InvocationHandler() {
           public Object invoke(Object proxy, Method m, Object[] args) 
             throws Throwable {
     				ApiBuilder builder = ApiMethods.get(m.getName());
    				output.add(builder.buildProto("sent", Time.now(), args));
    				return null;
           }
         });
   }

  /**
   * Tests the simulator's ability to recieve api events.
   * @throws Exception
   */
	public void testSimulator() throws Exception {
		Platform p = new Platform();
		p.runInThread(new String[] {});

		long maxWait = 5000L;		
		assertTrue(p.isRunning(maxWait) && !p.hasExceptions());

		EClientSocketFactory factory = p.getInstance(EClientSocketFactory.class,
				2000L);

		final AtomicInteger called = new AtomicInteger(0);
		final List<TWSProto.Event> eventsReceived = Lists.newArrayList();
		EClientSocket client = factory.create("simulate", 0, 
				(EWrapper)Proxy.newProxyInstance(
						EWrapper.class.getClassLoader(), 
						new Class[] { EWrapper.class }, 
						new InvocationHandler() {
							public Object invoke(Object proxy, Method m, Object[] args) 
							throws Throwable {
								called.incrementAndGet();
								eventsReceived.add(
										ApiMethods.get(m.getName()).buildProto(
												"received", Time.now(), args));
								return null;
							}
						}), true);

		assertFalse(client.isConnected());
		
		HostPort hp = new HostPort();
		client.eConnect(hp.host, hp.port, 0);
		assertTrue(client.isConnected());
		
		EClientSocketSimulator sim = EClientSocketSimulator.getSimulator("simulate", 0);
		assertNotNull(sim);

		// A special hack to use an EWrapper proxy to generate the protos easily.
		// In practice, the protos are read from a file of historical data.
		final List<TWSProto.Event> eventsToSend = Lists.newArrayList();
		EWrapper w = getEWrapper(eventsToSend);
		
		final int COUNT = 500;
		
		// Set up the data source
		final AtomicInteger sourceInvoked = new AtomicInteger(0);
		final AtomicInteger writeToSink = new AtomicInteger(0);
		DataSource ds = new DataSource("FromInputList") {
			@Override
			protected void source(BlockingQueue<TWSProto.Event> sink) throws Exception {
				
				assertTrue(sourceInvoked.get() < 2); 
				sourceInvoked.incrementAndGet();
				assertEquals(COUNT, eventsToSend.size());
				
				for (TWSProto.Event event : eventsToSend) {
					sink.put(event);
					writeToSink.incrementAndGet();
					assertTrue("eventsToSend=" + eventsToSend.size(), 
							eventsToSend.size() <= COUNT);
					assertTrue("writeToSink=" + writeToSink.get(),
							writeToSink.get() <= COUNT);
				}
		  }
		};
		sim.addDataSource(ds);
		
		// Maven's test framework seems to run multiple tests and we have problems
		// with port bindings for http.
		assertTrue(p.isRunning(maxWait) && !p.hasExceptions());

		// Generate the data -- the eventsToSend list gets populated.
		for (int i = 0; i < COUNT; i++) {
			w.tickPrice(1000, 3, i, 1);
		}
		
		assertEquals(COUNT, eventsToSend.size());
		assertEquals(0, eventsReceived.size());
		
		ds.start();
		
		// Wait for everything to drain.
		Thread.sleep(5000L);
		
		assertEquals(1, sourceInvoked.get());
		assertEquals(writeToSink.get(), eventsToSend.size());
		
		// Should have run only one task.
		assertEquals(1, QueueProcessor.queueProcessed.get("simulate").get());
		
		// Makes sure that the client receives the same data.
		assertEquals(eventsToSend.size(), sim.getEWrapperInvokes());
		assertEquals(eventsToSend.size(), called.get());
		assertEquals(called.get(), eventsReceived.size());
		assertEquals(eventsToSend.size(), eventsReceived.size());
		
		for (int i = 0; i < eventsToSend.size(); i++) {
			assertEquals(eventsToSend.get(i).getMethod(), eventsReceived.get(i).getMethod());
			assertTrue(eventsReceived.get(i).isInitialized());
			assertTrue(checkSame(eventsToSend.get(i).getFieldList(), 
					eventsReceived.get(i).getFieldList()));
		}
		
		p.shutdown();
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
