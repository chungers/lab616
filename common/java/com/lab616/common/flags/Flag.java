// 2009 lab616.com, All Rights Reserved.

package com.lab616.common.flags;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author david
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Flag {
	
  final static String EMPTY_VALUE = "";
  final static Character LIST_SEPARATOR = ',';
  
	/**
	 * Name of the flag.
	 * @return The name.
	 */
	String name();
	
	/**
	 * Default string value.
	 * @return The default value.
	 */
	String defaultValue() default EMPTY_VALUE;
	
	/**
	 * Whether this flag is required or not.  Default is optional.
	 * @return True if required.
	 */
	boolean required() default false;
	
	/**
	 * Doc string, useful for printing help messages.
	 * @return The doc string.
	 */
	String doc() default "";
}
