/**
 * 
 */
package com.lab616.common.scripting;

import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.Singleton;

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
}
