/**
 * Copyright (C) 2006 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.inject.servlet;

import com.google.inject.Inject;
import com.google.inject.OutOfScopeException;
import com.google.inject.Stage;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * Apply this filter in web.xml above all other filters (typically), to all requests where you plan
 *  to use servlet scopes. This is also needed in order to dispatch requests to injectable filters
 *  and servlets:
 *  <pre>
 *  &lt;filter&gt;
 *    &lt;filter-name&gt;guiceFilter&lt;/filter-name&gt;
 *    &lt;filter-class&gt;<b>com.google.inject.servlet.GuiceFilter</b>&lt;/filter-class&gt;
 *  &lt;/filter&gt;
 *
 *  &lt;filter-mapping&gt;
 *    &lt;filter-name&gt;guiceFilter&lt;/filter-name&gt;
 *    &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 *  &lt;/filter-mapping&gt;
 *  </pre>
 *
 * This filter must appear before every filter that makes use of Guice injection or servlet
 * scopes functionality. Typically, you will only register this filter in web.xml and register
 * any other filters (and servlets) using a {@link ServletModule}.
 *
 * @author crazybob@google.com (Bob Lee)
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class GuiceFilter implements Filter {
  static final ThreadLocal<Context> localContext = new ThreadLocal<Context>();
  static volatile FilterPipeline pipeline = new DefaultFilterPipeline();

  /** Used to inject the servlets configured via {@link ServletModule} */
  static volatile WeakReference<ServletContext> servletContext =
      new WeakReference<ServletContext>(null);

  private static final String MULTIPLE_INJECTORS_ERROR =
      "Multiple injectors detected. Please install only one"
          + " ServletModule in your web application. While you may "
          + "have more than one injector, you should only configure"
          + " guice-servlet in one of them. (Hint: look for legacy "
          + "ServetModules or multiple calls to Servlets.configure()).";

  //VisibleForTesting
  @Inject
  static void setPipeline(FilterPipeline pipeline, Stage stage) {

    // Multiple injectors with Servlet pipelines?!
    // We don't throw an exception in DEVELOPMENT stage, to allow for legacy
    // tests that don't have a tearDown that calls GuiceFilter#reset().
    if (GuiceFilter.pipeline instanceof ManagedFilterPipeline) {
      if (Stage.PRODUCTION.equals(stage)) {
        throw new RuntimeException(MULTIPLE_INJECTORS_ERROR);
      } else {
        Logger.getLogger(GuiceFilter.class.getName()).warning(MULTIPLE_INJECTORS_ERROR);
      }
    }

    // We overwrite the default pipeline
    GuiceFilter.pipeline = pipeline;
  }

  //VisibleForTesting
  static void reset() {
    pipeline = new DefaultFilterPipeline();
  }

  public void doFilter(ServletRequest servletRequest,
      ServletResponse servletResponse, FilterChain filterChain)
      throws IOException, ServletException {

    Context previous = localContext.get();
    FilterPipeline filterPipeline = pipeline;

    try {
      localContext.set(new Context((HttpServletRequest) servletRequest,
          (HttpServletResponse) servletResponse));

      //dispatch across the servlet pipeline, ensuring web.xml's filterchain is honored
      filterPipeline.dispatch(servletRequest, servletResponse, filterChain);

    } finally {
      localContext.set(previous);
    }
  }

  static HttpServletRequest getRequest() {
    return getContext().getRequest();
  }

  static HttpServletResponse getResponse() {
    return getContext().getResponse();
  }

  static ServletContext getServletContext() {
    return servletContext.get();
  }

  static Context getContext() {
    Context context = localContext.get();
    if (context == null) {
      throw new OutOfScopeException("Cannot access scoped object. Either we"
          + " are not currently inside an HTTP Servlet request, or you may"
          + " have forgotten to apply " + GuiceFilter.class.getName()
          + " as a servlet filter for this request.");
    }
    return context;
  }

  static class Context {

    final HttpServletRequest request;
    final HttpServletResponse response;

    Context(HttpServletRequest request, HttpServletResponse response) {
      this.request = request;
      this.response = response;
    }

    HttpServletRequest getRequest() {
      return request;
    }

    HttpServletResponse getResponse() {
      return response;
    }
  }

  public void init(FilterConfig filterConfig) throws ServletException {
    final ServletContext servletContext = filterConfig.getServletContext();

    // Store servlet context in a weakreference, for injection
    GuiceFilter.servletContext = new WeakReference<ServletContext>(servletContext);

    // In the default pipeline, this is a noop. However, if replaced
    // by a managed pipeline, a lazy init will be triggered the first time
    // dispatch occurs.
    GuiceFilter.pipeline.initPipeline(servletContext);
  }

  public void destroy() {

    try {
      // Destroy all registered filters & servlets in that order
      pipeline.destroyPipeline();

    } finally {
      reset();
      servletContext.clear();
    }
  }
}
