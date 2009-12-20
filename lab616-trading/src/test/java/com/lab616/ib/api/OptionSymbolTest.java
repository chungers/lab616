// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import junit.framework.TestCase;

/**
 *
 *
 * @author david
 *
 */
public class OptionSymbolTest extends TestCase {

  public void testCallMonths() throws Exception {
    for (int i = DateTimeConstants.JANUARY; i <= DateTimeConstants.DECEMBER; i++) {
      DateTime date = new DateTime().withMonthOfYear(i);
      Character call = OptionSymbol.getCallMonth(date);
      assertEquals(OptionSymbol.CALL_MONTHS.get(i), call);
    }
  }


  public void testPutMonths() throws Exception {
    for (int i = DateTimeConstants.JANUARY; i <= DateTimeConstants.DECEMBER; i++) {
      DateTime date = new DateTime().withMonthOfYear(i);
      Character call = OptionSymbol.getPutMonth(date);
      assertEquals(OptionSymbol.PUT_MONTHS.get(i), call);
    }
  }
  
  public void testStrikes() throws Exception {
    assertEquals((Character) 'A', OptionSymbol.getStrike(5.));
    assertEquals((Character) 'E', OptionSymbol.getStrike(25.));
    assertEquals((Character) 'F', OptionSymbol.getStrike(230.));
    assertEquals((Character) 'B', OptionSymbol.getStrike(110.));
    assertEquals((Character) 'C', OptionSymbol.getStrike(15.));
    assertEquals((Character) 'T', OptionSymbol.getStrike(100.));
    assertEquals((Character) 'Q', OptionSymbol.getStrike(385.));
    assertEquals((Character) 'R', OptionSymbol.getStrike(190.));
    assertEquals((Character) 'O', OptionSymbol.getStrike(475.));
    assertEquals((Character) 'J', OptionSymbol.getStrike(50.));
    assertEquals((Character) 'L', OptionSymbol.getStrike(260.));
    assertEquals((Character) 'M', OptionSymbol.getStrike(65.));
    assertEquals((Character) 'N', OptionSymbol.getStrike(170.));
    assertEquals((Character) 'P', OptionSymbol.getStrike(80.));
    assertEquals((Character) 'R', OptionSymbol.getStrike(590.));
    assertEquals((Character) 'W', OptionSymbol.getStrike(47.5));
    assertEquals((Character) 'X', OptionSymbol.getStrike(52.5));
    assertEquals((Character) 'Y', OptionSymbol.getStrike(87.5));
    assertEquals((Character) 'Z', OptionSymbol.getStrike(122.5));
  }
  
  public void testSymbols() throws Exception {
    assertEquals("AAPLAA", OptionSymbol.getCallOptionSymbol(
        "AAPL", new DateTime().withMonthOfYear(1), 205.));

    assertEquals("AAPLMA", OptionSymbol.getPutOptionSymbol(
        "AAPL", new DateTime().withMonthOfYear(1), 205.));
  
    assertEquals("AAPLLA", OptionSymbol.getCallOptionSymbol(
        "AAPL", new DateTime().withMonthOfYear(12), 205.));
  }
}
