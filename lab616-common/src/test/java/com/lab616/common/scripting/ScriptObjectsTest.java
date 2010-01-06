/**
 * 
 */
package com.lab616.common.scripting;

import com.lab616.common.scripting.ScriptObject.Script;
import junit.framework.TestCase;

import com.google.inject.Guice;
import com.lab616.common.scripting.ScriptObject.ScriptModule;

/**
 * @author david
 *
 */
public class ScriptObjectsTest extends TestCase {

  @ScriptModule(name = "TestCommand", doc = "test command")
	static class TestCommand extends ScriptObject {
  	@Script(name = "Command1", doc = "test command")
		public String command1(String message) {
			return "Hello " + message;
		}
  	@Script(name = "Command2", doc = "test command")
		public String command2(String message, int count) {
			return "Hello again: " + message + " with count = " + count;
		}
	}
	
  @ScriptModule(name = "TestCommand", doc = "test command")
	static class TestCommand2 extends ScriptObject {
  	@Script(name = "TestCommand1", doc = "duplicate name for test command")
		public String command1(String message) {
			return "Hello " + message;
		}
	}

  @ScriptModule(name = "TestCommand3", doc = "test command")
	static class TestCommand3 extends ScriptObject {
  	@Script(name = "TestCommand3", doc = "another command script object.")
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
    ScriptObjects scripts = Guice.createInjector(
      new ScriptingModule() {
        @Override
        public void configure() {
          bind(TestCommand.class);
        }
      }).getInstance(ScriptObjects.class);

    ScriptObject tc1 = scripts.load("TestCommand");
		assertNotNull(tc1);
    ScriptObject tc2 = scripts.load("TestCommand.Command1");
		assertNotNull(tc2);
    ScriptObject tc3 = scripts.load("TestCommand.Command2");
		assertNotNull(tc3);
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
        new ScriptingModule() {

          @Override
          public void configure() {
            bind(TestCommand.class);
            bind(TestCommand2.class);
          }
        });

    } catch (Exception e) {
      ex = e;
    }
    assertNotNull(ex);

  }
}
