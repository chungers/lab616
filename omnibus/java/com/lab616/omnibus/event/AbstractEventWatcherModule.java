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
public abstract class AbstractEventWatcherModule extends AbstractModule {

	public void bind(EventWatcher w) {
		Multibinder<EventWatcher> mbinder = 
			Multibinder.newSetBinder(binder(), EventWatcher.class);
		mbinder.addBinding().toInstance(w);
	}
	
	public void bind(Class<? extends EventWatcher> clz) {
		Multibinder<EventWatcher> mbinder = 
			Multibinder.newSetBinder(binder(), EventWatcher.class);
		mbinder.addBinding().to(clz).in(Scopes.SINGLETON);
	}

	public void bind(Provider<EventWatcher> p) {
		Multibinder<EventWatcher> mbinder = 
			Multibinder.newSetBinder(binder(), EventWatcher.class);
		mbinder.addBinding().toProvider(p).in(Scopes.SINGLETON);
	}
}
