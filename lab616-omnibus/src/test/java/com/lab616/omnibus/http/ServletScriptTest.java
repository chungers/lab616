// 2009-2010 lab616.com, All Rights Reserved.
package com.lab616.omnibus.http;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import com.lab616.common.scripting.AbstractScriptingModule;
import com.lab616.common.scripting.ScriptObject;
import com.lab616.common.scripting.ScriptObject.ScriptModule;
import com.lab616.omnibus.Kernel;

/**
 * @author david
 *
 */
public class ServletScriptTest extends TestCase {

  static Logger logger = Logger.getLogger(ServletScriptTest.class);

  private static Kernel k;

  private static final String DEFAULT_STRING = "xyzwv";
  private static final String DEFAULT_COUNT = "9999";
  
  @ScriptModule(name = "TestModule1", doc = "TestModule1.help")
  @ServletScript(path = "/TestModule1")
  public static class TestModule1 extends ScriptObject {

    @ServletScript(path = "Command0")
    @Script(name = "Command0", doc = "Command0.help")
    public String command0() {
      return "NoArgs";
    }
    
    @ServletScript(path = "Command1")
    @Script(name = "Command1", doc = "Command1.help")
    public String command1(
        @Parameter(name="message", defaultValue=DEFAULT_STRING, doc="The message.") 
        String message) {
      return "Command1=" + message;
    }
    
    @ServletScript(path = "Command2")
    @Script(name = "Command2", doc = "Command2.help")
    public String command2(
        @Parameter(name="message", doc="The message.") 
        String message, 
        @Parameter(name="count", defaultValue=DEFAULT_COUNT, doc="The count.") 
        int count) {
      return "Command2=" + message + "," + count;
    }

    @ServletScript(path = "Command3")
    @Script(name = "Command3", doc = "Command3.help")
    public String command3(
        @Parameter(name="message", doc="The message.", defaultValue=DEFAULT_STRING) 
        String message, 
        @Parameter(name="count", defaultValue=DEFAULT_COUNT, doc="The count.") 
        int count,
        @Parameter(name="flag", defaultValue="FALSE", doc="The flag.") 
        boolean flag) {
      return "Command2=" + message + "," + count + "," + flag;
    }
  }
  
  static class TestGuiceModule extends AbstractScriptingModule {
    @Override
    protected void configure() {
      super.bind(TestModule1.class);
    }
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    if (k == null) {
      k = new Kernel(null,
        new TestGuiceModule()).runInThread(new String[]{});
    }

    while (!k.isRunning(5000L));
  }

  public void testCommand0() throws Exception {
    HttpGet get = new HttpGet("localhost", HttpServerModule.HTTP_PORT, "/TestModule1/Command0");
    String result = new String(get.fetch());
    String expected = (new TestModule1()).command0();
    assertEquals(expected.trim(), result.trim());
  }
  
  public void testCommand1() throws Exception {
    String test = "this Is A Test";
    HttpGet get = new HttpGet("localhost", HttpServerModule.HTTP_PORT, "/TestModule1/Command1")
      .add("message", test);
    String result = new String(get.fetch());
    String expected = (new TestModule1()).command1(test);
    assertEquals(expected.trim(), result.trim());
  }
  
  public void testCommand2() throws Exception {
    String test = "this Is A Test";
    int count = 8900;
    HttpGet get = new HttpGet("localhost", HttpServerModule.HTTP_PORT, "/TestModule1/Command2")
      .add("message", test).add("count", "" + count);
    String result = new String(get.fetch());
    String expected = (new TestModule1()).command2(test, count);
    assertEquals(expected.trim(), result.trim());
  }
  
  public void testCommand2DefaultArg() throws Exception {
    String test = "this Is A Test";
    HttpGet get = new HttpGet("localhost", HttpServerModule.HTTP_PORT, "/TestModule1/Command2")
      .add("message", test);
    String result = new String(get.fetch());
    String expected = (new TestModule1()).command2(test, Integer.parseInt(DEFAULT_COUNT));
    assertEquals(expected.trim(), result.trim());
  }

  public void testCommand3() throws Exception {
    String test = "this Is A Test";
    int count = 8900;
    boolean flag = true;
    HttpGet get = new HttpGet("localhost", HttpServerModule.HTTP_PORT, "/TestModule1/Command3")
      .add("message", test).add("count", "" + count).add("flag", ""+ flag);
    String result = new String(get.fetch());
    String expected = (new TestModule1()).command3(test, count, flag);
    assertEquals(expected.trim(), result.trim());
  }
  
  public void testCommand3DefaultArgs() throws Exception {
    int count = 8900;
    HttpGet get = new HttpGet("localhost", HttpServerModule.HTTP_PORT, "/TestModule1/Command3")
      .add("count", "" + count);
    String result = new String(get.fetch());
    String expected = (new TestModule1()).command3(DEFAULT_STRING, count, false);
    assertEquals(expected.trim(), result.trim());
  }
  
  public void testHang() throws Exception {
    while (k.isRunning(1000L)) {
      Thread.sleep(60000L);
    }
  }
}
