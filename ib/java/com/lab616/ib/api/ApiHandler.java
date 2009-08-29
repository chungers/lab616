// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * API handler
 *
 * @author david
 */
public class ApiHandler implements InvocationHandler {

  static Map<String, Handler> handlers = Maps.newHashMap();
  
  interface Handler {
    public void receive(Object[] args);
  }
  
  @Override
  public Object invoke(Object proxy, Method method, Object[] args)
      throws Throwable {
    if (handlers.get(method.getName()) != null) {
      handlers.get(method.getName()).receive(args);
    }
    return null;
  }

  static void print(String method, Object[] args) {
    StringBuffer buff = new StringBuffer(method);
    for (Object o : args) {
      buff.append(",");
      buff.append(o.toString());
    }
    System.out.println(buff.toString());
  }
  
  static {
    handlers.put("tickPrice", new Handler() {
      @Override
      public void receive(Object[] args) {
        print("tickPrice", args);
      }
    });
    handlers.put("tickSize", new Handler() {
      @Override
      public void receive(Object[] args) {
        print("tickSize", args);
      }
    });
    handlers.put("tickGeneric", new Handler() {
      @Override
      public void receive(Object[] args) {
        print("tickGeneric", args);
      }
    });
    handlers.put("tickString", new Handler() {
      @Override
      public void receive(Object[] args) {
        print("tickString", args);
      }
    });
  }
}
