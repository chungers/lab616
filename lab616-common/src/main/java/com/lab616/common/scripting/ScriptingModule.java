/**
 * 
 */
package com.lab616.common.scripting;

import com.google.inject.Binder;
import com.google.inject.Module;

/**
 * @author david
 *
 */
public class ScriptingModule implements Module {
	
	public void configure(Binder binder) {
		binder.requestStaticInjection(ScriptObjects.class);
	}
}
