// 2009 lab616.com, All Rights Reserved.

package com.lab616.common;

/**
 * Pair
 *
 * @author david
 *
 */
public class Pair<A, B> {

  public A first;
  public B second;
  
  
  public static <A, B> Pair<A, B> of(A a, B b) {
    Pair<A, B> pair = new Pair<A, B>();
    pair.first = a;
    pair.second = b;
    return pair;
  }
}
