// 2009 lab616.com, All Rights Reserved.

package com.lab616.common;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Parser from String to some type.
 *
 * @author david
 *
 */
public class Converter {
  
	public static DateTimeFormatter ISO8601 = ISODateTimeFormat.dateTime();

	public static Map<Type, FromString<?>> stringConverters =
		Maps.newHashMap();
	public static Map<FromString<?>, Class<?>> resultTypes =
		Maps.newHashMap();

	/**
	 * Abstract class that automatically registers the converter function
	 * for converting type T from a string input.
	 * @param <T> Destination type.
	 */
	static abstract class FromString<T> implements Function<String, T> {
		protected FromString(Class<T> clz, Class<?>... others) {
			stringConverters.put(clz, this);
			resultTypes.put(this, clz);
			for (Class<?> c : others) {
				stringConverters.put(c, this);
			}
		}
	}

  public static Function<String, DateTime> TO_DATETIME = 
    new FromString<DateTime>(DateTime.class) {
    public DateTime apply(String v) {
      return ISO8601.parseDateTime(v);
    }
  };
	
  public static Function<String, String> TO_STRING = 
    new FromString<String>(String.class) {
    public String apply(String v) {
      return v;
    }
  };
  
  public static Function<String, Boolean> TO_BOOLEAN = 
    new FromString<Boolean>(Boolean.class, boolean.class) {
    public Boolean apply(String v) {
      return Boolean.parseBoolean(v);
    }
  };

  public static Function<String, Integer> TO_INTEGER = 
    new FromString<Integer>(Integer.class, int.class) {
    public Integer apply(String v) {
      return Integer.decode(v);
    }
  };

  public static Function<String, Long> TO_LONG = 
  	new FromString<Long>(Long.class, long.class) {
    public Long apply(String v) {
      return Long.decode(v);
    }
  };

  public static Function<String, Float> TO_FLOAT = 
  	new FromString<Float>(Float.class, float.class) {
    public Float apply(String v) {
      return Float.parseFloat(v);
    }
  };

  public static Function<String, Double> TO_DOUBLE = 
  	new FromString<Double>(Double.class, double.class) {
    public Double apply(String v) {
      return Double.parseDouble(v);
    }
  };

  static <T> T[] convertArray(String[] in, T[] out, Function<String, T> f) {
    for (int i = 0; i < out.length; i++) {
      out[i] = f.apply(in[i]);
    }
    return out;
  }
  
  public static Function<String[], DateTime[]> TO_DATETIME_ARRAY = 
    new Function<String[], DateTime[]>() {
    public DateTime[] apply(String[] v) {
      return convertArray(v, new DateTime[v.length], TO_DATETIME);
    }
  };

  public static Function<String[], List<DateTime>> TO_DATETIME_LIST = 
    new Function<String[], List<DateTime>>() {
    public List<DateTime> apply(String[] v) {
      return Lists.newArrayList(TO_DATETIME_ARRAY.apply(v));
    }
  };

  public static Function<String[], Set<DateTime>> TO_DATETIME_SET = 
    new Function<String[], Set<DateTime>>() {
    public Set<DateTime> apply(String[] v) {
      return Sets.newHashSet(TO_DATETIME_ARRAY.apply(v));
    }
  };

  public static Function<String[], String[]> TO_STRING_ARRAY = 
    new Function<String[], String[]>() {
    public String[] apply(String[] v) {
      return v;
    }
  };

  public static Function<String[], List<String>> TO_STRING_LIST = 
    new Function<String[], List<String>>() {
    public List<String> apply(String[] v) {
      return Lists.newArrayList(v);
    }
  };

  public static Function<String[], Set<String>> TO_STRING_SET = 
    new Function<String[], Set<String>>() {
    public Set<String> apply(String[] v) {
      return Sets.newHashSet(v);
    }
  };

  public static Function<String[], Boolean[]> TO_BOOLEAN_ARRAY = 
    new Function<String[], Boolean[]>() {
    public Boolean[] apply(String[] v) {
      return convertArray(v, new Boolean[v.length], TO_BOOLEAN);
    }
  };

  public static Function<String[], List<Boolean>> TO_BOOLEAN_LIST = 
    new Function<String[], List<Boolean>>() {
    public List<Boolean> apply(String[] v) {
      return Lists.newArrayList(TO_BOOLEAN_ARRAY.apply(v));
    }
  };

  public static Function<String[], Set<Boolean>> TO_BOOLEAN_SET = 
    new Function<String[], Set<Boolean>>() {
    public Set<Boolean> apply(String[] v) {
      return Sets.newHashSet(TO_BOOLEAN_ARRAY.apply(v));
    }
  };

  public static Function<String[], Integer[]> TO_INTEGER_ARRAY = 
    new Function<String[], Integer[]>() {
    public Integer[] apply(String[] v) {
      return convertArray(v, new Integer[v.length], TO_INTEGER);
    }
  };

  public static Function<String[], List<Integer>> TO_INTEGER_LIST = 
    new Function<String[], List<Integer>>() {
    
    public List<Integer> apply(String[] v) {
      return Lists.newArrayList(TO_INTEGER_ARRAY.apply(v));
    }
  };

  public static Function<String[], Set<Integer>> TO_INTEGER_SET = 
    new Function<String[], Set<Integer>>() {
    public Set<Integer> apply(String[] v) {
      return Sets.newHashSet(TO_INTEGER_ARRAY.apply(v));
    }
  };

  public static Function<String[], Long[]> TO_LONG_ARRAY = 
    new Function<String[], Long[]>() {
    public Long[] apply(String[] v) {
      return convertArray(v, new Long[v.length], TO_LONG);
    }
  };

  public static Function<String[], List<Long>> TO_LONG_LIST = 
    new Function<String[], List<Long>>() {
    public List<Long> apply(String[] v) {
      return Lists.newArrayList(TO_LONG_ARRAY.apply(v));
    }
  };

  public static Function<String[], Set<Long>> TO_LONG_SET = 
    new Function<String[], Set<Long>>() {
    public Set<Long> apply(String[] v) {
      return Sets.newHashSet(TO_LONG_ARRAY.apply(v));
    }
  };

  public static Function<String[], Float[]> TO_FLOAT_ARRAY = 
    new Function<String[], Float[]>() {
    public Float[] apply(String[] v) {
      return convertArray(v, new Float[v.length], TO_FLOAT);
    }
  };

  public static Function<String[], List<Float>> TO_FLOAT_LIST = 
    new Function<String[], List<Float>>() {
    public List<Float> apply(String[] v) {
      return Lists.newArrayList(TO_FLOAT_ARRAY.apply(v));
    }
  };

  public static Function<String[], Set<Float>> TO_FLOAT_SET = 
    new Function<String[], Set<Float>>() {
    public Set<Float> apply(String[] v) {
      return Sets.newHashSet(TO_FLOAT_ARRAY.apply(v));
    }
  };

  public static Function<String[], Double[]> TO_DOUBLE_ARRAY = 
    new Function<String[], Double[]>() {
    public Double[] apply(String[] v) {
      return convertArray(v, new Double[v.length], TO_DOUBLE);
    }
  };

  public static Function<String[], List<Double>> TO_DOUBLE_LIST = 
    new Function<String[], List<Double>>() {
    public List<Double> apply(String[] v) {
      return Lists.newArrayList(TO_DOUBLE_ARRAY.apply(v));
    }
  };

  public static Function<String[], Set<Double>> TO_DOUBLE_SET = 
    new Function<String[], Set<Double>>() {
    public Set<Double> apply(String[] v) {
      return Sets.newHashSet(TO_DOUBLE_ARRAY.apply(v));
    }
  };
}
