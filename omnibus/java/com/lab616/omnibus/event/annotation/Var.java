// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.event.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Variable... containing the index into a prepared statement parttern.
 * 
 * @author david
 *
 */
@Retention(RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD })
public @interface Var {
	int value();
}

