/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lab616.ib.scripting;

import com.ib.client.EWrapper;
import com.lab616.ib.api.TWSClientModule;
import com.lab616.ib.api.loggers.ManagedLogger;
import com.lab616.ib.api.loggers.TWSEventProtoLogger;
import com.lab616.ib.api.proto.TWSProto;
import com.lab616.ib.api.servlets.TWSControllerModule;
import com.lab616.ib.api.util.ProtoDataFile;
import com.lab616.ib.scripting.PlaybackData.Playback.Source;
import com.lab616.ib.scripting.PlaybackData.PlaybackSession;
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
 * Test case combining data loading from exiting proto log file
 * and writing it out as a new event log.
 * 
 * @author dchung
 */
public class EventLogManagementTest extends TestCase {

  static {
    TWSEventProtoLogger.APPEND_FILE = false;
  }
  static Logger logger = Logger.getLogger(EventLogManagementTest.class);
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

  /**
   * Tests event logging with simulated api calls.
   * @throws Exception
   */
  public void testEWrapperCallsAndEventLogging() throws Exception {

    String profile = "simulate";

    logger.info("Checking on the kernel");
    assertTrue(k.isRunning(1000L));

    // Create a simulator session
    // Get the playback
    logger.info("Loading playback script.");
    PlaybackData pb = k.getScript("PlaybackData", 2000L).asInstanceOf(PlaybackData.class);

    // Load the data with 1 msec latency between ticks.
    PlaybackSession<EWrapper> dataPlayback = pb.newSession(profile).withEWrapper("api");

    // Now start a proto logger
    logger.info("Getting log management script.");

    EventLogManagement em = k.getScript("EventLogManagement", 5000L).asInstanceOf(EventLogManagement.class);

    logger.info("Starting proto log.");

    ManagedLogger<File> eventLogger = em.logProto(profile, 0, "/tmp");
    File logFile = eventLogger.getResource();

    // Wait till the logger is ready.
    assertTrue(eventLogger.isReady(2000L));
    assertNotNull(logFile);  // Writing not started yet.

    // Since proto files always APPEND, if the file already exists, read it
    // and get a record count.
    int initial = 0;
    if (TWSEventProtoLogger.APPEND_FILE) {
      for (TWSProto.Event event : ProtoDataFile.getReader(logFile).readAll()) {
        assertTrue(event.isInitialized());
        initial++;
      }
    }

    logger.info("Start loading data.");
    EWrapper ewrapper = dataPlayback.start();

    int COUNT = 500;
    for (int i = 0; i < COUNT; i++) {
      ewrapper.tickPrice(1000, 0, 45., 0);
    }

    logger.info("Going into wait loop.");
    // Now we need to block for a while until the data loading is complete.
    do {
      logger.info(">>>> Queue = " + pb.getQueueDepth(profile, 0));
      Thread.sleep(2000L);
    } while (!pb.isQueueEmpty(profile, 0));

    logger.info("Stopping the logger.");
    eventLogger.halt();

    // Now read back the logFile.
    assertTrue(logFile.exists());

    logger.info("Reading back the log file.");
    int actual = 0;
    for (TWSProto.Event event : ProtoDataFile.getReader(logFile).readAll()) {
      assertTrue(event.isInitialized());
      actual++;
    }
    logger.info("Actual read = " + actual);
    assertEquals("Events read and logged must match.", COUNT, actual - initial);
  }

  /**
   * Tests reading from file and logging to file.
   * @throws Exception
   */
  public void testPlaybackAndEventLogging() throws Exception {

    String profile = "simulate2";

    // First read it and get record count.
    File pf = getDataFile();
    int actual = 0;
    for (TWSProto.Event event : ProtoDataFile.getReader(pf).readAll()) {
      assertTrue(event.isInitialized());
      actual++;
    }
    logger.info("Actual read = " + actual); // 37787

    logger.info("Checking on the kernel");
    assertTrue(k.isRunning(1000L));

    // Create a simulator session
    // Get the playback
    logger.info("Loading playback script.");
    PlaybackData pb = k.getScript("PlaybackData", 2000L).asInstanceOf(PlaybackData.class);

    // Load the data with 1 msec latency between ticks.
    PlaybackSession<Source> dataPlayback = pb.newSession(profile).withProtoFile(
      pf.getAbsolutePath(), 1L);

    logger.info("Adding a listener to check the number of events.");
    final AtomicInteger count1 = new AtomicInteger(0);
    Stoppable statement = k.getInstance(EventEngine.class, 1000L).add(
      "select * from EventMessage",
      new EventSubscriber<EventMessage>() {

        @Override
        public void update(EventMessage t) {
          count1.incrementAndGet();
          if (count1.get() % 5000 == 0) {
            logger.info("count = " + count1.get() + ", source=" + t.getSource());
          }
        }
      });

    // Make sure simulate2, 0 is ready

    // Now start a proto logger
    logger.info("Getting log management script.");

    EventLogManagement em = k.getScript("EventLogManagement", 1000L).asInstanceOf(EventLogManagement.class);

    logger.info("Starting proto log.");
    ManagedLogger<File> eventLogger = em.logProto(profile, 0, "/tmp");
    File logFile = eventLogger.getResource();

    // Block until logger is ready.
    assertTrue(eventLogger.isReady(2000L));
    assertNotNull(logFile);  // Writing not started yet.

    // Since proto files always APPEND, if the file already exists, read it
    // and get a record count.
    int initial = 0;
    if (TWSEventProtoLogger.APPEND_FILE) {
      for (TWSProto.Event event : ProtoDataFile.getReader(logFile).readAll()) {
        assertTrue(event.isInitialized());
        initial++;
      }
    }

    logger.info("Start loading data.");
    Source src = dataPlayback.start();

    logger.info("Going into wait loop.");
    // Now we need to block for a while until the data loading is complete.
    do {
      logger.info("Queue = " + pb.getQueueDepth(profile, 0));
      Thread.sleep(2000L);
    } while (!pb.isQueueEmpty(profile, 0) || !src.finished());

    // Not normally exposed but required for test case.
    while (!((TWSEventProtoLogger)eventLogger).isQueueEmpty()) {
      Thread.sleep(2L);
    }

    // Now read back the logFile.
    assertTrue(logFile.exists());

    logger.info("Reading back the log file.");
    int actual2 = 0;
    for (TWSProto.Event event : ProtoDataFile.getReader(logFile).readAll()) {
      assertTrue(event.isInitialized());
      actual2++;
    }
    logger.info("Actual2 read = " + actual2); // 37787
    assertEquals("Sanity check of events detected.", actual, count1.get());
    assertEquals("Events read and logged must match.", actual, actual2 - initial);

    logger.info("Stopping the logger.");
    eventLogger.halt();
  }
}
