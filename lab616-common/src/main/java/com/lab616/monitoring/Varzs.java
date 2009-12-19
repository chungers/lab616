// 2009 lab616.com, All Rights Reserved.

package com.lab616.monitoring;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.inject.internal.Maps;

/**
 * Varz exporter class.
 * 
 * @author david
 */

public class Varzs {

	final static Character LIST_SEPARATOR = '|';

	private static Map<String, VarzExporter<?>> registered = Maps.newTreeMap();

	static class VarzExporter<T> implements Comparable<String> {
		Field field;
		Varz varz;
		VarzExporter(Varz varz, Field f) {
			this.varz = varz;
			this.field = f;
		}

		public String name() {
			return this.varz.name();
		}
		
		public String value() {
			try {
				return getValue();
			} catch (Exception e) {
				return "EXCEPTION";
			}
		}
		
		public String getValue() throws Exception {
			Object v = this.field.get(null);
			return (v == null) ? "null" : v.toString(); 
		}

		public final String toString() {
			return this.varz.name();
		}

		public final int compareTo(String key) {
			return name().compareTo(key);
		}
	}

	// For Array
	static class ArrayVarzExporter<T> extends VarzExporter<T[]> {
		ArrayVarzExporter(Varz varz, Field f) {
			super(varz, f);
		}

		public String getValue() throws Exception {
			Object array = this.field.get(null);
			StringBuffer buff = new StringBuffer();
			int len = Array.getLength(array);
			for (int i = 0; i < len - 1; i++) {
				buff.append(Array.get(array, i));
				buff.append(LIST_SEPARATOR);
			}
			buff.append(Array.get(array, len - 1));
			return buff.toString();
		}
	}

	// For List and Set
	static class IterableVarzExporter<T> extends VarzExporter<Iterable<T>> {
		IterableVarzExporter(Varz varz, Field f) {
			super(varz, f);
		}

		@SuppressWarnings("unchecked")
		public String getValue() throws Exception {
			Iterator<T> itr = ((Iterable<T>)(this.field.get(null))).iterator();
			StringBuffer buff = new StringBuffer();
			if (itr.hasNext()) {
				buff.append(itr.next());
			}
			while (itr.hasNext()) {
				buff.append(LIST_SEPARATOR);
				buff.append(itr.next());
			}
			return buff.toString();
		}
	}

	// For Maps
	static class MapVarzExporter<K, V> extends VarzExporter<Map<K, V>> {
		MapVarzExporter(Varz varz, Field f) {
			super(varz, f);
		}

		@SuppressWarnings("unchecked")
		public String getValue() throws Exception {
			Map<K, V> map = ((Map<K, V>)(this.field.get(null)));
			StringBuffer buff = new StringBuffer();
			int i = 0;
			for (K key : map.keySet()) {
				buff.append(key);
				buff.append(":");
				buff.append(map.get(key));
				if (++i < map.size()) {
					buff.append(LIST_SEPARATOR);
				}
			}
			return buff.toString();
		}
	}

	/**
	 * Samples the registered varz values in form of NAME=VALUE 
	 * 
	 * @return
	 */
	public final static Iterable<String> getValues() {
		return new Iterable<String>() {
			public Iterator<String> iterator() {
				return new Iterator<String>() {
					Iterator<String> itr = registered.keySet().iterator();
					public boolean hasNext() {
						return itr.hasNext();
					}
					public String next() {
						VarzExporter<?> v = registered.get(itr.next());
						return String.format("%s=%s", v.name(), v.value());
					}
					public void remove() {
						// No-op.
					}
				};
			}
		};
	}
	
	public static void export(Class<?> clz) {
		// Reflect on the class to look for all public static variables.
		VarzExporter<?> desc;
		for (Field f : clz.getDeclaredFields()) {
			Varz varz = f.getAnnotation(Varz.class);
			int m = f.getModifiers();
			if (varz != null && Modifier.isPublic(m) && Modifier.isStatic(m)) {
				Type gType = f.getGenericType();
				if (f.getType().isArray()) {
					Class<?> ct = f.getType().getComponentType();

					if (ct.isPrimitive()) {
						throw new IllegalArgumentException(
								"Primitive arrays not supported: " + f + "@" + varz);
					}

					if (ct.equals(String.class)) {
						desc = new ArrayVarzExporter<String>(varz, f);
					} else if (ct.equals(Boolean.class)) {
						desc = new ArrayVarzExporter<Boolean>(varz, f);
					} else if (ct.equals(Integer.class)) {
						desc = new ArrayVarzExporter<Integer>(varz, f);
					} else if (ct.equals(Long.class)) {
						desc = new ArrayVarzExporter<Long>(varz, f);
					} else if (ct.equals(Float.class)) {
						desc = new ArrayVarzExporter<Float>(varz, f);
					} else if (ct.equals(Double.class)) {
						desc = new ArrayVarzExporter<Double>(varz, f);
					} else {
						desc = new ArrayVarzExporter<Object>(varz, f);
					}

					if (desc != null) {
						registered.put(desc.name(), desc);
					}
				} else if (gType instanceof ParameterizedType && 
						((ParameterizedType)gType).getRawType().equals(List.class)) {
					// Container such as List<T>
					//Type eType = ((ParameterizedType)gType).getActualTypeArguments()[0];

					desc = new IterableVarzExporter<Object>(varz, f);

					if (desc != null) {
						registered.put(desc.name(), desc);
					}
				} else if (gType instanceof ParameterizedType && 
						((ParameterizedType)gType).getRawType().equals(Map.class)) {
					// Container such as Map<K, V>
					//Type eType = ((ParameterizedType)gType).getActualTypeArguments()[0];

					desc = new MapVarzExporter<String, Object>(varz, f);

					if (desc != null) {
						registered.put(desc.name(), desc);
					}
				} else {
					if (gType.equals(String.class)) {
						desc = new VarzExporter<String>(varz, f);
					} else if (gType.equals(Integer.class)) {
						desc = new VarzExporter<Integer>(varz, f);
					} else if (gType.equals(Long.class)) {
						desc = new VarzExporter<Long>(varz, f);
					} else if (gType.equals(Float.class)) {
						desc = new VarzExporter<Float>(varz, f);
					} else if (gType.equals(Boolean.class)) {
						desc = new VarzExporter<Boolean>(varz, f);
					} else if (gType.equals(Double.class)) {
						desc = new VarzExporter<Double>(varz, f);
					} else {
						desc = new VarzExporter<Object>(varz, f);
					}

					if (desc != null) {
						registered.put(desc.name(), desc);
					}
				}
			}
		}
	}
}