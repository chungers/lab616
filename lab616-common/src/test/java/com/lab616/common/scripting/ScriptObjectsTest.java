/**
 * 
 */
package com.lab616.common.scripting;

import junit.framework.TestCase;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Module;

/**
 * @author david
 *
 */
public class ScriptObjectsTest extends TestCase {

	@ScriptObject(name = "Test", doc = "test command")
	static class TestCommand {
		
		public String command1(String message) {
			return "Hello " + message;
		}

		public String command2(String message, int count) {
			return "Hello again: " + message + " with count = " + count;
		}
	}
	
	@ScriptObject(name = "Test", doc = "duplicate name for test command")
	static class TestCommand2 {
		
		public String command1(String message) {
			return "Hello " + message;
		}
	}

	@ScriptObject(name = "Test3", doc = "another command script object.")
	static class TestCommand3 {
		
		public String command3(String message) {
			return "Hello " + message;
		}
	}
	/**
	 * Normal use case.
	 * @throws Exception
	 */
	public void testBinding() throws Exception {
		// Bind the command in Guice.
		Guice.createInjector(
				new ScriptingModule(),
				new Module() {
			public void configure(Binder binder) {
				ScriptObjects.with(binder)
					.bind(TestCommand.class);
			}
		});
		
		assertTrue(ScriptObjects.exists("Test"));
		
		System.out.println(ScriptObjects.list());
		
		TestCommand tc = (TestCommand)ScriptObjects.load("Test");
		assertNotNull(tc);
	}

	/**
	 * Expects exception to be thrown because of duplicate names.
	 * @throws Exception
	 */
	public void testDuplicateCommands() throws Exception {
		// Bind the command in Guice.
		Exception ex = null;
		try {
			Guice.createInjector(
					new ScriptingModule(),
					new Module() {
				public void configure(Binder binder) {
					ScriptObjects.with(binder)
						.bind(TestCommand.class)
						.bind(TestCommand2.class);
				}
			});
			
		} catch (Exception e) {
			ex = e;
		}
		assertNotNull(ex);
	}
}
