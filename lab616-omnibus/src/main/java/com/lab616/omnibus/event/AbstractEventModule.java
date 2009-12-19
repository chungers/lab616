// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.event;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;

/**
 * Abstract Guice module for event watchers.
 * 
 * @author david
 *
 */
public abstract class AbstractEventModule extends AbstractModule {

	protected void bindEventWatcher(AbstractEventWatcher w) {
		Multibinder<AbstractEventWatcher> mbinder = 
			Multibinder.newSetBinder(binder(), AbstractEventWatcher.class);
		mbinder.addBinding().toInstance(w);
	}
	
	protected void bindEventWatcher(Class<? extends AbstractEventWatcher> clz) {
		Multibinder<AbstractEventWatcher> mbinder = 
			Multibinder.newSetBinder(binder(), AbstractEventWatcher.class);
		mbinder.addBinding().to(clz).in(Scopes.SINGLETON);
	}

	protected void bindEventWatcher(Provider<AbstractEventWatcher> p) {
		Multibinder<AbstractEventWatcher> mbinder = 
			Multibinder.newSetBinder(binder(), AbstractEventWatcher.class);
		mbinder.addBinding().toProvider(p).in(Scopes.SINGLETON);
	}

  @SuppressWarnings("unchecked")
  protected void bindEventDefinition(EventDefinition<?> def) {
    Multibinder<EventDefinition> mbinder = 
      Multibinder.newSetBinder(binder(), EventDefinition.class);
    mbinder.addBinding().toInstance(def);
  }
  
  @SuppressWarnings("unchecked")
  protected void bindEventDefinition(Class<? extends EventDefinition<?>> clz) {
    Multibinder<EventDefinition> mbinder = 
      Multibinder.newSetBinder(binder(), EventDefinition.class);
    mbinder.addBinding().to(clz).in(Scopes.SINGLETON);
  }
}
