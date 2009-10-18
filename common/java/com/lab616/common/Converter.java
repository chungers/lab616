// 2009 lab616.com, All Rights Reserved.

package com.lab616.common;

import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Parser from String to some type.
 *
 * @author david
 *
 */
public class Converter {
  
  public static Function<String, String> TO_STRING = 
    new Function<String, String>() {
    public String apply(String v) {
      return v;
    }
  };
  
  public static Function<String, Boolean> TO_BOOLEAN = 
    new Function<String, Boolean>() {
    public Boolean apply(String v) {
      return Boolean.parseBoolean(v);
    }
  };

  public static Function<String, Integer> TO_INTEGER = 
    new Function<String, Integer>() {
    
    public Integer apply(String v) {
      return Integer.decode(v);
    }
  };

  public static Function<String, Long> TO_LONG = new Function<String, Long>() {
    
    public Long apply(String v) {
      return Long.decode(v);
    }
  };

  public static Function<String, Float> TO_FLOAT = new Function<String, Float>() {
    
    public Float apply(String v) {
      return Float.parseFloat(v);
    }
  };

  public static Function<String, Double> TO_DOUBLE = new Function<String, Double>() {
    
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

  public static Function<String[], boolean[]> TO_P_BOOLEAN_ARRAY = 
    new Function<String[], boolean[]>() {
    
    public boolean[] apply(String[] v) {
      boolean[] result = new boolean[v.length];
      for (int i = 0; i < result.length; i++) {
        result[i] = TO_BOOLEAN.apply(v[i]);
      }
      return result; 
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
