/**
 * 
 */
package com.lab616.common.scripting;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

public abstract class ScriptObject {

  @Retention(RUNTIME)
  @Target({ ElementType.TYPE })
  public @interface ScriptModule {

    String name();
    String doc();
  }


  @Retention(RUNTIME)
  @Target({ ElementType.METHOD})
  public @interface Script {

    String name();
    String doc();
  }

  protected ScriptObject() {
    
  }

  /**
   * Casts to an instance of the given type.
   * @param <T> The type to convert to.
   * @param clz
   * @return
   */
  public final <T extends ScriptObject> T asInstanceOf(Class<T> clz) {
    // Sanity check
    if (!clz.isAssignableFrom(getClass())) {
      throw new IllegalArgumentException("Not a valid class: " + clz);
    }
    return clz.cast(this);
  }
}
