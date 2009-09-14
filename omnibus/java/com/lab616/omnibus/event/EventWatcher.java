// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.event;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Generalized interface for an entity that is interested in some event
 * stream that matches the given statement with variables.
 * 
 * @author david
 *
 */
public abstract class EventWatcher {

	@Retention(RUNTIME)
	@Target({ ElementType.TYPE })
	public @interface Statement {
		
	}

	@Retention(RUNTIME)
	@Target({ ElementType.TYPE })
	public @interface Expression {
		
	}

	@Retention(RUNTIME)
	@Target({ ElementType.METHOD })
	public @interface Var {
		
	}

	private EventEngine engine;
	
	public final void setEngine(EventEngine engine) {
		this.engine = engine;
	}
	
	protected final EventEngine getEngine() {
		return this.engine;
	}
}
