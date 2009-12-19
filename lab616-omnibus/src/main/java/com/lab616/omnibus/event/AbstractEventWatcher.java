// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.event;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPPreparedStatement;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EPStatementState;
import com.google.common.base.Function;
import com.google.inject.internal.Maps;
import com.google.inject.internal.Preconditions;
import com.lab616.omnibus.event.EventEngine.State;
import com.lab616.omnibus.event.annotation.Events;
import com.lab616.omnibus.event.annotation.Pattern;
import com.lab616.omnibus.event.annotation.Statement;
import com.lab616.omnibus.event.annotation.Var;

/**
 * Generalized interface for an entity that is interested in some event
 * stream that matches the given statement with variables.
 * 
 * @author david
 *
 */
public abstract class AbstractEventWatcher {

	private EventEngine engine;
	private EPStatement statement;
	
	public final void setEngine(EventEngine engine) {
		this.engine = engine;
	}
	
	public final void setStatement(EPStatement statement) {
		this.statement = statement;
	}
	
	public final void start() {
		this.statement.start();
	}
	
	public void stop() {
	  if (this.engine.getState() == State.RUNNING) {
	    synchronized (this.statement) {
	      EPStatementState state = this.statement.getState();
	      if (state != EPStatementState.DESTROYED || 
	          state != EPStatementState.STOPPED) {
	        this.statement.stop();
	      }
	    }
	  }
	}
	
	protected final EventEngine getEngine() {
		return this.engine;
	}
	
	/**
	 * Adds a new EventWatcher to the engine at runtime.
	 * @param watcher The new watcher.
	 */
	protected final void add(AbstractEventWatcher watcher) {
		this.engine.add(watcher);
	}
	
	/**
	 * Posts an event to the engine.
	 * @param eventObject The event object.
	 */
	protected final void post(Object eventObject) {
		this.engine.post(eventObject);
	}
	
	/**
	 * Builds a statement from the Pattern or Statement annotations on the class.
	 * The order for selecting the right statement expression to build the 
	 * EPStatement is 1. getStatement(), 2. getPattern(), 3. @Statement, then
	 * 4. @Pattern. Null values will cause the search to continue to the end.
	 * 
	 * @param builder builder that takes the value from a Statement or Pattern.
	 * @return The epl statement.
	 */
	EPStatement buildFromAnnotations(
			Function<Statement, EPStatement> statementBuilder,
			Function<Pattern, EPStatement> patternBuilder) {
		Preconditions.checkNotNull(statementBuilder);
		Preconditions.checkNotNull(patternBuilder);
		
		if (getStatement() != null) {
			return statementBuilder.apply(Events.statement(getStatement()));
		}
		if (getPattern() != null) {
			return patternBuilder.apply(Events.pattern(getPattern()));
		}
		Statement statement = getClass().getAnnotation(Statement.class);
		if (statement != null) {
			return statementBuilder.apply(statement);
		}
		Pattern pattern = getClass().getAnnotation(Pattern.class);
		if (pattern != null) {
			return patternBuilder.apply(pattern);
		}
		return null;
	}
	
	/**
	 * Override this method to use Statement without annotation.
	 * @return The prepared statement.
	 */
	protected String getStatement() {
		return null;
	}
	
	/**
	 * Override this method to use Pattern without using annotation.
	 * @return The pattern.
	 */
	protected String getPattern() {
		return null;
	}
	
	/**
	 * Override this method to provide statement variables without annotations.
	 * @return The list of parameters.
	 */
	protected Object[] getParameters() {
		return new Object[] {};
	}
	
	/**
	 * Creates a EPL statement object based on the annotations in this
	 * class. Subclasses can optionally override the statement creation behavior.
	 * 
	 * @param admin The admin interface of the epser engine.
	 * @return The statement.
	 */
	EPStatement createStatement(final EPAdministrator admin) {
		return buildFromAnnotations(
				new Function<Statement, EPStatement> () {
					public EPStatement apply(Statement s) {
						EPPreparedStatement prepared = admin.prepareEPL(s.value());
						// Substitute the statement variables.
						return admin.create(prepareFromCurrentState(prepared));
					}
				},
				new Function<Pattern, EPStatement> () {
					public EPStatement apply(Pattern p) {
						EPPreparedStatement prepared = admin.preparePattern(p.value());
						// Substitute the statement variables.
						return admin.create(prepareFromCurrentState(prepared));
					}
				});
	}
	
	/**
	 * Statement's subscriber object which receives the event according to the
	 * esper engine's subscriber object spec.
	 * 
	 * @return The subscriber object.
	 */
	Object getSubscriber() {
		return this;
	}

	/**
	 * Returns a map of prepared statement parameters by the ordering of the
	 * parameters to this method.
	 * 
	 * @param args The parameters.
	 * @return A map of parameters.
	 */
	Map<Integer, Function<AbstractEventWatcher,Object>> getParams(Object... args) {
		Map<Integer, Function<AbstractEventWatcher,Object>> sorted = Maps.newTreeMap();
		int i = 1; 
		for (Object o : args) {
			final Object arg = o;
			sorted.put(i++, new Function<AbstractEventWatcher,Object>() {
				public Object apply(AbstractEventWatcher w) {
					return arg;
				}
			});
		}
		return sorted;
	}
	
	/**
	 * Returns a map of prepared statement parameters based on the current state
	 * of this object and the field and method annotations.  
	 * The keys are in sorted order by the Var annotation index value.
	 * 
	 * @return The view for params of prepared statements.
	 */
	Map<Integer, Function<AbstractEventWatcher,Object>> getParamsFromAnnotations() {
		Map<Integer, Function<AbstractEventWatcher,Object>> annotated = Maps.newTreeMap();
		for (Field f : getClass().getFields()) {
			if (f.getAnnotation(Var.class) != null) {
				Integer index = f.getAnnotation(Var.class).value();
				final Field field = f;
				annotated.put(index, new Function<AbstractEventWatcher, Object> () {
					public Object apply(AbstractEventWatcher w ) {
						try {
							return field.get(w);
						} catch (IllegalAccessException e) {
							throw new EventEngineException(e);
						}
					}
				});
			}
		}
		for (Method m : getClass().getMethods()) {
			if (m.getAnnotation(Var.class) != null && 
					m.getParameterTypes().length == 0) {
				Integer index = m.getAnnotation(Var.class).value();
				final Method method = m;
				annotated.put(index, new Function<AbstractEventWatcher, Object> () {
					public Object apply(AbstractEventWatcher w) {
						try {
							return method.invoke(w);
						} catch (Exception e) {
							throw new EventEngineException(e);
						}
					}
				});
			}
		}
		return annotated;
	}

	/**
	 * Prepares the given prepared statement with the current state of the
	 * object and returns the instance given.
	 * 
	 * @param pstmt The prepared statement.
	 * @return The prepared statement.
	 */
	EPPreparedStatement prepareFromCurrentState(final EPPreparedStatement pstmt) {
		Map<Integer, Function<AbstractEventWatcher,Object>> params = 
			getParams(getParameters());
		
		if (params.isEmpty()) {
			params = getParamsFromAnnotations();
		}
		
		// Now go through the view in sorted sequence of the keys:
		int index = 1;
		for (Integer key : params.keySet()) {
			pstmt.setObject(index++, params.get(key).apply(this));
		}
		return pstmt;
	}
}
