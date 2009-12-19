// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus;

import java.util.Map;

import com.google.common.collect.Maps;
import com.lab616.util.Time;

/**
 * Basic system related event.
 * 
 * @author david
 */
public final class SystemEvent {

	// Event name used in the engine / epl to identify system level events.
	public static final String EVENT_NAME = "SystemEvent";
	
	private final long id;
	private String component;
	private String method;
	private Map<String, String> params = Maps.newHashMap();
	
	public SystemEvent() {
		id = Time.now();
	}
	
	public SystemEvent(long id) {
		this.id = id;
	}
	
	public long getId() {
		return id;
	}
	
	public String getComponent() {
  	return component;
  }
	
	public SystemEvent setComponent(String component) {
  	this.component = component;
  	return this;
  }
	
	public String getMethod() {
  	return method;
  }
	
	public SystemEvent setMethod(String method) {
  	this.method = method;
  	return this;
  }
	
	public Map<String, String> getParams() {
  	return params;
  }
	
	public SystemEvent setParam(String p, String v) {
  	this.params.put(p, v);
  	return this;
  }
	
	public String getParam(String p) {
	  return this.params.get(p);
	}
	
	public String toString() {
		return String.format("%s[%s:%s:%s]", EVENT_NAME, component, method, params);
	}
}
