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

  protected MapBinder<String, HttpServlet> getServletBinder() {
    return MapBinder.newMapBinder(binder(), String.class, HttpServlet.class);
  }
  
	public void bind(String url, HttpServlet instance) {
		getServletBinder().addBinding(url).toInstance(instance);
	}

	public void bind(String url, Provider<HttpServlet> provider) {
	  getServletBinder().addBinding(url).toProvider(provider);
	}

	public void bind(String url, Class<? extends HttpServlet> servletClz) {
	  getServletBinder().addBinding(url).to(servletClz);
	}
}
