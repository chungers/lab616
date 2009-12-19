// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.event;


/**
 * @author david
 *
 */
public class EventEngineException extends RuntimeException {
	
  private static final long serialVersionUID = 1L;

	public EventEngineException(Exception e) {
		super(e);
	}
}
