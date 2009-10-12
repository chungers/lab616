// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api;

import java.lang.reflect.Method;
import java.util.List;

import com.google.common.collect.Lists;
import com.ib.client.EWrapper;
import com.lab616.common.Pair;
import com.lab616.ib.api.proto.TWSProto;

/**
 * @author david
 *
 */
public class ApiBuilder {
  private String method;
  private List<Column> columns = Lists.newArrayList();
  private int methodArgs = 0;
  public ApiBuilder(String method) {
    this.method = method;
  }
  
  public class Column {
    String name;
    Class<?> type;
    boolean apiArg = false;
    
    public Column(String n, Class<?> t) {
      name = n;
      type = t;
      ApiBuilder.this.columns.add(this);
    }
    
    public Column setApiArg(boolean b) {
      this.apiArg = b;
      ApiBuilder.this.methodArgs++;
      return this;
    }
    
    public Column parse(String n, Class<?> type) {
      return new Column(n, type);
    }

    public Column apiArg(String n, Class<?> type) {
      return new Column(n, type).setApiArg(true);
    }
    
    public ApiBuilder done() {
      return ApiBuilder.this;
    }
  }
  
  public Method getApiMethod() throws NoSuchMethodException {
    return EWrapper.class.getMethod(method, getMethodArgs());
  }
  
  public String getMethodName() {
    return this.method;
  }
  
  public Class<?>[] getMethodArgs() {
    Class<?>[] args = new Class<?>[methodArgs];
    int i = 0;
    for (Column c : columns) {
      if (c.apiArg) {
        args[i++] = c.type;
      }
    }
    return args;
  }
  
  public Column parse(String name, Class<?> type) {
    return new Column(name, type);
  }
  
  public Column apiArg(String name, Class<?> type) {
    return new Column(name, type).setApiArg(true);
  }
  
  public TWSProto.Event buildProto(TWSEvent event) {
    if (this.method.equals(event.getMethod())) {
      return buildProto(event.getTimestamp(), event.getArgs());
    }
    return null;
  }
  
  public TWSProto.Event buildProto(long timestamp, Object[] args) {
    TWSProto.Event.Builder eb = TWSProto.Event.newBuilder()
      .setMethod(TWSProto.Method.valueOf(this.method))
      .setTimestamp(timestamp);
    int i = 0;
    for (Column c : this.columns) {
      if (c.apiArg) {
        TWSProto.Field.Builder fb = TWSProto.Field.newBuilder();
        Object value = args[i++];
        if (value instanceof Integer) {
          fb.setIntValue((Integer) value);
        } else if (value instanceof Double) {
          fb.setDoubleValue((Double) value);
        } else if (value instanceof String) {
          fb.setStringValue((String) value);
        }
        eb.addFields(fb.build());
      }
    }
    return eb.build();
  }
  
  public Pair<Method, Object[]> buildArgs(TWSProto.Event event) 
    throws NoSuchMethodException {
    Object[] args = new Object[event.getFieldsCount()];
    for (int i = 0; i < args.length; i++) {
      TWSProto.Field f = event.getFields(i);
      if (f.hasDoubleValue()) {
        args[i] = f.getDoubleValue();
      }
      if (f.hasStringValue()) {
        args[i] = f.getStringValue();
      }
      if (f.hasIntValue()) {
        args[i] = f.getIntValue();
      }
    }
    return Pair.of(getApiMethod(), args);
  }
}
