// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.http;

import javax.servlet.http.HttpServlet;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.multibindings.MapBinder;

/**
 * Abstract Guice module for servlets.
 * 
 * @author david
 *
 */
public abstract class AbstractHttpServletModule extends AbstractModule {

	public void bind(String url, HttpServlet instance) {
		MapBinder<String, HttpServlet> mbinder = 
			MapBinder.newMapBinder(binder(), String.class, HttpServlet.class);
		mbinder.addBinding(url).toInstance(instance);
	}

	public void bind(String url, Provider<HttpServlet> provider) {
		MapBinder<String, HttpServlet> mbinder = 
			MapBinder.newMapBinder(binder(), String.class, HttpServlet.class);
		mbinder.addBinding(url).toProvider(provider);
	}

	public void bind(String url, Class<? extends HttpServlet> servletClz) {
		MapBinder<String, HttpServlet> mbinder = 
			MapBinder.newMapBinder(binder(), String.class, HttpServlet.class);
		mbinder.addBinding(url).to(servletClz);
	}
}
