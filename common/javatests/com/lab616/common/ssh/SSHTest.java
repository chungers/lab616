package com.lab616.common.ssh;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.junit.Test;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.LocalPortForwarder;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import junit.framework.TestCase;

/**
 * Tests for JSch
 * 
 * @author david
 */
public class SSHTest extends TestCase {

  public static void main(String[] argv) throws Exception {
    new SSHTest().testGanymedSSHPortForwarding();
  }
  
  public void testGanymedSSHPortForwarding() throws Exception {
    String hostname = "dev.lab616.com";
    String username = "lab616";

    File keyfile = new File("/Users/david/lab616/deploy/accounts/lab616.id_dsa");
    String keyfilePass = "";
    
    /* Create a connection instance */
    Connection conn = new Connection(hostname);
    conn.connect();

    /* Authenticate */
    boolean isAuthenticated = conn.authenticateWithPublicKey(
        username, keyfile, keyfilePass);

    if (isAuthenticated == false)
      throw new IOException("Authentication failed.");

    /* Example Port Forwarding: -L 8080:www.remotehost:80 (OpenSSH notation) */
    LocalPortForwarder lpf1 = conn.createLocalPortForwarder(
        7496, hostname, 7496);

    /* Now simply point your webbrowser to 127.0.0.1:8080 */
    /* (on the host where you execute this program)                         */

    /* ===== OK, now let's establish some remote port forwardings ===== */

    /* Example Port Forwarding: -R 127.0.0.1:8080:www.ganymed.ethz.ch:80 (OpenSSH notation)
     * 
     * Specifies that the port 127.0.0.1:8080 on the remote server is to be forwarded to the
     * given host and port on the local side.  This works by allocating a socket to listen to port
     * 8080 on the remote side (the ssh server), and whenever a connection is made to this port, the
     * connection is forwarded over the secure channel, and a connection is made to
     * www.ganymed.ethz.ch:80 by the Ganymed SSH-2 library.
     * 
     * (the above text is based partially on the OpenSSH man page)
     */

    /* You can create as many of them as you want */
    //conn.requestRemotePortForwarding("127.0.0.1", 9000, hostname, 7496);

    /* Now, on the ssh server, if you connect to 127.0.0.1:9000, then the connection is forwarded
     * through the secure tunnel to the library, which in turn will forward the connection
     * to www.ganymed.ethz.ch:80. */

    /* Sleep a bit... (30 seconds) */
    //Thread.sleep(30000);

    /* Stop accepting remote connections that are being forwarded to www.ganymed.ethz.ch:80 */
    //conn.cancelRemotePortForwarding(9000);

    /* Sleep a bit... (20 seconds) */
    Thread.sleep(20000 * 60);

    
    /* Stop accepting connections on 127.0.0.1:8080 that are being forwarded to www.ethz.ch:80 */
    lpf1.close();

    /* Close the connection */
    conn.close();
  }


  //@Test
  public void _testJSch() throws Exception {
    JSch jsch=new JSch();

    String host = "localhost";
    String user = "lab616";

    int localPort = 9001;
    String remoteHost = "dev.lab616.com";
    int remotePort = 7496;

    Session session=jsch.getSession(user, remoteHost, 22);

    Properties config = new Properties();
    config.put("StrictHostKeyChecking", "no");
    session.setConfig(config);

    session.setPassword("mhmu8241");
    session.connect();

    //Channel channel=session.openChannel("shell");
    //channel.connect();

    int assignedPort = session.setPortForwardingL(
        localPort, remoteHost, remotePort);

    System.out.println(String.format("localhost:%d -> %s:%d", 
        assignedPort, remoteHost, remotePort));

  }  
}