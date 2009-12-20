// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api.builders;

import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import com.ib.client.Contract;
import com.lab616.common.builder.AbstractBuilder;
import com.lab616.common.builder.Builder;
import com.lab616.common.builder.BuilderException;

/**
 * @author david
 *
 */
public class ContractBuilderTest extends TestCase {

  static AtomicInteger called = new AtomicInteger();
  
  public static class Test {
    public void setFoo(String foo) {
      called.incrementAndGet();
    }
  }
  public void testBuilder() throws Exception {
    Contract contract = new ContractBuilder("GOOG").build();

    assertEquals("GOOG", contract.m_symbol);
    assertEquals("STK", contract.m_secType);
    assertEquals("USD", contract.m_currency);
    assertEquals("SMART", contract.m_exchange);
  }
  
  public void testUnknownField() throws Exception {
    try {
      new ContractBuilder("GOOG").setProperty("blah").to("foo").build();
      fail("Expected exception.");
    } catch (BuilderException e) {
      // ok.
    }
    
    Builder<Test> b = new AbstractBuilder<Test>(Test.class) { };
    
    b.setProperty("foo", String.class).to("bar").build();
    assertEquals(1, called.get());
      
    b.set("foo", "bar").build();
    assertEquals(2, called.get());
  }
}
