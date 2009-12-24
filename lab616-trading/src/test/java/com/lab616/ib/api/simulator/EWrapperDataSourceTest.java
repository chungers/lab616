// 2009-2010 lab616.com, All Rights Reserved.
package com.lab616.ib.api.simulator;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.ib.client.EWrapper;
import com.lab616.ib.api.proto.TWSProto;

import junit.framework.TestCase;

/**
 * @author david
 *
 */
public class EWrapperDataSourceTest extends TestCase {

	/**
	 * Tests sending and receiving data in separate threads.
	 * @throws Exception
	 */
	public void testSendingData() throws Exception {
		String sourceName = "test-source";
		EWrapperDataSource ds = new EWrapperDataSource(sourceName);
		EWrapper wrapper = ds.getEWrapper();
		
		// Call something on the wrapper other than the methods we care about:
		// Added since Scala REPL somehow calls equals().
		assertFalse(wrapper.equals(this));
		assertTrue(wrapper.hashCode() > 0);
		
		// Create a queue to collect the events.
		BlockingQueue<TWSProto.Event> sink = new LinkedBlockingQueue<TWSProto.Event>();
		// Bind the sink to the datasource to receive its events.
		ds.setSink(sink);
		
		// Start the data source in a thread
		Thread th = new Thread(ds);
		th.start();
		
		// Now we start sending events.
		int COUNT = 1000;
		ds.start();
		for (int i = 0; i < COUNT; i++) {
			wrapper.tickPrice(1000, 3, 10.0, 0);
			wrapper.tickSize(1000, 1, 10000);
		}
		// Finally we send the last error / not connected event.
		wrapper.error(1000, 504, "Not Connected.");
		
		// Wait a bit.
		do {
			Thread.sleep(1000L);
		} while (!ds.finished());
		
		assertEquals(Thread.State.TERMINATED, th.getState());

		// Total method calls.
		assertEquals(2 * COUNT + 1, sink.size());
		
		TWSProto.Event event = sink.peek();
		assertEquals(sourceName, event.getSource());
	}
}
