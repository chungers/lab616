/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.avro.reflect;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.avro.AvroRuntimeException;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;

/**
 * {@link org.apache.avro.io.DatumReader DatumReader} for existing classes via
 * Java reflection.
 */
public class ReflectDatumReader extends GenericDatumReader<Object> {
  public ReflectDatumReader() {}

  public ReflectDatumReader(Class c) {
    this(ReflectData.get().getSchema(c));
  }

  public ReflectDatumReader(Schema root) {
    setSchema(root);
  }

  protected Object newRecord(Object old, Schema schema) {
    Class c = ReflectData.get().getClass(schema);
    return (c.isInstance(old) ? old : newInstance(c));
  }

  protected void addField(Object record, String name, int position, Object o) {
    try {
      ReflectData.getField(record.getClass(), name).set(record, o);
    } catch (IllegalAccessException e) {
      throw new AvroRuntimeException(e);
    }
  }

  protected Object getField(Object record, String name, int position) {
    try {
      return ReflectData.getField(record.getClass(), name).get(record);
    } catch (IllegalAccessException e) {
      throw new AvroRuntimeException(e);
    }
  }

  protected void removeField(Object record, String name, int position) {
    addField(record, name, position, null);
  }

  @SuppressWarnings("unchecked")
  protected Object createEnum(String symbol, Schema schema) {
    return Enum.valueOf(ReflectData.get().getClass(schema), symbol);
  }

  protected Object createFixed(Object old, Schema schema) {
    Class c = ReflectData.get().getClass(schema);
    return c.isInstance(old) ? old : newInstance(c);
  }

  private static final Class<?>[] EMPTY_ARRAY = new Class[]{};
  private static final Map<Class,Constructor> CTOR_CACHE =
    new ConcurrentHashMap<Class,Constructor>();

  /** Create a new instance of the named class. */
  @SuppressWarnings("unchecked")
  protected static Object newInstance(Class c) {
    Object result;
    try {
      Constructor meth = (Constructor)CTOR_CACHE.get(c);
      if (meth == null) {
        meth = c.getDeclaredConstructor(EMPTY_ARRAY);
        meth.setAccessible(true);
        CTOR_CACHE.put(c, meth);
      }
      result = meth.newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return result;
  }

}

