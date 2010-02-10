/**
 * 
 */
package com.lab616.common.scripting;

import com.google.inject.AbstractModule;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.multibindings.MapBinder;
import com.lab616.common.scripting.ScriptObject.Script;
import com.lab616.common.scripting.ScriptObject.ScriptModule;
import java.lang.reflect.Method;

/**
 * Guice module for the scripting framework.
 * @author david
 *
 */
public class ScriptingModule extends AbstractModule {

  public static class Scripting {

    private Class<?> clz;
    private Script annotation;

    public String toString() {
      return String.format("%s - %s (impl=%s)",
        annotation.name(), annotation.doc(),
        clz.getCanonicalName());
    }
  }


  protected MapBinder<String, ScriptObject> getScriptObjectBinder() {
    return MapBinder.newMapBinder(binder(), String.class, ScriptObject.class);
  }

  
  @SuppressWarnings("unchecked")
	@Override
  protected <T> AnnotatedBindingBuilder<T> bind(Class<T> clazz) {
  	if (ScriptObject.class.isAssignableFrom(clazz)) {
  		bindScript((Class<ScriptObject>)clazz);
  	}
  	return super.bind(clazz);
  }

  /**
   * Binds a script object.
   * @param clz The script object class.
   */
	protected void bindScript(Class<? extends ScriptObject> clz) {
    // Ge the name of the script module:
    ScriptModule sm = clz.getAnnotation(ScriptModule.class);
    if (sm == null) {
      throw new IllegalStateException("Missing @ScriptModule annotation: " +
        clz);
    }
    String moduleName = sm.name();
    // Get all the annotated methods:
    for (Method m : clz.getMethods()) {
      Script s = m.getAnnotation(Script.class);
      if (s != null) {
        String scriptName = String.format("%s.%s", moduleName, s.name());
        getScriptObjectBinder().addBinding(scriptName).to(clz);
      }
    }
    // Map top-level module
    getScriptObjectBinder().addBinding(moduleName).to(clz);
  }

  @Override
  protected void configure() {
    // Do nothing.
  }
}
