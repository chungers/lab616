/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lab616.ib.scripting;

import com.google.inject.Inject;
import com.lab616.common.scripting.ScriptException;
import com.lab616.common.scripting.ScriptObject;
import com.lab616.common.scripting.ScriptObject.Script;
import com.lab616.common.scripting.ScriptObject.ScriptModule;
import com.lab616.ib.api.TWSClient;
import com.lab616.ib.api.TWSClientManager;
import org.apache.log4j.Logger;

/**
 *
 * @author dchung
 */
@ScriptModule(name = "ConnectionManagement",
doc = "Basic scripts for managing TWS client connections.")
public class ConnectionManagement extends ScriptObject {

  private static Logger logger = Logger.getLogger(ConnectionManagement.class);
  private final TWSClientManager clientManager;

  @Inject
  public ConnectionManagement(TWSClientManager clientManager) {
    this.clientManager = clientManager;
  }

  /**
   * Starts a new connection profile.
   * @param profile The name of the profile.
   * @param hostName Hostname.
   * @param port Port number.
   */
  @Script(name = "newProfile",
  doc = "Adds a new profile which maps a host:port to a profile name.")
  public void newProfile(String profile, String hostName, int port) {
    logger.info(String.format(
      "START: newProfile with profile=%s, hostName=%s, port=%d",
      profile, hostName, port));
    try {
      if (!this.clientManager.profileExists(profile)) {
        this.clientManager.addProfile(hostName, hostName, port);
      }
    } catch (Exception e) {
      throw new ScriptException(this, e,
        "Exception adding new profile %s @ (%s:%d)", profile, hostName, port);
    }
    logger.info(String.format(
      "OK: newProfile with profile=%s, hostName=%s, port=%d",
      profile, hostName, port));
  }

  /**
   * Starts a new connection by profile name.
   * @param profile The profile name.
   * @param timeoutMillis Timeout in milliseconds for the client to be ready.
   * @return The client id.
   */
  @Script(name = "newConnection",
  doc = "Adds a new connection with the given profile, returns the client id.")
  public int newConnection(String profile, long timeoutMillis) {
    if (!this.clientManager.profileExists(profile)) {
      throw new ScriptException(this, "Unkown profile: %s", profile);
    }

    long timeout = (timeoutMillis > 0) ? timeoutMillis : 100L; // Min 100 msec.
    long sleep = 50L;
    long waits = timeout / sleep; // Number of waits.
    TWSClient client = null;
    int clientId = -1;
    logger.info(String.format(
      "START: newConnection with profile=%s, timeoutMillis=%d, waits=%d",
      profile, timeout, waits));
    try {
      clientId = this.clientManager.newConnection(profile, false);
      do {
        client = this.clientManager.getClient(profile, clientId);
        if (client != null && client.isReady()) {
          break;
        }
        Thread.sleep(sleep);
      } while (waits-- > 0);
      // Now block for the account ready
      client.waitUntilReady(++waits * sleep);
    } catch (Exception e) {
      throw new ScriptException(this, e,
        "Exception while starting up client (%s@%d): %s",
        profile, clientId, e);
    }
    // On connection failure, the connection client will be removed
    // by the manager.
    client = this.clientManager.getClient(profile, clientId);
    if (waits == 0 && (client == null || !client.isReady())) {
      throw new ScriptException(this,
        "newConnection for (%s@%d) timed out after %s msec.",
        profile, clientId, timeout);
    }
    logger.info(String.format(
      "OK: newConnection with profile=%s, timeoutMillis=%d, waits=%d",
      profile, timeout, waits));
    return clientId;
  }

  /**
   * Destroys a client connection identified by the profile and client id.
   * @param profile The profile.
   * @param clientId The client id.
   */
  @Script(name = "destroyConnection",
  doc = "Destroys the client connection of given profile and client id.")
  public void destroyConnection(String profile, int clientId) {
    if (this.clientManager.getClient(profile, clientId) == null) {
      throw new ScriptException(this, 
        "No such client %s@%d", profile, clientId);
    }
    logger.info(String.format(
      "START: destroyConnection with profile=%s, clientId=%d",
      profile, clientId));
    boolean disconnected = false;
    try {
      disconnected = this.clientManager.stopConnection(profile, clientId);
    } catch (Exception e) {
      throw new ScriptException(this, e,
        "Exception while destroying client %s@%d", profile, clientId);
    }
    if (!disconnected) {
      throw new ScriptException(this,
        "Unable to disconnect client %s@%d", profile, clientId);
    }
    logger.info(String.format(
      "OK: destroyConnection with profile=%s, clientId=%d",
      profile, clientId));
  }
}
