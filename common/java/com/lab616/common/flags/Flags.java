// 2009 lab616.com, All Rights Reserved.

package com.lab616.common.flags;

import static com.lab616.common.Converter.TO_BOOLEAN;
import static com.lab616.common.Converter.TO_BOOLEAN_ARRAY;
import static com.lab616.common.Converter.TO_BOOLEAN_LIST;
import static com.lab616.common.Converter.TO_BOOLEAN_SET;
import static com.lab616.common.Converter.TO_DOUBLE;
import static com.lab616.common.Converter.TO_DOUBLE_ARRAY;
import static com.lab616.common.Converter.TO_DOUBLE_LIST;
import static com.lab616.common.Converter.TO_DOUBLE_SET;
import static com.lab616.common.Converter.TO_FLOAT;
import static com.lab616.common.Converter.TO_FLOAT_ARRAY;
import static com.lab616.common.Converter.TO_FLOAT_LIST;
import static com.lab616.common.Converter.TO_FLOAT_SET;
import static com.lab616.common.Converter.TO_INTEGER;
import static com.lab616.common.Converter.TO_INTEGER_ARRAY;
import static com.lab616.common.Converter.TO_INTEGER_LIST;
import static com.lab616.common.Converter.TO_INTEGER_SET;
import static com.lab616.common.Converter.TO_LONG;
import static com.lab616.common.Converter.TO_LONG_ARRAY;
import static com.lab616.common.Converter.TO_LONG_LIST;
import static com.lab616.common.Converter.TO_LONG_SET;
import static com.lab616.common.Converter.TO_STRING;
import static com.lab616.common.Converter.TO_STRING_ARRAY;
import static com.lab616.common.Converter.TO_STRING_LIST;
import static com.lab616.common.Converter.TO_STRING_SET;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * Flags management.
 *
 * @author david
 *
 */
public class Flags {

  public interface Printable {
    public String getCodeLocation();
    public String getFlagName();
    public String getCurrentValue();
  }
  
  // I is String or String[], E is the element type.
  static class FlagParser<I, E> implements Printable {
    final Flag flag;
    final Field field;
    final Function<I, E> parser;
    FlagParser(Flag flag, Field field, Function<I, E> p) {
      this.flag = flag;
      this.field = field;
      this.parser = p;
    }
    
    protected String getOptionValue(CommandLine cmd) {
      String value = null;
      if (cmd != null) {
        value = cmd.getOptionValue(this.flag.name());
      } else if (!Flag.EMPTY_VALUE.equals(this.flag.defaultValue())){
        value = this.flag.defaultValue();
      }
      return value;
    }
    
    protected String[] getOptionValues(CommandLine cmd) {
      String[] values = null;
      if (cmd != null) {
        values = cmd.getOptionValues(this.flag.name());
      } else if (!Flag.EMPTY_VALUE.equals(this.flag.defaultValue())){
        values = this.flag.defaultValue().split(Flag.LIST_SEPARATOR.toString());
      }
      return values;
    }

    void apply(CommandLine cmd) throws Exception {
      String value = getOptionValue(cmd);
      if (value != null) {
        this.field.set(null, value);
      }
    }
    
    public final String getCodeLocation() {
      return field.getDeclaringClass().getName() + "." + field.getName();
    }
    
    public final String getFlagName() {
      return flag.name();
    }
    
    protected String format(Object state) {
      return state.toString();
    }
    
    public final String getCurrentValue() {
      Object v;
      try {
        v = field.get(null); 
      } catch (Exception e) {
        v = "exception(" + e.getMessage() + ")";
      }
      if (v == null) {
        return "null";
      } else {
        if (flag.privacy()) {
          return "XXXXXXXXXX";
        }
        return format(v);
      }
    }
    
    E convert(I input) {
      return this.parser.apply(input);
    }
    
    int getArgs() {
      return 1;
    }
    
    @SuppressWarnings("static-access")
    Option toOption() {
      return OptionBuilder
      .withArgName(flag.name())
      .withLongOpt(flag.name())
      .withDescription(flag.doc())
      .hasArgs(getArgs())
      .isRequired(flag.required())
      .withValueSeparator(Flag.LIST_SEPARATOR)
      .create();
    }
    
    @Override
    public String toString() {
      return String.format("f=%s,flag=%s,p=%s", field, flag, parser);
    }
  }
  
  static class ArrayFlagParser<E> extends FlagParser<String[], E[]> {
    ArrayFlagParser(Flag flag, Field field, Function<String[], E[]> p) {
      super(flag, field, p);
    }
    
    @Override
    void apply(CommandLine cmd) throws Exception {
      this.field.set(null, convert(getOptionValues(cmd)));
    }

    @Override
    int getArgs() {
      return Integer.MAX_VALUE;
    }
  }
  
  static class SingleFlagParser<E> extends FlagParser<String, E> {
    SingleFlagParser(Flag flag, Field field, Function<String, E> p) {
      super(flag, field, p);
    }
    
    @Override
    void apply(CommandLine cmd) throws Exception {
      this.field.set(null, convert(getOptionValue(cmd)));
    }
    
    @Override
    int getArgs() {
      return Integer.MAX_VALUE;
    }
  }
  
  static class ListFlagParser<E> extends FlagParser<String[], List<E>> {
    ListFlagParser(Flag flag, Field field, Function<String[], List<E>> p) {
      super(flag, field, p);
    }
    
    @Override
    void apply(CommandLine cmd) throws Exception {
      this.field.set(null, convert(getOptionValues(cmd)));
    }
    
    @Override
    int getArgs() {
      return Integer.MAX_VALUE;
    }
  }
  
  static class SetFlagParser<E> extends FlagParser<String[], Set<E>> {
    SetFlagParser(Flag flag, Field field, Function<String[], Set<E>> p) {
      super(flag, field, p);
    }
    
    @Override
    void apply(CommandLine cmd) throws Exception {
      this.field.set(null, convert(getOptionValues(cmd)));
    }
  }


  static List<FlagParser<?,?>> registered = Lists.newArrayList();
  static List<Printable> view = Lists.newArrayList();

  public static List<Printable> listAll() {
    return view;
  }
  
  /**
   * Registers a class whose public static variables are set by parsing
   * the command line flags.
   * 
   * @param clz  The class.
   */
  public static void register(Class<?> clz) {
    FlagParser<?, ?> desc = null;
    // Reflect on the class to look for all public static variables.
	  for (Field f : clz.getDeclaredFields()) {
	    Flag flag = f.getAnnotation(Flag.class);
	    int m = f.getModifiers();
	    if (flag != null && Modifier.isPublic(m) && Modifier.isStatic(m)) {
	      Type gType = f.getGenericType();
        if (f.getType().isArray()) {
          Class<?> ct = f.getType().getComponentType();

          if (ct.isPrimitive()) {
            throw new IllegalArgumentException(
                "Primitive arrays not supported: " + f + "@" + flag);
          }
          
          if (ct.equals(String.class)) {
            desc = new ArrayFlagParser<String>(flag, f, TO_STRING_ARRAY);
          } else if (ct.equals(Boolean.class)) {
            desc = new ArrayFlagParser<Boolean>(flag, f, TO_BOOLEAN_ARRAY);
          } else if (ct.equals(Integer.class)) {
            desc = new ArrayFlagParser<Integer>(flag, f, TO_INTEGER_ARRAY);
          } else if (ct.equals(Long.class)) {
            desc = new ArrayFlagParser<Long>(flag, f, TO_LONG_ARRAY);
          } else if (ct.equals(Float.class)) {
            desc = new ArrayFlagParser<Float>(flag, f, TO_FLOAT_ARRAY);
          } else if (ct.equals(Double.class)) {
            desc = new ArrayFlagParser<Double>(flag, f, TO_DOUBLE_ARRAY);
          }

          if (desc != null) {
            registered.add(desc);
            view.add(desc);
          }
        } else if (gType instanceof ParameterizedType && 
	          ((ParameterizedType)gType).getRawType().equals(List.class)) {
	        // Container such as List<T>
          Type eType = ((ParameterizedType)gType).getActualTypeArguments()[0];
          
          if (eType.equals(String.class)) {
            desc = new ListFlagParser<String>(flag, f, TO_STRING_LIST);
          } else if (eType.equals(Integer.class)) {
            desc = new ListFlagParser<Integer>(flag, f, TO_INTEGER_LIST);
          } else if (eType.equals(Long.class)) {
            desc = new ListFlagParser<Long>(flag, f, TO_LONG_LIST);
          } else if (eType.equals(Float.class)) {
            desc = new ListFlagParser<Float>(flag, f, TO_FLOAT_LIST);
          } else if (eType.equals(Boolean.class)) {
            desc = new ListFlagParser<Boolean>(flag, f, TO_BOOLEAN_LIST);
          } else if (eType.equals(Double.class)) {
            desc = new ListFlagParser<Double>(flag, f, TO_DOUBLE_LIST);
          }
          
          if (desc != null) {
            registered.add(desc);
            view.add(desc);
          }
	      } else if (gType instanceof ParameterizedType && 
            ((ParameterizedType)gType).getRawType().equals(Set.class)) {
          // Container such as List<T>
          Type eType = ((ParameterizedType)gType).getActualTypeArguments()[0];
          
          if (eType.equals(String.class)) {
            desc = new SetFlagParser<String>(flag, f, TO_STRING_SET);
          } else if (eType.equals(Integer.class)) {
            desc = new SetFlagParser<Integer>(flag, f, TO_INTEGER_SET);
          } else if (eType.equals(Long.class)) {
            desc = new SetFlagParser<Long>(flag, f, TO_LONG_SET);
          } else if (eType.equals(Float.class)) {
            desc = new SetFlagParser<Float>(flag, f, TO_FLOAT_SET);
          } else if (eType.equals(Boolean.class)) {
            desc = new SetFlagParser<Boolean>(flag, f, TO_BOOLEAN_SET);
          } else if (eType.equals(Double.class)) {
            desc = new SetFlagParser<Double>(flag, f, TO_DOUBLE_SET);
          }
          
          if (desc != null) {
            registered.add(desc);
            view.add(desc);
          }
        } else {
          if (gType.equals(String.class)) {
            desc = new SingleFlagParser<String>(flag, f, TO_STRING);
          } else if (gType.equals(Integer.class)) {
            desc = new SingleFlagParser<Integer>(flag, f, TO_INTEGER);
          } else if (gType.equals(Long.class)) {
            desc = new SingleFlagParser<Long>(flag, f, TO_LONG);
          } else if (gType.equals(Float.class)) {
            desc = new SingleFlagParser<Float>(flag, f, TO_FLOAT);
          } else if (gType.equals(Boolean.class)) {
            desc = new SingleFlagParser<Boolean>(flag, f, TO_BOOLEAN);
          } else if (gType.equals(Double.class)) {
            desc = new SingleFlagParser<Double>(flag, f, TO_DOUBLE);
          }
          
          if (desc != null) {
            registered.add(desc);
            view.add(desc);
          }
        }
	    }
	  }
	}
	
	/**
	 * Parses the command line arguments and sets all public static fields
	 * with the {@link Flag} annotation.
	 * 
	 * @param argv
	 * @throws Exception
	 */
  public static void parse(String[] argv) throws Exception {
	  // Build options
	  Options options = new Options();
	  for (FlagParser<?, ?> fp : registered) {
	    options.addOption(fp.toOption());
	  }
	  
	  CommandLineParser parser = new GnuParser();
	  CommandLine cmd = parser.parse(options, argv);

	  // Go through all the registered parsers
	  for (FlagParser<?, ?> fp : registered) {
	    if (cmd.hasOption(fp.flag.name())) {
	      fp.apply(cmd);
	    } else if (!Flag.EMPTY_VALUE.equals(fp.flag.defaultValue())) {
	      fp.apply(null);
	    }
	  }
	}
}
