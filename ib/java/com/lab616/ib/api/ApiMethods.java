// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api;

import java.lang.reflect.Method;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.common.collect.Maps;

/**
 * @author david
 *
 */
public class ApiMethods {

  static Logger logger = Logger.getLogger(ApiMethods.class);
  
  public static ApiBuilder TICK_GENERIC = new ApiBuilder("tickGeneric")
  .parse("timestamp", Long.class)
  .parse("method", String.class)
  .apiArg("tickerId", int.class)
  .apiArg("tickType", int.class)
  .apiArg("value", double.class).done();
  
  public static ApiBuilder TICK_PRICE = new ApiBuilder("tickPrice")
  .parse("timestamp", Long.class)
  .parse("method", String.class)
  .apiArg("tickerId", int.class)
  .apiArg("field", int.class)
  .apiArg("price", double.class)
  .apiArg("canAutoExecute", int.class).done();

  public static ApiBuilder TICK_SIZE = new ApiBuilder("tickSize")
  .parse("timestamp", Long.class)
  .parse("method", String.class)
  .apiArg("tickerId", int.class)
  .apiArg("field", int.class)
  .apiArg("size", int.class).done();
  
  public static ApiBuilder TICK_STRING = new ApiBuilder("tickString")
  .parse("timestamp", Long.class)
  .parse("method", String.class)
  .apiArg("tickerId", int.class)
  .apiArg("tickType", int.class)
  .apiArg("value", String.class).done();

  public static ApiBuilder MKT_DEPTH = new ApiBuilder("updateMktDepth")
  .parse("timestamp", Long.class)
  .parse("method", String.class)
  .apiArg("tickerId", int.class)
  .apiArg("position", int.class)
  .apiArg("operation", int.class)
  .apiArg("side", int.class)
  .apiArg("price", double.class)
  .apiArg("size", int.class).done();

  static Map<String, Method> methods = Maps.newHashMap();
  
  static void register(ApiBuilder b) throws NoSuchMethodException {
    methods.put(b.getMethodName(), b.getApiMethod());
  }

  static {
    try {
      register(TICK_GENERIC);
      register(TICK_PRICE);
      register(TICK_SIZE);
      register(TICK_STRING);
      register(MKT_DEPTH);
    } catch (NoSuchMethodException e) {
      logger.fatal("No such method: ", e);
    }
  }
}
