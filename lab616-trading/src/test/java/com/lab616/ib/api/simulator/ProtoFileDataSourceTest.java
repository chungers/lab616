// 2009-2010 lab616.com, All Rights Reserved.
package com.lab616.ib.api.simulator;

import java.io.File;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import junit.framework.TestCase;

import com.lab616.ib.api.proto.TWSProto;
import com.lab616.ib.api.util.ProtoDataFile;

/**
 * Tests for the data source which loads a protofile.
 * @author david
 *
 */
public class ProtoFileDataSourceTest extends TestCase {

	static Logger logger = Logger.getLogger(ProtoFileDataSourceTest.class);
	
	private File getDataFile() throws Exception {
		String workingDir = System.getProperty("user.dir");
		String dataFile = workingDir + "/" + System.getProperty(
				"dataFilesDir", "testing/") + "/unittest.proto";
		return new File(dataFile);
	}
	
	public void testLoadFile() throws Exception {
		File pf = getDataFile();
		assertTrue("No data file to test: " + pf.getCanonicalPath(), pf.exists());

		// First read it and get record count.
		int actual = 0;
		for (TWSProto.Event event : ProtoDataFile.getReader(pf.getAbsolutePath()).readAll()) {
			assertTrue(event.isInitialized());
			actual++;
		}
		System.out.println("Actual read = " + actual); // 37787

		ProtoFileDataSource ds = new ProtoFileDataSource(pf.getAbsolutePath());
		
		// Create a queue that takes the output of the data source:
		BlockingQueue<TWSProto.Event> incoming = ds.createSink();
		
		// Start the data source in a separate thread.
		Thread th = new Thread(ds);
		th.start();
		
		assertTrue("Incoming should be empty before start() is called.", 
				incoming.isEmpty());
		
		// Now start reading
		ds.start();
		
		// Wait a little bit:
		Thread.sleep(2000L);
		
		assertFalse("Incoming should have events after start() is called.", 
				incoming.isEmpty());
		int count = 0;
		while (!incoming.isEmpty()) {
			TWSProto.Event event = incoming.take();
			assertTrue("Must be initialized: "  + event, event.isInitialized());
			if (count++ % 1000 == 0) {
				System.out.print(".");
				System.out.flush();
				logger.info("Read " + count);
			}
		}
		
		System.out.println(" Total read = " + count); // 37787
		assertTrue(count > 0);
		assertEquals(actual, count);
		assertEquals("Thread should stop.", Thread.State.TERMINATED, th.getState());
	}
}
