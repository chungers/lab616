// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.avro.generic.GenericData;
import org.apache.avro.util.Utf8;
import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.ib.client.EWrapper;
import com.lab616.common.Converter;
import com.lab616.common.Pair;
import com.lab616.ib.api.avro.TWSEvent;
import com.lab616.ib.api.avro.TWSField;
import com.lab616.ib.api.avro.TWSFieldType;
import com.lab616.ib.api.avro.TWSMethod;
import com.lab616.ib.api.proto.TWSProto;

/**
 * @author david
 *
 */
public class ApiBuilder {
  
  static Logger logger = Logger.getLogger(ApiBuilder.class);
  
  private TWSProto.Method method;
  private List<Column> columns = Lists.newArrayList();
  private int methodArgs = 0;
  public ApiBuilder(TWSProto.Method method) {
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
    Method m = EWrapper.class.getMethod(method.name(), getMethodArgs());
    if (m.getName().equals(getMethodName())) {
      return m;
    }
    throw new RuntimeException("Not matching method type: " + 
        method + " for " + m);
  }
  
  public String getMethodName() {
    return this.method.name();
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
  
  /**
   * For conversion of CSV data to proto format.
   * @param source The source identifier.
   * @param col Columns.
   * @return The proto.
   */
  public TWSProto.Event buildProto(String source, String[] col) {
    TWSProto.Event.Builder eb = TWSProto.Event.newBuilder()
    .setMethod(this.method)
    .setTimestamp(Long.decode(col[0]));
    if (source != null) {
      eb.setSource(source);
    }
    int i = 2;
    for (Column c: this.columns) {
      if (c.apiArg) {
        TWSProto.Field.Builder fb = TWSProto.Field.newBuilder();
        String value = col[i];
        // convert from value string to typed value.
        if (c.type == int.class) {
          fb.setIntValue(Converter.TO_INTEGER.apply(value)); 
        } else if (c.type == double.class) {
          fb.setDoubleValue(Converter.TO_DOUBLE.apply(value));
        } else if (c.type == long.class) {
          fb.setLongValue(Converter.TO_LONG.apply(value));
        } else if (c.type == String.class) {
          fb.setStringValue(Converter.TO_STRING.apply(value));
        } else if (c.type == boolean.class) {
          fb.setBooleanValue(Converter.TO_BOOLEAN.apply(value));
        }
        eb.addField(fb.build());
        i++;
      }
    }
    return eb.build();
  }
  
  public TWSProto.Event buildProto(String source, long timestamp, Object[] args) {
    TWSProto.Event.Builder eb = TWSProto.Event.newBuilder()
      .setMethod(this.method)
      .setTimestamp(timestamp);
    if (source != null) {
      eb.setSource(source);
    }
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
        } else if (value instanceof Long) {
          fb.setLongValue((Long) value);
        } else if (value instanceof Boolean) {
          fb.setBooleanValue((Boolean) value);
        }
        eb.addField(fb.build());
      }
    }
    return eb.build();
  }
  
  public Pair<Method, Object[]> buildArgs(TWSProto.Event event) 
    throws NoSuchMethodException {
    Object[] args = new Object[event.getFieldCount()];
    for (int i = 0; i < args.length; i++) {
      TWSProto.Field f = event.getField(i);
      if (f.hasDoubleValue()) {
        args[i] = f.getDoubleValue();
      }
      if (f.hasStringValue()) {
        args[i] = f.getStringValue();
      }
      if (f.hasIntValue()) {
        args[i] = f.getIntValue();
      }
      if (f.hasLongValue()) {
        args[i] = f.getLongValue();
      }
      if (f.hasBooleanValue()) {
        args[i] = f.getBooleanValue();
      }
    }
    return Pair.of(getApiMethod(), args);
  }


  public TWSEvent buildAvro(String source, long timestamp, Object[] args) {
    TWSEvent event = new TWSEvent();
    event.method = TWSMethod.valueOf(this.getMethodName());
    event.timestamp = timestamp;
    event.source = new Utf8(source);
    int i = 0;
    event.fields = new GenericData.Array<TWSField>(args.length, 
        (new TWSField()).getSchema());
    for (Column c : this.columns) {
      if (c.apiArg) {
        TWSField field = new TWSField();
        Object value = args[i++];
        if (value instanceof Integer) {
          field.intValue = ((Integer) value);
        } else if (value instanceof Double) {
          field.doubleValue = ((Double) value);
        } else if (value instanceof String) {
          field.stringValue = new Utf8((String) value);
        } else if (value instanceof Long) {
          field.longValue = ((Long) value);
        } else if (value instanceof Boolean) {
          field.booleanValue = ((Boolean) value);
        }
        event.fields.add(field);
      }
    }
    return event;
  }
  
}
