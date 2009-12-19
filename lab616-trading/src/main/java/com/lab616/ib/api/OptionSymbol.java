// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api;

import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import com.google.common.collect.ImmutableMap;


/**
 * 
 * See {@linkplain http://www.optionsxpress.com/educate/opt_symbols.aspx}.
 *
 * @author david
 *
 */
public class OptionSymbol {

  static final Map<Integer, Character> CALL_MONTHS = 
    new ImmutableMap.Builder<Integer, Character>()
    .put(DateTimeConstants.JANUARY, 'A')
    .put(DateTimeConstants.FEBRUARY, 'B')
    .put(DateTimeConstants.MARCH, 'C')
    .put(DateTimeConstants.APRIL, 'D')
    .put(DateTimeConstants.MAY, 'E')
    .put(DateTimeConstants.JUNE, 'F')
    .put(DateTimeConstants.JULY, 'G')
    .put(DateTimeConstants.AUGUST, 'H')
    .put(DateTimeConstants.SEPTEMBER, 'I')
    .put(DateTimeConstants.OCTOBER, 'J')
    .put(DateTimeConstants.NOVEMBER, 'K')
    .put(DateTimeConstants.DECEMBER, 'L')
    .build();
  
  static final Map<Integer, Character> PUT_MONTHS = 
    new ImmutableMap.Builder<Integer, Character>()
    .put(DateTimeConstants.JANUARY, 'M')
    .put(DateTimeConstants.FEBRUARY, 'N')
    .put(DateTimeConstants.MARCH, 'O')
    .put(DateTimeConstants.APRIL, 'P')
    .put(DateTimeConstants.MAY, 'Q')
    .put(DateTimeConstants.JUNE, 'R')
    .put(DateTimeConstants.JULY, 'S')
    .put(DateTimeConstants.AUGUST, 'T')
    .put(DateTimeConstants.SEPTEMBER, 'U')
    .put(DateTimeConstants.OCTOBER, 'V')
    .put(DateTimeConstants.NOVEMBER, 'W')
    .put(DateTimeConstants.DECEMBER, 'X')
    .build();
  
  public static Character getCallMonth(DateTime date) {
    int delta = date.getMonthOfYear() - DateTimeConstants.JANUARY;
    return (char) ('A' + delta);
  }

  public static Character getPutMonth(DateTime date) {
    int delta = date.getMonthOfYear() - DateTimeConstants.JANUARY;
    return (char) ('M' + delta);
  }
  
  public static Character getStrike(double strike) {
    char val;
    if ((strike - 0.5) % 1. == 0.) {
      val = (char) ('U' + Math.max(0, (strike - 7.5) / 5. % 6.));
    } else {
      int delta = (int) ((strike - 5.) / 5. % 20.);
      val = (char) ('A' + Math.max(0, delta));
    }
    if (val >= 'A' && val <= 'Z') {
      return val;
    }
    return null;
  }
  
  public static String getCallOptionSymbol(String base, DateTime expiration, 
      double strike) {
    return String.format("%s%s%s", base, getCallMonth(expiration), getStrike(strike));
  }

  public static String getPutOptionSymbol(String base, DateTime expiration, 
      double strike) {
    return String.format("%s%s%s", base, getPutMonth(expiration), getStrike(strike));
  }
}
