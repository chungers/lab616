// 2009 lab616.com, All Rights Reserved.

package com.lab616.common;

import java.io.Serializable;

/**
 * Pair
 *
 * @author david
 *
 */

public class Pair<A, B> implements Serializable {
  
  private static final long serialVersionUID = 1L;

  public A first;
  public B second;
  
  public Pair(A first, B second) {
    this.first = first;
    this.second = second;
  }
  
  public static <A, B> Pair<A, B> of(A a, B b) {
    return new Pair<A, B>(a, b);
  }
  
  public String toString() {
    return String.format("(%s,%s)", first, second);
  }
}
