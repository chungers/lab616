/**
 * 
 */
package com.lab616.common.scripting;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.lab616.common.Pair;
import com.lab616.common.scripting.ScriptObject.Parameter;
import com.lab616.common.scripting.ScriptObject.Script;

/**
 * For binding scripts.
 * 
 * See unit test for use case / usage.
 * 
 * @author david
 *
 */
@Singleton
public class ScriptObjects {

  private final Map<String, ScriptObject> scriptObjects;

  static final Logger logger = Logger.getLogger(ScriptObjects.class);

  @Inject
  public ScriptObjects(Map<String, ScriptObject> scriptObjects) {
    this.scriptObjects = scriptObjects;
  }

  /**
   * Loads the script by name, as specified in annotation.
   * @param name The name.
   * @return The object, if exists.
   */
  public ScriptObject load(String name){
    return this.scriptObjects.get(name);
  }

  /**
   * Loads the script by exact type match.
   * @param <S> The type parameter.
   * @param clz The type of ScriptObject subclass.
   * @return The script object instance.
   */
  public <S extends ScriptObject> ScriptObject load(Class<S> clz) {
    for (ScriptObject s : scriptObjects.values()) {
      if (s.getClass() == clz) {
        return s;
      }
    }
    return null;
  }

  public Iterable<ScriptObject> getScriptObjects() {
    logger.info("Loading script objects: size = " + scriptObjects.size());
    return scriptObjects.values();
  }
    
  public static class Descriptor {
    final Class<? extends ScriptObject> clz;
    public final Method method;
    public final Script annotation;
    public final List<Pair<Parameter, Type>> params;

    Descriptor(Class<? extends ScriptObject> clz,
        Method method, List<Pair<Parameter, Type>> params) {
      this.clz = clz;
      this.method = method;
      this.annotation = method.getAnnotation(Script.class);
      this.params = params;
    }
  }

  private static Map<Method, Descriptor> scriptMethods = 
    Maps.newHashMap();

  static void register(String name, Class<? extends ScriptObject> clz, Method m) {
    if (m.getAnnotation(Script.class) == null) {
      return; // Do nothing.
    }
    Annotation[][] annotations = m.getParameterAnnotations();
    Type[] paramTypes = m.getParameterTypes();
    if (annotations.length != paramTypes.length) {
      throw new IllegalStateException("Missing parameter annotation for " + m);
    }
    List<Pair<Parameter, Type>> paramAnnotations = Lists.newArrayList();
    for (int i = 0; i < paramTypes.length; i++) {
      // Search for Parameter in each array for parameter i:
      for (Annotation a : annotations[i]) {
        if (a instanceof Parameter) {
          paramAnnotations.add(Pair.of((Parameter)a, paramTypes[i]));
        }
      }
    }
    if (paramAnnotations.size() != paramTypes.length) {
      throw new IllegalStateException("Missing parameter annotation for " + m);
    }
    Descriptor desc = new Descriptor(clz, m, paramAnnotations);
    scriptMethods.put(m, desc);
  }

  public static Descriptor getDescriptor(Method m) {
    return scriptMethods.get(m);
  }
}
