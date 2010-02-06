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

  @Retention(RUNTIME)
  @Target({ ElementType.PARAMETER})
  public @interface Parameter {
    /**
     * Name of the parameter, as accessible by mappings of key/value pair.
     * @return The name of the parameter.
     */
    String name();

    /**
     * Default value if non supplied.  Type conversion is required to take
     * the default value string and convert it to the proper type required
     * by the script.  For complex types, this is not required because the value
     * of this parameter will be a session-scoped variable name/reference that is
     * automatically resolved prior to script invocation.
     * @return The string value of the parameter.
     */
    String defaultValue() default "";

    /**
     * The doc string.
     * @return The document string.
     */
    String doc() default "";
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
