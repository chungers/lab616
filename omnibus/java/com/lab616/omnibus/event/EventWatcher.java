// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.event;

import java.lang.annotation.Annotation;

/**
 * Event watcher using a Statement which can be created dynamically 
 * rather than statically in code using annotations.
 * 
 * @author david
 *
 * @param <A> The annotation type @Pattern or @statement.
 */
public abstract class EventWatcher<A extends Annotation> 
	extends AbstractEventWatcher {

	private final String statement;
	private final Object[] parameters;
	
	public EventWatcher(String exp, Object... args) {
		this.statement = exp;
		this.parameters = args;
	}

	@Override
  protected final Object[] getParameters() {
	  return parameters;
  }

	@Override
  protected final String getPattern() {
	  return null;
  }

	@Override
  protected final String getStatement() {
		return this.statement;
	}
}
