// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.event;

/**
 * Utility for working with the {@code @} {@link Event} annotation.
 *
 * @author david
 *
 */
public class Events {

  /**
   * Returns a {@link Event} annotation of the given name.
   * @param name The name of the event.
   * @return Event annotation.
   */
  public static Event named(String name) {
    return new EventImpl(name);
  }
}
