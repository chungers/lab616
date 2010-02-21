package com.lab616.ib.scripting;

import org.apache.log4j.Logger;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.inject.Inject;
import com.lab616.common.scripting.ScriptException;
import com.lab616.common.scripting.ScriptObject;
import com.lab616.common.scripting.ScriptObject.ScriptModule;
import com.lab616.ib.api.TWSClient;
import com.lab616.ib.api.TWSClientManager;
import com.lab616.ib.api.TWSClientManager.Managed;
import com.lab616.ib.api.loggers.ManagedLogger;
import com.lab616.ib.api.loggers.TWSEventAvroLogger;
import com.lab616.ib.api.loggers.TWSEventCSVLogger;
import com.lab616.ib.api.loggers.TWSEventProtoLogger;
import com.lab616.omnibus.http.ServletScript;

/**
 * Module for managing event logs in various formats.
 * @author dchung
 */
@ServletScript(path = "/tws/lm")
@ScriptModule(name = "EventLogManagement",
doc = "Basic scripts for managing TWS client event logging.")
public class EventLogManagement extends ScriptObject {

  private static Logger logger = Logger.getLogger(EventLogManagement.class);
  private final TWSClientManager clientManager;

  @Inject
  public EventLogManagement(TWSClientManager clientManager) {
    this.clientManager = clientManager;
  }

  /**
   * Log the events associated with profile/client id in the given directory.
   * @param profile The profile.
   * @param clientId The client id.
   * @param directory The directory.
   */
  @ServletScript(path = "logcsv")
  @Script(name = "logCSV",
  doc = "Log events from the client of profile,clientId to CSV file.")
  public ManagedLogger logCSV(
    @Parameter(name="profile", doc="Name of the client profile.")
    final String profile,
    @Parameter(name="clientId", doc="The client id.")
    final int clientId,
    @Parameter(name="dir", doc="The directory where the data file will be written")
    final String directory) {
    return log(Format.CSV, profile, clientId, directory);
  }

  /**
   * Logs the event in a proto file for client at (profile, clientId).
   * @param profile The profile.
   * @param clientId The client id.
   * @param directory The directory.
   */
  @ServletScript(path = "logproto")
  @Script(name = "logProto",
  doc = "Logs the events from profile, clientId to proto file.")
  public ManagedLogger logProto(
    @Parameter(name="profile", doc="Name of the client profile.")
    final String profile,
    @Parameter(name="clientId", doc="The client id.")
    final int clientId,
    @Parameter(name="dir", doc="The directory where the data file will be written")
    final String directory) {
    return log(Format.PROTO, profile, clientId, directory);
  }

  /**
   * Logs the events in a Hadoop avro file, for the client @ (profile, id).
   * @param profile The profile.
   * @param clientId The client id.
   * @param directory The directory.
   */
  @ServletScript(path = "logavro")
  @Script(name = "logAvro",
  doc = "Logs the events from profile, clientId to Hadoop avro file.")
  public ManagedLogger logAvro(
    @Parameter(name="profile", doc="Name of the client profile.")
    final String profile,
    @Parameter(name="clientId", doc="The client id.")
    final int clientId,
    @Parameter(name="dir", doc="The directory where the data file will be written")
    final String directory) {
    return log(Format.AVRO, profile, clientId, directory);
  }

  private enum Format {

    CSV,
    PROTO,
    AVRO;
  }

  ManagedLogger log(final Format format, final String profile, final int clientId,
    final String directory) {
    logger.info(String.format(
      "START: logProto %s for %s@%d in directory %s",
      format, profile, clientId, directory));
    try {
      // Get the client.
      final TWSClient client = this.clientManager.getClient(profile, clientId);

      // Check to see if we already have a writer for this
      Managed managed = this.clientManager.findAssociatedComponent(
        profile, clientId,
        new Predicate<Managed>() {

          @Override
          public boolean apply(Managed m) {
            // Look for exact match for this client.
            switch (format) {
              case CSV:
                return m instanceof TWSEventCSVLogger
                  && ((TWSEventCSVLogger) m).getSourceId().equals(
                  client.getSourceId());
              case PROTO:
                return m instanceof TWSEventProtoLogger
                  && ((TWSEventProtoLogger) m).getSourceId().equals(
                  client.getSourceId());
              case AVRO:
                return m instanceof TWSEventAvroLogger
                  && ((TWSEventAvroLogger) m).getSourceId().equals(
                  client.getSourceId());
            }
            return false;
          }
        });
      if (managed != null) {
        // This is safe since we won't get here without the apply() being true.
        return (ManagedLogger) managed;
      }
      // If not, we submit work to the work queue and block on the future.
      switch (format) {
        case CSV:
          final TWSEventCSVLogger w1 = new TWSEventCSVLogger(directory,
            profile, client.getSourceId());
          this.clientManager.enqueue(profile, clientId,
            new Function<TWSClient, ManagedLogger>() {
            @Override
            public ManagedLogger apply(TWSClient tws) {
              tws.getEventEngine().add(w1);
              return w1;
            }
          });
          return w1;
        case PROTO:
          final TWSEventProtoLogger w2 = new TWSEventProtoLogger(directory,
            profile, client.getSourceId());
          this.clientManager.enqueue(profile, clientId,
            new Function<TWSClient, ManagedLogger>() {
            @Override
            public ManagedLogger apply(TWSClient tws) {
              tws.getEventEngine().add(w2);
              return w2;
            }
          });
          return w2;
        case AVRO:
          final TWSEventAvroLogger w3 = new TWSEventAvroLogger(directory,
            profile, client.getSourceId());
          this.clientManager.enqueue(profile, clientId,
            new Function<TWSClient, ManagedLogger>() {
            @Override
            public ManagedLogger apply(TWSClient tws) {
              tws.getEventEngine().add(w3);
              return w3;
            }
          });
          return w3;
      }
      return null; // Impossible to get here.
    } catch (Exception e) {
      throw new ScriptException(this, e,
        "Exception starting %s log for %s@%d in %s",
        format, profile, clientId, directory);
    }
  }
}
