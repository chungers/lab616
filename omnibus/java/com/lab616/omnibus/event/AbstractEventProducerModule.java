// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.event;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;

/**
 * Abstract Guice module for event producers.  An event producer is required
 * to provide registries of events for the event engine.
 * 
 * @author david
 *
 */
abstract class AbstractEventProducerModule extends AbstractModule {

	@SuppressWarnings("unchecked")
  void bindEventDefinition(EventDefinition<?> def) {
		Multibinder<EventDefinition> mbinder = 
			Multibinder.newSetBinder(binder(), EventDefinition.class);
		mbinder.addBinding().toInstance(def);
	}
	
  @SuppressWarnings("unchecked")
  void bindEventDefinition(Class<? extends EventDefinition<?>> clz) {
		Multibinder<EventDefinition> mbinder = 
			Multibinder.newSetBinder(binder(), EventDefinition.class);
		mbinder.addBinding().to(clz).in(Scopes.SINGLETON);
	}
}
