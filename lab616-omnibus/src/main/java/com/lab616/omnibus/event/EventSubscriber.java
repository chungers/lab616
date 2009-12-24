// 2009-2010 lab616.com, All Rights Reserved.
package com.lab616.omnibus.event;

/**
 * @author david
 *
 */
public interface EventSubscriber<T> {

	public void update(T event);
	
}
