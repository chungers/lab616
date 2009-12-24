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

  public static ApiBuilder CURRENT_TIME = new ApiBuilder(
      TWSProto.Method.currentTime)
  .parse("timestamp", Long.class)
  .parse("method", String.class)
  .apiArg("time", long.class).done();

  public static ApiBuilder UPDATE_ACCT_VALUE = new ApiBuilder(
      TWSProto.Method.updateAccountValue)
  .parse("timestamp", Long.class)
  .parse("method", String.class)
  .apiArg("key", String.class)
  .apiArg("value", String.class)
  .apiArg("currency", String.class)
  .apiArg("accountName", String.class).done();

  public static ApiBuilder NEXT_VALID_ID = new ApiBuilder(
      TWSProto.Method.nextValidId)
  .parse("timestamp", Long.class)
  .parse("method", String.class)
  .apiArg("nextId", int.class).done();

  public static ApiBuilder HISTORICAL_DATA = new ApiBuilder(
      TWSProto.Method.historicalData)
  .parse("timestamp", Long.class)
  .parse("method", String.class)
  .apiArg("tickerId", int.class)
  .apiArg("date", String.class)
  .apiArg("open", double.class)
  .apiArg("high", double.class)
  .apiArg("low", double.class)
  .apiArg("close", double.class)
  .apiArg("volume", int.class)
  .apiArg("count", int.class)
  .apiArg("wap", double.class)
  .apiArg("hasGaps", boolean.class).done();

  public static ApiBuilder REALTIME_BAR = new ApiBuilder(
      TWSProto.Method.realtimeBar)
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

  public static ApiBuilder TICK_GENERIC = new ApiBuilder(
      TWSProto.Method.tickGeneric)
  .parse("timestamp", Long.class)
  .parse("method", String.class)
  .apiArg("tickerId", int.class)
  .apiArg("tickType", int.class)
  .apiArg("value", double.class).done();

  public static ApiBuilder TICK_PRICE = new ApiBuilder(
      TWSProto.Method.tickPrice)
  .parse("timestamp", Long.class)
  .parse("method", String.class)
  .apiArg("tickerId", int.class)
  .apiArg("field", int.class)
  .apiArg("price", double.class)
  .apiArg("canAutoExecute", int.class).done();

  public static ApiBuilder TICK_SIZE = new ApiBuilder(
      TWSProto.Method.tickSize)
  .parse("timestamp", Long.class)
  .parse("method", String.class)
  .apiArg("tickerId", int.class)
  .apiArg("field", int.class)
  .apiArg("size", int.class).done();

  public static ApiBuilder TICK_STRING = new ApiBuilder(
      TWSProto.Method.tickString)
  .parse("timestamp", Long.class)
  .parse("method", String.class)
  .apiArg("tickerId", int.class)
  .apiArg("tickType", int.class)
  .apiArg("value", String.class).done();

  public static ApiBuilder MKT_DEPTH = new ApiBuilder(
      TWSProto.Method.updateMktDepth)
  .parse("timestamp", Long.class)
  .parse("method", String.class)
  .apiArg("tickerId", int.class)
  .apiArg("position", int.class)
  .apiArg("operation", int.class)
  .apiArg("side", int.class)
  .apiArg("price", double.class)
  .apiArg("size", int.class).done();

  public static ApiBuilder ERROR = new ApiBuilder(
      TWSProto.Method.error)
  .parse("timestamp", Long.class)
  .parse("method", String.class)
  .apiArg("id", int.class)
  .apiArg("errorCode", int.class)
  .apiArg("errorString", String.class).done();

  static Map<String, Pair<Method, ApiBuilder>> methods = Maps.newHashMap();

  public static ApiBuilder get(String method) {
    if (methods.containsKey(method)) {
      return methods.get(method).second;
    }
    return null;
  }

  static void register(ApiBuilder b) throws NoSuchMethodException {
    methods.put(b.getMethodName(), 
        new Pair<Method, ApiBuilder>(b.getApiMethod(), b));
  }

  static {
    try {
      register(CURRENT_TIME);
      register(REALTIME_BAR);
      register(UPDATE_ACCT_VALUE);
      register(NEXT_VALID_ID);
      register(HISTORICAL_DATA);
      register(TICK_GENERIC);
      register(TICK_PRICE);
      register(TICK_SIZE);
      register(TICK_STRING);
      register(MKT_DEPTH);
      register(ERROR);
      
      logger.info("ApiMethods=" + methods);
    } catch (NoSuchMethodException e) {
      logger.fatal("No such method: ", e);
    }
  }
}
