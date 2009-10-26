// 2009 lab616.com, All Rights Reserved.

package com.lab616.common.flags;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.junit.Test;

import com.google.common.collect.Lists;


/**
 * Tests for flags.
 * 
 * @author david
 */
public class FlagTest extends TestCase {

	public static class TestClass {
		
		@Flag(name="string", doc="Sets the value of string.")
		public static String string = "def";
	
    @Flag(name="string_array", doc="Sets the value of string array.")
    public static String[] string_array;

    @Flag(name="string_list", required=true)
		public static List<String> required_string_list;
		
		@Flag(name="integer_array")
		public static Integer[] integer_array;
		
    @Flag(name="long_set")
    public static Set<Long> long_set;
    
		@Flag(name="trap1")
		private static String trap1;
		
		@Flag(name="trap2")
		public String trap2;
		
		public static String trap3;
	}
	
	@Test
	public void testBasicUsage() throws Exception {

	  boolean exception = false;
	  
		Flags.register(TestClass.class);

		///////////////////////////////////////////////
    exception = false;
		try {
	    Flags.parse("".split(" "));
		} catch (MissingOptionException e) {
		  exception = true;
		}
		assertTrue(exception);
		
    ///////////////////////////////////////////////
		exception = false;
    try {
      Flags.parse("--string_list=a,b,c".split(" "));
    } catch (MissingOptionException e) {
      exception = true;
    }
    assertFalse(exception);
    assertEquals("def", TestClass.string);
    assertEquals(Lists.newArrayList("a", "b", "c"),
        TestClass.required_string_list);
    assertNull(TestClass.trap1);
		assertNull(TestClass.trap3);
		
    ///////////////////////////////////////////////
    exception = false;
    try {
      Flags.parse("--string_array=aa,bb,cc --string_list=x".split(" "));
    } catch (Exception e) {
      e.printStackTrace();
      exception = true;
    }
    assertFalse(exception);
    assertEquals(newArrayList("aa", "bb", "cc"), 
        newArrayList(TestClass.string_array));
    assertNull(TestClass.trap1);
    assertNull(TestClass.trap3);
    
    ///////////////////////////////////////////////
    exception = false;
    try {
      Flags.parse("--string_list=a --integer_array=1,2,3".split(" "));
    } catch (MissingOptionException e) {
      exception = true;
    }
    assertFalse(exception);
    assertEquals(newArrayList("a"), TestClass.required_string_list);
    assertEquals(newArrayList(1, 2, 3), newArrayList(TestClass.integer_array));

    ///////////////////////////////////////////////
    exception = false;
    try {
      Flags.parse("--string_list=a --string=STRING".split(" "));
    } catch (MissingOptionException e) {
      exception = true;
    }
    assertFalse(exception);
    assertEquals(newArrayList("a"), TestClass.required_string_list);
    assertEquals("STRING", TestClass.string);

    ///////////////////////////////////////////////
    exception = false;
    try {
      Flags.parse(" --required_string_list=a --bgous".split(" "));
    } catch (UnrecognizedOptionException e) {
      exception = true;
    }
    assertTrue(exception);
	}

	private CommandLine parse(String arg, Option... options) throws Exception {
    Options optionSettings = new Options();
    for (Option opt : options) {
      optionSettings.addOption(opt);
    }
    CommandLineParser parser = new GnuParser();
    return parser.parse(optionSettings, arg.split(" "));
	}
	
	
	@SuppressWarnings("static-access")
  @Test
	public void testCli() throws Exception {
    Option opt = new Option("foo", "foo", true, "To set foo.");
    opt.setRequired(true);
    opt.setArgs(Integer.MAX_VALUE);
    opt.setValueSeparator(',');
    
    CommandLine cmd;
    boolean exception = false;
    
    /////////////////////////////////////////////////
    cmd = parse("--foo=fooValue", opt);
    assertTrue(cmd.hasOption("foo"));
    assertEquals("fooValue", cmd.getOptionValue("foo"));

    /////////////////////////////////////////////////
    exception = false;
    try {
      cmd = parse("--bar=fooValue", opt);
    } catch (UnrecognizedOptionException e) {
      exception = true;
    }
    assertTrue(exception);
    
    /////////////////////////////////////////////////
    exception = false;
    try {
      cmd = parse("--bar=fooValue,value2 --foo=optional", 
          OptionBuilder.withArgName("foo").withLongOpt("foo")
          .hasArgs().create(),
          OptionBuilder.withArgName("bar").withLongOpt("bar")
            .hasArgs()
            .isRequired()
            .withValueSeparator(',').create());
    } catch (Exception e) {
      exception = true;
    }
    assertFalse(exception);
    assertEquals(null, cmd.getOptionValue("string"));
    assertEquals("optional", cmd.getOptionValue("foo"));
    assertEquals(2, cmd.getOptionValues("bar").length);
    assertEquals("fooValue", cmd.getOptionValues("bar")[0]);
    assertEquals("value2", cmd.getOptionValues("bar")[1]);
	}
	
	@Test
	public void testMatch() throws Exception {
	  assertTrue("Yes".matches("Yes|OK"));
	  assertTrue("YES".matches("Yes|YES|OK"));
	  assertTrue("Trader Workstation".matches("Trader Workstation.*"));
    assertTrue("IB Trader Workstation".matches(".*Trader Workstation.*"));
    assertTrue("Login failed: Invalid username etc.".matches("^Login failed.*"));
	}
}
