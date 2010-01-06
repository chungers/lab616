/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lab616.ib.scripting;

import com.lab616.common.scripting.ScriptException;
import com.lab616.ib.api.TWSClientModule;
import com.lab616.ib.api.servlets.TWSControllerModule;
import com.lab616.omnibus.Kernel;
import junit.framework.TestCase;

/**
 *
 * @author dchung
 */
public class ConnectionManagementTest extends TestCase {

  private static Kernel k;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    if (k == null) {
      k = new Kernel(null,
        new TWSClientModule(),
        new TWSControllerModule(),
        new ScriptingModule()).runInThread(new String[]{});

      while (!k.isRunning(5000L)) {
        // isRunning blocks automatically.
      }
    }
  }

  public void testStartConnections() throws Exception {
    ConnectionManagement cm = k.getScript("ConnectionManagement")
      .asInstanceOf(ConnectionManagement.class);

    // First start a new profile
    String profile = "test";
    cm.newProfile(profile, "localhost", 1234);

    // Start a connection with a bad profile name.
    Exception ex = null;
    try {
      cm.newConnection("bad", 1000L);
    } catch (Exception e) {
      ex = e;
    }
    assertNotNull(ex);
    assertTrue(ex instanceof ScriptException);
    assertTrue(((ScriptException) ex).getScriptObject() == cm);

    // Now start the connection using the known profile
    int clientId = -1;
    try {
      clientId = cm.newConnection(profile, 1000L);
    } catch (ScriptException e) {
      // We expect a timeout.
      ex = e;
    }
    assertNotNull("Expects a timeout exception.", ex);
  }
}
