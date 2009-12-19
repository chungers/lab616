// 2009 lab616.com, All Rights Reserved.

package com.lab616.common.builder;


/**
 * @author david
 *
 */
public interface Builder<T> {

	interface Property<T> {
	  public Builder<T> to(Object o);
	}

	public T build();
  public Property<T> setProperty(String name, Class<?>... argTypes);
  
  public Builder<T> set(String name, Object v);
}
