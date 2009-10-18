// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api;

import java.lang.reflect.Method;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.common.collect.Maps;
import com.lab616.common.Pair;
import com.lab616.ib.api.proto.TWSProto;

/**
 * @author david
 *
 */
public class ApiMethods {

  static Logger logger = Logger.getLogger(ApiMethods.class);
  
  public static ApiBuilder HISTORICAL_DATA = new ApiBuilder(TWSProto.Method.historicalData)
  .parse("timestamp", Long.class)
  .parse("method", String.class)
  .apiArg("tickerId", int.class)
  .apiArg("date", String.class)
  .apiArg("open", double.class)
  .apiArg("high", double.class)
  .apiArg("low", double.class)
  .apiArg("close", double.class)
  .apiArg("volume", int.class)
  .apiArg("wap", double.class)
  .apiArg("hasGaps", boolean.class).done();
  
  public static ApiBuilder REALTIME_BAR = new ApiBuilder(TWSProto.Method.realtimeBar)
  .parse("timestamp", Long.class)
  .parse("method", String.class)
  .apiArg("tickerId", int.class)
  .apiArg("time", long.class)
  .apiArg("open", double.class)
  .apiArg("high", double.class)
  .apiArg("low", double.class)
  .apiArg("close", double.class)
  .apiArg("volume", long.class)
  .apiArg("wap", double.class)
  .apiArg("count", int.class).done();

  public static ApiBuilder TICK_GENERIC = new ApiBuilder(TWSProto.Method.tickGeneric)
  .parse("timestamp", Long.class)
  .parse("method", String.class)
  .apiArg("tickerId", int.class)
  .apiArg("tickType", int.class)
  .apiArg("value", double.class).done();
  
  public static ApiBuilder TICK_PRICE = new ApiBuilder(TWSProto.Method.tickPrice)
  .parse("timestamp", Long.class)
  .parse("method", String.class)
  .apiArg("tickerId", int.class)
  .apiArg("field", int.class)
  .apiArg("price", double.class)
  .apiArg("canAutoExecute", int.class).done();

  public static ApiBuilder TICK_SIZE = new ApiBuilder(TWSProto.Method.tickSize)
  .parse("timestamp", Long.class)
  .parse("method", String.class)
  .apiArg("tickerId", int.class)
  .apiArg("field", int.class)
  .apiArg("size", int.class).done();
  
  public static ApiBuilder TICK_STRING = new ApiBuilder(TWSProto.Method.tickString)
  .parse("timestamp", Long.class)
  .parse("method", String.class)
  .apiArg("tickerId", int.class)
  .apiArg("tickType", int.class)
  .apiArg("value", String.class).done();

  public static ApiBuilder MKT_DEPTH = new ApiBuilder(TWSProto.Method.updateMktDepth)
  .parse("timestamp", Long.class)
  .parse("method", String.class)
  .apiArg("tickerId", int.class)
  .apiArg("position", int.class)
  .apiArg("operation", int.class)
  .apiArg("side", int.class)
  .apiArg("price", double.class)
  .apiArg("size", int.class).done();

  static Map<String, Pair<Method, ApiBuilder>> methods = Maps.newHashMap();
  
  public static ApiBuilder get(String method) {
    return methods.get(method).second;
  }
  
  static void register(ApiBuilder b) throws NoSuchMethodException {
    methods.put(b.getMethodName(), 
        new Pair<Method, ApiBuilder>(b.getApiMethod(), b));
  }

  static {
    try {
      register(REALTIME_BAR);
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
