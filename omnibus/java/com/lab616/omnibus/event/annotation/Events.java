// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.event.annotation;

import java.io.Serializable;
import java.lang.annotation.Annotation;

import com.lab616.omnibus.event.EventMessage;

/**
 * Utility for working with the {@code @} {@link EventMessage} annotation.
 *
 * @author david
 *
 */
public class Events {

	static class StatementImpl implements Statement, Serializable {
	  
    private static final long serialVersionUID = 1666102845366095397L;
		private final String value;

	  public StatementImpl(String value) {
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
	    if (!(o instanceof Statement)) {
	      return false;
	    }

	    Statement other = (Statement) o;
	    return value.equals(other.value());
	  }

	  public String toString() {
	    return "@" + Statement.class.getName() + "(value=" + value + ")";
	  }

	  public Class<? extends Annotation> annotationType() {
	    return Statement.class;
	  }
	}

	/**
   * Returns a {@link Statement} annotation of the given value.
   * @param pattern Statement pattern
   * @return Statement annotation.
   */
  public static Statement statement(String pattern) {
    return new StatementImpl(pattern);
  }
  
  
	static class PatternImpl implements Pattern, Serializable {

		private static final long serialVersionUID = -6661014760793819614L;
		private final String value;

	  public PatternImpl(String value) {
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
	    if (!(o instanceof Pattern)) {
	      return false;
	    }

	    Pattern other = (Pattern) o;
	    return value.equals(other.value());
	  }

	  public String toString() {
	    return "@" + Pattern.class.getName() + "(value=" + value + ")";
	  }

	  public Class<? extends Annotation> annotationType() {
	    return Pattern.class;
	  }
	}

	/**
   * Returns a {@link Pattern} annotation of the given value.
   * @param pattern The pattern
   * @return Pattern annotation.
   */
  public static Pattern pattern(String pattern) {
    return new PatternImpl(pattern);
  }
}
