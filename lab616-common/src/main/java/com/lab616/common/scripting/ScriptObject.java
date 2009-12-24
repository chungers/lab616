/**
 * 
 */
package com.lab616.common.scripting;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author david
 *
 */
@Retention(RUNTIME)
@Target({ ElementType.TYPE })
public @interface ScriptObject {

	String name();
	
	String doc();
}
