/**
 * 
 */
package com.lab616.common.scripting;

import java.lang.reflect.Method;

import com.google.inject.AbstractModule;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.multibindings.MapBinder;
import com.lab616.common.scripting.ScriptObject.Script;
import com.lab616.common.scripting.ScriptObject.ScriptModule;

/**
 * Guice module for the scripting framework.
 * @author david
 *
 */
public abstract class AbstractScriptingModule extends AbstractModule {

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
    // Get the name of the script module:
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
        ScriptObjects.register(scriptName, clz, m);
      }
    }
    // Map top-level module
    getScriptObjectBinder().addBinding(moduleName).to(clz);
  }
	
	
}
