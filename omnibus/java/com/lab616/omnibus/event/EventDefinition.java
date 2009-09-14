// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.event;

import com.espertech.esper.client.Configuration;

/**
 * Event definition
 * @param <T> Type parameter: Class or Map.
 */
public abstract class EventDefinition<T> {
	final String name;
	final T eventType;
	
	public EventDefinition(String name, T eventType) {
		this.name = name;
		this.eventType = eventType;
	}

	public String name() {
		return this.name;
	}
	
	public T type() {
		return this.eventType;
	}
	
	public String toString() {
		return String.format("EventType[%s:%s]", this.name, this.eventType);
	}
	
	public boolean equals(Object o) {
		if (o instanceof EventDefinition<?>) {
			EventDefinition<?> e = (EventDefinition<?>)o;
			return e.eventType.equals(eventType) && e.name.equals(name);
		}
		return false;
	}
	
	abstract void configure(Configuration configuration);
}
