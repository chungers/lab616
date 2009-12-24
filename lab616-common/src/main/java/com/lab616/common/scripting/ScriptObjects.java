/**
 * 
 */
package com.lab616.common.scripting;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * For binding scripts.
 * 
 * See unit test for use case / usage.
 * 
 * @author david
 *
 */
public class ScriptObjects {

	public static class Scripting {
		private Class<?> clz;
		private ScriptObject annotation;
		
		public String toString() {
			return String.format("%s - %s (impl=%s)", 
					annotation.name(), annotation.doc(), 
					clz.getCanonicalName());
		}
	}
	
	private static Map<String, Scripting> scriptObjects = Maps.newHashMap();

	private ScriptObjects() {}
	
	@Inject
	private static Injector injector;

	public static class AddBinding {
		Binder binder;
		public AddBinding bind(Class<?> clz) {
			ScriptObject c = clz.getAnnotation(ScriptObject.class);
			if (c != null) {
				if (scriptObjects.containsKey(c.name())) {
					throw new IllegalStateException("FATAL: Command name " + 
							c.name() + " already bound.");
				}
				Scripting u = new Scripting();
				u.clz = clz;
				u.annotation = c;
				scriptObjects.put(c.name(), u);
			}
			binder.bind(clz);
			return this;
		}
	}
	
	public static AddBinding with(final Binder binder) {
		AddBinding ab = new AddBinding();
		ab.binder = binder;
		return ab;
	}

	public static boolean exists(String script) {
		return scriptObjects.containsKey(script);
	}

	public static List<Scripting> list() {
		return Lists.newArrayList(scriptObjects.values());
	}
	
	public static Object load(String name){
		if (!scriptObjects.containsKey(name)) {
			return null;
		}
		Scripting c = scriptObjects.get(name);
		return injector.getInstance(c.clz);
	}
}
