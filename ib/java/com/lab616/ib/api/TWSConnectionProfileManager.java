// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api;

import java.util.Map;

import org.apache.log4j.Logger;

import com.lab616.monitoring.Varz;
import com.lab616.monitoring.VarzMap;
import com.lab616.monitoring.Varzs;

/**
 *
 *
 * @author david
 *
 */
public class TWSConnectionProfileManager {

  @Varz(name="tws-connection-profiles")
  public static Map<String, HostPort> profiles = 
    VarzMap.create(HostPort.class);
  
  static Logger logger = Logger.getLogger(TWSConnectionProfileManager.class);
  
  public static class HostPort {
    public String host;
    public Integer port;
    
    public HostPort() { // Default constructor.
    }
    
    public HostPort(String host, Integer port) {
      this.host = host; this.port = port;
    }

    public void set(HostPort hostPort) {
      this.host = hostPort.host; this.port = hostPort.port;
    }
    
    public String toString() {
      return String.format("%s:%d", host, port);
    }
  }
  
  static {
    Varzs.export(TWSConnectionProfileManager.class);
  }
  
  public HostPort getHostPort(String profile) {
    return profiles.get(profile);
  }
  
  public void addProfile(String profile, HostPort hostPort) {
    profiles.get(profile).set(hostPort);
    logger.info("Added " + hostPort + ", allProfiles = " + profiles);
  }
}
