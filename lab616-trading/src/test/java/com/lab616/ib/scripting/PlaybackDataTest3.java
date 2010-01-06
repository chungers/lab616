/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.lab616.ib.scripting;

import com.lab616.common.scripting.ScriptObjects;
import com.lab616.ib.api.TWSClientModule;
import com.lab616.ib.api.proto.TWSProto;
import com.lab616.ib.api.servlets.TWSControllerModule;
import com.lab616.ib.api.util.ProtoDataFile;
import com.lab616.ib.scripting.PlaybackData.Playback.Source;
import com.lab616.omnibus.Kernel;
import com.lab616.omnibus.event.EventEngine;
import com.lab616.omnibus.event.EventEngine.Stoppable;
import com.lab616.omnibus.event.EventMessage;
import com.lab616.omnibus.event.EventSubscriber;
import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;
import junit.framework.TestCase;
import org.apache.log4j.Logger;

/**
 *
 * @author dchung
 */
public class PlaybackDataTest3 extends TestCase {

  static Logger logger = Logger.getLogger(PlaybackDataTest3.class);
  
  private static Kernel k;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    if (k == null) {
      k = new Kernel(null,
        new TWSClientModule(),
        new TWSControllerModule(),
        new ScriptingModule()).runInThread(new String[]{});
    }
  }

	private File getDataFile() throws Exception {
		String workingDir = System.getProperty("user.dir");
		String dataFile = workingDir + "/" + System.getProperty(
				"dataFilesDir", "testing/") + "/unittest.proto";
		return new File(dataFile);
	}

  public void testPlayback() throws Exception {

		// First read it and get record count.
    File pf = getDataFile();
		int actual = 0;
		for (TWSProto.Event event : ProtoDataFile.getReader(pf.getAbsolutePath()).readAll()) {
			assertTrue(event.isInitialized());
			actual++;
		}
		System.out.println("Actual read = " + actual); // 37787

    final AtomicInteger count1 = new AtomicInteger(0);

    Stoppable statement = k.getInstance(EventEngine.class, 1000L).add(
      "select * from EventMessage",
      new EventSubscriber<EventMessage>() {

        @Override
        public void update(EventMessage t) {
          count1.incrementAndGet();
          if (count1.get() % 5000 == 0) {
            logger.info("count = " + count1.get());
          }
        }
      });

    // Create a simulator session
    // Get the playback
    PlaybackData pb = k.getInstance(ScriptObjects.class)
      .load("PlaybackData").asInstanceOf(PlaybackData.class);

    // Load the data with 1 msec latency between ticks.
    Source src = pb.newSession("simulate").withProtoFile(
      pf.getAbsolutePath(), 1L).start();

    long t1 = System.nanoTime();

    while (!pb.isQueueEmpty("simulate", 0) || !src.finished()) {
      logger.info("Queue = " + pb.getQueueDepth("simulate", 0));
      Thread.sleep(1500L);
    }

    long dt = System.nanoTime() - t1;
    logger.info("dt = " + dt + ", qps=" + 1e9/dt * count1.get());
    assertEquals(actual, count1.get());

    // Stop the statement.
    statement.halt();
  }
}
