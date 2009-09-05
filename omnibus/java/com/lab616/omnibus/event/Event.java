// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.event;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation for Events.
 *
 * @author david
 *
 */
@Retention(RUNTIME)
@Target({ ElementType.TYPE })
public @interface Event {

  String value();

}
