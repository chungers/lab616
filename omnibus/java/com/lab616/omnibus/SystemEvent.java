// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * Basic system related event.
 * 
 * @author david
 */
public final class SystemEvent {

	// Event name used in the engine / epl to identify system level events.
	public static final String EVENT_NAME = "obus";
	
	private String component;
	private String method;
	private Map<String, String> params = Maps.newHashMap();
	
	public String getComponent() {
  	return component;
  }
	
	public void setComponent(String component) {
  	this.component = component;
  }
	
	public String getMethod() {
  	return method;
  }
	
	public void setMethod(String method) {
  	this.method = method;
  }
	
	public Map<String, String> getParams() {
  	return params;
  }
	
	public void setParam(String p, String v) {
  	this.params.put(p, v);
  }
	
	public String toString() {
		return String.format("%s[%s:%s:%s]", EVENT_NAME, component, method, params);
	}
}
