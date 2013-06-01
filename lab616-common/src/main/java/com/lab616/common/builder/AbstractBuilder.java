// 2009 lab616.com, All Rights Reserved.

package com.lab616.common.builder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * A builder for IB API objects.
 * 
 * @author david
 *
 * @param <T>
 */
public class AbstractBuilder<T> implements Builder<T> {

  abstract class PropertySetter {
    abstract void apply(Object object, String prop) throws Exception;
    protected Object getValue(String prop) {
      return values.get(prop);
    }
  }
  
  private final Class<T> clz;
  private Map<String, PropertySetter> properties = Maps.newHashMap();
  private Map<String, Object> values = Maps.newHashMap();
  
  public AbstractBuilder(Class<T> clz) {
    this.clz = clz;
  }

  private PropertySetter getPropertySetter(final Field f) {
    return new PropertySetter() {
      @Override
      void apply(Object object, String prop) throws Exception {
        f.set(object, getValue(prop));
      }
    };
  }

  private PropertySetter getPropertySetter(final Method m) {
    return new PropertySetter() {
      @Override
      void apply(Object object, String prop) throws Exception {
        m.invoke(object, getValue(prop));
      }
    };
  }
 
  /**
   * Returns the property value.
   * @param name The property
   * @return The value.
   */
  public final Object getProperty(String name) {
    return values.get(name);
  }
  
  /**
   * Simple setter.
   */
  @Override
  public final Builder<T> set(String name, Object v) {
    return setProperty(name, new Class<?>[] { v.getClass() }).to(v);
  }

  /**
   * Sets a property for the underlying object to be constructed.
   * @param prop Property name, matches to the field name in the IB class.
   * @return
   */
  @Override
  public final Property<T> setProperty(final String prop, Class<?>... types) {
    try {
      if (!properties.containsKey(prop)) {
        try {
          Field f = clz.getField(prop);
          properties.put(prop, getPropertySetter(f));
        } catch (NoSuchFieldException e) {
          try {
            Method m = clz.getMethod(
                "set" + 
                prop.toUpperCase().charAt(0) + prop.substring(1), 
                types);
            properties.put(prop, getPropertySetter(m));
          } catch (NoSuchMethodException e2) {
            throw new BuilderException(e2);
          }
        }
      }
      return new Property<T>() {
        @Override
        public Builder<T> to(Object o) {
          values.put(prop, o);
          return AbstractBuilder.this;
        }
      };
    } catch (Exception e) {
      throw new BuilderException(e);
    }
  }
  
  @Override
  public T build() {
    try {
      T object = clz.newInstance();

      // Set the object state based on what's in this builder.
      for (String p : properties.keySet()) {
        PropertySetter s = properties.get(p);
        s.apply(object, p);
      }
      
      return object;
    } catch (Exception e) {
      throw new BuilderException(e);
    }
  }
}
