// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.event;

import java.io.Serializable;
import java.lang.annotation.Annotation;

/**
 * Implementation of the Event annotation class.
 *
 * @author david
 *
 */
class EventImpl implements Event, Serializable {
  
  private static final long serialVersionUID = 164117457334688167L;
  private final String value;

  public EventImpl(String value) {
    this.value = value;
  }

  public String value() {
    return this.value;
  }

  public int hashCode() {
    // This is specified in java.lang.Annotation.
    return (127 * "value".hashCode()) ^ value.hashCode();
  }

  public boolean equals(Object o) {
    if (!(o instanceof Event)) {
      return false;
    }

    Event other = (Event) o;
    return value.equals(other.value());
  }

  public String toString() {
    return "@" + Event.class.getName() + "(value=" + value + ")";
  }

  public Class<? extends Annotation> annotationType() {
    return Event.class;
  }
}
