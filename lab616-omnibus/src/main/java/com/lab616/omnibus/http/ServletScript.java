// 2009-2010 lab616.com, All Rights Reserved.
package com.lab616.omnibus.http;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author david
 *
 */
@Retention(RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface ServletScript {

  /**
   * The url path that exposes this script via http.
   * @return The path.
   */
  String path();
}
