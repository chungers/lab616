// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.event.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The value of this annotation is actually a statement pattern with ? denoting
 * the variables by position (starting at 1).
 * 
 * @author david
 */
@Retention(RUNTIME)
@Target({ ElementType.TYPE })
public @interface Statement {

	String value();

}
