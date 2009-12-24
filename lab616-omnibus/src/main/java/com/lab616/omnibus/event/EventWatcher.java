// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.event;


/**
 * Event watcher using a Statement which can be created dynamically 
 * rather than statically in code using annotations.
 * 
 * @author david
 *
 */
public abstract class EventWatcher<T> extends AbstractEventWatcher 
	implements EventSubscriber<T> {

	private final String statement;
	private final Object[] parameters;
	protected boolean isStatement = true;
	
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
	  return (isStatement) ? null : this.statement;
  }

	@Override
  protected final String getStatement() {
		return (isStatement) ? this.statement : null;
	}
}
