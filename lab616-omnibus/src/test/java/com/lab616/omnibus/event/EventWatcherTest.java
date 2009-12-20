// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.event;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import com.espertech.esper.client.EPStatement;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.lab616.omnibus.event.annotation.Pattern;
import com.lab616.omnibus.event.annotation.Statement;
import com.lab616.omnibus.event.annotation.Var;

/**
 * @author david
 *
 */
public class EventWatcherTest extends TestCase {

	static final String TEST_STMT = "select * from blah where p = ? and y = ?";
	@Statement(TEST_STMT)
	static class TestWatcher extends AbstractEventWatcher {
		
		@Var(1)
		public String param1;
		
		@Var(2)
		public int param2;
	}
	
	public void testStatement() throws Exception {
		TestWatcher t1 = new TestWatcher();
		final AtomicInteger invoked = new AtomicInteger(0);
		t1.buildFromAnnotations(
				new Function<Statement, EPStatement>() {
					public EPStatement apply(Statement s) {
						assertEquals(TEST_STMT, s.value());
						invoked.incrementAndGet();
						return null;
					}
				}, 
				new Function<Pattern, EPStatement>() {
					public EPStatement apply(Pattern p) {
						fail();
						return null;
					}
				});
		assertEquals(1, invoked.get());
		
		t1.param1 = "Test";
		t1.param2 = 1000;
		
		Map<Integer, Function<AbstractEventWatcher,Object>> view = 
		  t1.getParamsFromAnnotations();
		
		{
			List<Integer> expected = Lists.newArrayList(1, 2);
			List<Integer> found = Lists.newArrayList(view.keySet());
			assertEquals(expected, found);
		}
		{
			List<Object> expected = Lists.newArrayList((Object)t1.param1, t1.param2);
			List<Object> found = Lists.newArrayList();
			for (Function<AbstractEventWatcher,Object> f : view.values()) {
				found.add(f.apply(t1));
			}
			assertEquals(expected, found);
		}
	}


	static final String TEST_PTTN = "every blah where a = ? and b = ?";
	@Pattern(TEST_PTTN)
	static class PatternWatcher extends AbstractEventWatcher {
		
		private String param1;
		private boolean param2;
		
		@Var(10)
		public String getParam1() {
			return param1;
		}
		
		@Var(20)
		public boolean param2() {
			return param2;
		}
	}
	
	public void testPattern() throws Exception {
		final AtomicInteger invoked = new AtomicInteger(0);
		PatternWatcher p1 = new PatternWatcher();
		invoked.set(0);
		p1.buildFromAnnotations(
				new Function<Statement, EPStatement>() {
					public EPStatement apply(Statement s) {
						fail();
						return null;
					}
				}, 
				new Function<Pattern, EPStatement>() {
					public EPStatement apply(Pattern p) {
						assertEquals(TEST_PTTN, p.value());
						invoked.incrementAndGet();
						return null;
					}
				});
		assertEquals(1, invoked.get());
		p1.param1 = "Test";
		p1.param2 = false;
		
		Map<Integer, Function<AbstractEventWatcher,Object>> view = p1.getParamsFromAnnotations();
		{
			List<Integer> expected = Lists.newArrayList(10, 20);
			List<Integer> found = Lists.newArrayList(view.keySet());
			assertEquals(expected, found);
		}
		{
			List<Object> expected = Lists.newArrayList((Object)p1.param1, p1.param2);
			List<Object> found = Lists.newArrayList();
			for (Function<AbstractEventWatcher,Object> f : view.values()) {
				found.add(f.apply(p1));
			}
			assertEquals(expected, found);
		}
	}
}
