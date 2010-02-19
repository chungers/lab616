// 2009-2010 lab616.com, All Rights Reserved.
package com.lab616.common;

import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.lab616.common.Converters.ValueRepository;

import junit.framework.TestCase;

/**
 * Tests for Converters.
 * @author david
 *
 */
public class ConvertersTest extends TestCase {

	public void testConversionFromStringToInteger() throws Exception {
		String input = "12345";
		assertEquals((Integer)12345, Converters.fromString(input, Integer.class));
		assertEquals((Integer)12345, Converters.fromString(input, int.class));
	}

	public void testConversionFromStringToFloat() throws Exception {
		String input = "12345.";
		assertEquals((Float)12345f, Converters.fromString(input, Float.class));
		assertEquals((float)12345f, Converters.fromString(input, float.class));
		assertEquals((Float)12345f, Converters.fromString(input, float.class));
	}

	public void testConversionFromStringToDouble() throws Exception {
		String input = "12345.999";
		assertEquals((Double)12345.999d, Converters.fromString(input, double.class));
		assertEquals((double)12345.999d, Converters.fromString(input, Double.class));
	}

	static class Test {
		// Test class.
	}
	
	static class Test2 extends Test {
		
	}
	
	public void testConversionFromStringToComplexType() throws Exception {
		String input = "this won't convert";
		try {
			Test test = new Test();
			assertEquals(test, Converters.fromString(input, Test.class, test));
			
		} catch (Exception e) {
			fail("Shouldn't throw exception.");
		}
	}
	
	public void testConversionFromStringToComplexWithRepo() throws Exception {
		String input = "key";
		Test val = new Test();
		Test2 test2 = new Test2();
		final Map<String, Test> map = Maps.newHashMap();
		map.put(input, val);
		map.put("another key", new Test());
		map.put("subclass", test2);
		
		try {
			assertEquals(val, Converters.fromString(input, Test.class, 
					new ValueRepository() {
				@Override
				public Object get(String key) {
					return map.get(key);
				}
			}));
		} catch (Exception e) {
			fail("Shouldn't throw exception.");
		}

		try {
			assertEquals(null, Converters.fromString("Bad key", Test.class, 
					new ValueRepository() {
				@Override
				public Object get(String key) {
					return map.get(key);
				}
			}));
		} catch (Exception e) {
			fail("Shouldn't throw exception.");
		}

		try {
			assertEquals(test2, Converters.fromString("subclass", Test.class, 
					new ValueRepository() {
				@Override
				public Object get(String key) {
					return map.get(key);
				}
			}));
		} catch (Exception e) {
			fail("Shouldn't throw exception for subclass.");
		}

		try {
			// key matches but there's type mismatch.
			assertEquals(null, Converters.fromString(input, Function.class, 
					new ValueRepository() {
				@Override
				public Object get(String key) {
					return map.get(key);
				}
			}));
			fail("Should throw exception.");
		} catch (ClassCastException e) {
		}
	}
}
