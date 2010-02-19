// 2009-2010 lab616.com, All Rights Reserved.
package com.lab616.common;

import java.lang.reflect.Type;



/**
 * Converters for different types.
 * 
 * @author david
 *
 */
public class Converters {

	/**
	 * Returns the converted typed value from a string, using the converter
	 * registered.
	 * @param <T> The output type.
	 * @param input The input string.
	 * @param toType The output type instance.
	 * @return The typed value.
	 */
  public static Object fromString(String input, Type toType) {
		Converter.FromString<?> function = Converter.stringConverters.get(toType);
		Class<?> resultType = Converter.resultTypes.get(function);
		if (function != null) {
			return resultType.cast(function.apply(input));
		}
		throw new IllegalArgumentException("Unsupported type: " + toType + 
				" for value " + input);
	}

	public static <T> Object fromString(String input, Class<T> toType, T defaultValue) {
		try {
			return fromString(input, toType);
		} catch (IllegalArgumentException e) {
			return defaultValue;
		} catch (ClassCastException e) {
			return defaultValue;
		} catch (Exception e) {
			throw new IllegalArgumentException("Bad input " + input + " for type " + toType);
		}
	}
	
	/**
	 * Interface for value lookup by key.
	 */
	public interface ValueRepository {
		public Object get(String key);
	}

	/**
	 * Returns the typed value by type conversion, with a repository for value
	 * lookup if type conversion fails.  This works for complex types that
	 * are not registered with the converters.  The string input is used as a key
	 * to look up the value.
	 * @param <T> The type.
	 * @param input The input string.
	 * @param toType The output type.
	 * @param repo The value repository for value lookup.
	 * @return A typed value.
	 */
	public static <T> Object fromString(String input, Class<T> toType, ValueRepository repo) {
		try {
			return fromString(input, toType);
		} catch (IllegalArgumentException e) {
			return toType.cast(repo.get(input));
		} catch (ClassCastException e) {
			return toType.cast(repo.get(input));
		} catch (Exception e) {
			throw new IllegalArgumentException("Bad input " + input + " for type " + toType);
		}
	}
	
}
