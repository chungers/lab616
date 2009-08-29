// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api;


import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;
import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 *
 * @author david
 *
 */
public class ApiTest {

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
  }

  static int OFFSET = Character.getNumericValue('a') - 1;
  static int FRAME_SIZE = 5;  // 0-31 for 26 alphabets so 5 bits are required.
  static int FRAMES = 6;
  
  // BINARY FORMAT from MSB to LSB.
  // For an unsigned 32 bit int, we support up to 4 letter symbol (e.g.
  // AAPL or those on NASDAQ, 2 bits delimiter, 1 expiration month code, and
  // a 1 char strike code, per standard option symbology.
  // Writing: start with first letter of symbol, write from LSB toward MSB.
  // Reading: start with LSB and read toward MSB and append to buffer.
  // 
  // [strike] [expiration] [delimiter] [symbol(N)][symbol(N-1)]...[symbol[0]]
  // where
  // [strike] = 1 char for the strike price, per standard option symbol rules
  // [expiration] = 1 char for the expiration month, per option symbol rules.
  // [delimiter] = 2 bytes. 0x3 indicates option code following. 0x0 for none.
  // [symbol(N)] = Nth char of the ticker symbol.
  // [symbol(0)] = First char of the ticker symbol.
  
  
  static int getTickerId(String symbol) {
    int size = symbol.length();
    if (size > FRAMES) {
      throw new IllegalArgumentException("Symbol size exceeds representation.");
    }

    int tickerId = 0;
    for (int i = 0; i < size; i++) {
      int c = symbol.charAt(i) - 'A' + 1;
      tickerId += Math.max(0, c) << (FRAME_SIZE * i);
    }
    return tickerId;
  }
  
  static String fromTickerId(int tickerId) {
    StringBuffer symbol = new StringBuffer();
    for (int i = 0; i < FRAMES; i++) {
      int mask = ((1 << FRAME_SIZE) - 1) << (FRAME_SIZE * i);
      int v = (tickerId & mask) >> (FRAME_SIZE * i);
      if (v > 0) {
        symbol.append((char)('A' + (v - 1)));
      }
    }
    return symbol.toString();
  }
  
  @Test
  public void testStockSymbol() throws Exception {
    Stock goog = new Stock("GOOG");
    Stock goog2 = new Stock(goog.getTickerId());
    assertEquals(goog.getTickerId(), goog2.getTickerId());
    assertEquals(goog.toString(), goog2.toString());
    assertEquals(goog, goog2);
    
    Stock emini = new Stock("/ES");
    Stock emini2 = new Stock(emini.getTickerId());
    assertEquals(emini, emini2);
    
    assertNotSame(emini.getTickerId(), goog.getTickerId());
  }
  
  @Test
  public void testGetTickerId() throws Exception {
    int t = getTickerId("AAP9");
    
    System.err.println("AAP9=" + t);
    
    System.err.println("from tickerId = " + fromTickerId(t));
  }
  
  public void no_testApiTick() throws Exception {
    String[] SYMBOLS = new String[] {
        "GOOG",
        "BRK.B",
    };
    
    String symbol = "GOOG/A".intern();
    
    System.err.println(Character.MAX_CODE_POINT);
    System.err.println(Character.MIN_CODE_POINT);
    System.err.println(Character.MIN_VALUE);
    System.err.println(Character.getNumericValue(Character.MAX_VALUE));
    System.err.println(Character.getNumericValue(Character.MIN_VALUE));
    
    System.err.println(">>>>>>");
    
    for (int i = 0; i < symbol.length(); i++) {
      System.err.println(symbol.codePointAt(i) + "," + Character.getNumericValue(symbol.charAt(i)));
    }
    
    char[] alphabets = new char[] { 'a', 'A', 'z', 'Z'};
    for (int i = 0; i < alphabets.length; i++) {
      System.err.println(Character.getNumericValue(alphabets[i]));
    }
  }
}
