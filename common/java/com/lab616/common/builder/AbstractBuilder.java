// 2009 lab616.com, All Rights Reserved.

package com.lab616.common.builder;

import java.lang.reflect.Field;
import java.util.Map;

import com.google.inject.internal.Maps;

/**
 * A builder for IB API objects.
 * 
 * @author david
 *
 * @param <T>
 */
public class AbstractBuilder<T> implements Builder<T> {

  private final Class<T> clz;
  private Map<String, Field> properties = Maps.newHashMap();
  private Map<String, Object> values = Maps.newHashMap();
  
  public AbstractBuilder(Class<T> clz) {
    this.clz = clz;
  }
  
  /**
   * Simple setter.
   */
  public final Builder<T> setProperty(String name, Object v) {
    return setProperty(name).to(v);
  }

  /**
   * Sets a property for the underlying object to be constructed.
   * @param prop Property name, matches to the field name in the IB class.
   * @return
   */
  public final Property<T> setProperty(final String prop) {
    try {
      if (!properties.containsKey(prop)) {
        Field f = clz.getField(prop);
        properties.put(prop, f);
      }
      return new Property<T>() {
        public Builder<T> to(Object o) {
          values.put(prop, o);
          return AbstractBuilder.this;
        }
      };
    } catch (Exception e) {
      throw new BuilderException(e);
    }
  }
  
  public final T build() {
    try {
      T object = clz.newInstance();

      // Set the object state based on what's in this builder.
      for (String p : properties.keySet()) {
        Field f = properties.get(p);
        Object v = values.get(p);
        f.set(object, v);
      }
      
      return object;
    } catch (Exception e) {
      throw new BuilderException(e);
    }
  }
}
