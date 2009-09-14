// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.event;


/**
 * @author david
 *
 */
public class EventEngineException extends RuntimeException {

	public enum ErrorCode {
		INVALID_STATE("InvalidState(Current=%s)");
		
		private String format;
		ErrorCode(String f) {
			format = f;
		}
		
		String getMessage(Object... args) {
			return String.format(format, args);
		}
	}
	
  private static final long serialVersionUID = 1L;

	public EventEngineException(Exception e) {
		super(e);
	}
	
	public EventEngineException(ErrorCode c, Object... args) {
		super(c.getMessage(args));
	}
}
