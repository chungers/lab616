// 2009 lab616.com, All Rights Reserved.

package com.lab616.monitoring;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.google.inject.internal.Lists;

import junit.framework.TestCase;

/**
 * @author david
 *
 */
public class VarzTest extends TestCase {

	@Varz(name = "varz3")
	public static Integer varz1;
	
	@Varz(name = "varz2")
	public static AtomicLong varz2 = new AtomicLong();
	
	@Varz(name = "varz1")
	public static List<AtomicInteger> varz3 = Lists.newArrayList();
	
	static {
		Varzs.export(VarzTest.class);
	}
	
	public void testExport() throws Exception {
		varz1 = 1;
		varz2.set(2L);
		
		AtomicInteger v = new AtomicInteger();
		v.set(3);
		varz3.add(v);
		AtomicInteger v2 = new AtomicInteger();
		v2.set(4);
		varz3.add(v2);
		
		System.out.println("got = " + Lists.newArrayList(Varzs.getValues()));
		
		List<String> expected = Lists.newArrayList(
				"varz1=" + varz3.get(0) + "," + varz3.get(1), 
				"varz2=" + varz2.toString(), 
				"varz3=" + varz1.toString());
		
		assertEquals(expected, Lists.newArrayList(Varzs.getValues()));
	}
}
