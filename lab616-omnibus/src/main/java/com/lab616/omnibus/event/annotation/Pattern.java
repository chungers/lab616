// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.event.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Pattern annotation.  The value is a pattern that can contain ? as variables.
 * 
 * @author david
 */
@Retention(RUNTIME)
@Target({ ElementType.TYPE })
public @interface Pattern {
	String value();
}

