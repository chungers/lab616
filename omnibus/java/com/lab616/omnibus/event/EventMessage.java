// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.event;

/**
 * Event within the system.
 */
public final class EventMessage<T> {

	public static final String EVENT_NAME = "EventMessage";
	
	public final String source;
	public final String destination;
	public final T payload;
	
	public EventMessage(String fromString, String toString, T payloadObj) {
		source = fromString;
		destination = toString;
		payload = payloadObj;
	}
	
	public String getSource() {
		return source;
	}
	
	public String getDestination() {
		return destination;
	}
	
	public T getPayload() {
		return payload;
	}
}
