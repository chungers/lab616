/******************************************************************************* 
 *  Copyright 2008 Amazon Technologies, Inc.
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  
 *  You may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at: http://aws.amazon.com/apache2.0
 *  This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR 
 *  CONDITIONS OF ANY KIND, either express or implied. See the License for the 
 *  specific language governing permissions and limitations under the License.
 * ***************************************************************************** 
 *    __  _    _  ___ 
 *   (  )( \/\/ )/ __)
 *   /__\ \    / \__ \
 *  (_)(_) \/\/  (___/
 * 
 *  Amazon Simple DB Java Library
 *  API Version: 2009-04-15
 *  Generated: Mon May 11 14:17:00 PDT 2009 
 * 
 */



package com.amazonaws.sdb.util;

import java.io.*;
import java.util.*;
import java.text.*;

/**
 * Provides collection of static functions for conversion of various values into strings that may be 
 * compared lexicographically.
 * 
 */
public class AmazonSimpleDBUtil {

    /** static value hardcoding date format used for conversation of Date into String */
    private static String dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    /**
     * Encodes positive integer value into a string by zero-padding number up to the specified number of digits.
     * 
     * @param	number positive integer to be encoded
     * @param	maxNumDigits maximum number of digits in the largest value in the data set
     * @return string representation of the zero-padded integer
     */
    public static String encodeZeroPadding(int number, int maxNumDigits) {
        String integerString = Integer.toString(number);
        int numZeroes = maxNumDigits - integerString.length();
        StringBuffer strBuffer = new StringBuffer(numZeroes + integerString.length());
        for (int i = 0; i < numZeroes; i++) {
            strBuffer.insert(i, '0');
        }
        strBuffer.append(integerString);
        return strBuffer.toString();
    }

    /**
     * Encodes positive float value into a string by zero-padding number up to the specified number of digits
     * 
     * @param	number positive float value to be encoded
     * @param	maxNumDigits	maximum number of digits preceding the decimal point in the largest value in the data set
     * @return string representation of the zero-padded float value
     */
    public static String encodeZeroPadding(float number, int maxNumDigits) {
        String floatString = Float.toString(number);
        int numBeforeDecimal = floatString.indexOf('.');
        numBeforeDecimal = (numBeforeDecimal >= 0 ? numBeforeDecimal : floatString.length());
        int numZeroes = maxNumDigits - numBeforeDecimal;
        StringBuffer strBuffer = new StringBuffer(numZeroes + floatString.length());
        for (int i = 0; i < numZeroes; i++) {
            strBuffer.insert(i, '0');
        }
        strBuffer.append(floatString);
        return strBuffer.toString();
    }

    /**
     * Decodes zero-padded positive integer value from the string representation
     * 
     * @param value zero-padded string representation of the integer
     * @return original integer value
     */
    public static int decodeZeroPaddingInt(String value) {
        return Integer.parseInt(value, 10);
    }

    /**
     * Decodes zero-padded positive float value from the string representation
     * 
     * @param value zero-padded string representation of the float value
     * @return original float value
     */
    public static float decodeZeroPaddingFloat(String value) {
        return Float.valueOf(value).floatValue();
    }

    /**
     * Encodes real integer value into a string by offsetting and zero-padding 
     * number up to the specified number of digits.  Use this encoding method if the data
     * range set includes both positive and negative values.
     * 
     * @param number integer to be encoded
     * @param maxNumDigits maximum number of digits in the largest absolute value in the data set
     * @param offsetValue offset value, has to be greater than absolute value of any negative number in the data set.
     * @return string representation of the integer
     */
    public static String encodeRealNumberRange(int number, int maxNumDigits, int offsetValue) {
        long offsetNumber = number + offsetValue;
        String longString = Long.toString(offsetNumber);
        int numZeroes = maxNumDigits - longString.length();
        StringBuffer strBuffer = new StringBuffer(numZeroes + longString.length());
        for (int i = 0; i < numZeroes; i++) {
            strBuffer.insert(i, '0');
        }
        strBuffer.append(longString);
        return strBuffer.toString();
    }

    /**
     * Encodes real float value into a string by offsetting and zero-padding 
     * number up to the specified number of digits.  Use this encoding method if the data
     * range set includes both positive and negative values.
     * 
     * @param number float to be encoded
     * @param maxDigitsLeft maximum number of digits left of the decimal point in the largest absolute value in the data set
     * @param maxDigitsRight maximum number of digits right of the decimal point in the largest absolute value in the data set, i.e. precision
     * @param offsetValue offset value, has to be greater than absolute value of any negative number in the data set.
     * @return string representation of the integer
     */
    public static String encodeRealNumberRange(float number, int maxDigitsLeft, int maxDigitsRight, int offsetValue) {
        int shiftMultiplier = (int) Math.pow(10, maxDigitsRight);
        long shiftedNumber = (long) Math.round(number * shiftMultiplier);
        long shiftedOffset = offsetValue * shiftMultiplier;
        long offsetNumber = shiftedNumber + shiftedOffset;
        String longString = Long.toString(offsetNumber);
        int numBeforeDecimal = longString.length();
        int numZeroes = maxDigitsLeft + maxDigitsRight - numBeforeDecimal;
        StringBuffer strBuffer = new StringBuffer(numZeroes + longString.length());
        for (int i = 0; i < numZeroes; i++) {
            strBuffer.insert(i, '0');
        }
        strBuffer.append(longString);
        return strBuffer.toString();
    }

    /**
     * Decodes integer value from the string representation that was created by 
     * using encodeRealNumberRange(..) function.
     * 
     * @param value string representation of the integer value
     * @param offsetValue offset value that was used in the original encoding
     * @return original integer value
     */
    public static int decodeRealNumberRangeInt(String value, int offsetValue) {
        long offsetNumber = Long.parseLong(value, 10);
        return (int) (offsetNumber - offsetValue);
    }

    /**
     * Decodes float value from the string representation that was created by using encodeRealNumberRange(..) function.
     * 
     * @param value string representation of the integer value
     * @param maxDigitsRight maximum number of digits left of the decimal point in 
     * the largest absolute value in the data set (must be the same as the one used for encoding).
     * @param offsetValue offset value that was used in the original encoding
     * @return original float value
     */
    public static float decodeRealNumberRangeFloat(String value, int maxDigitsRight, int offsetValue) {
        long offsetNumber = Long.parseLong(value, 10);
        int shiftMultiplier = (int) Math.pow(10, maxDigitsRight);
        double tempVal = (double) (offsetNumber - offsetValue * shiftMultiplier);
        return (float) (tempVal / (double) (shiftMultiplier));
    }

    /**
     * Encodes date value into string format that can be compared lexicographically 
     * 
     * @param date date value to be encoded
     * @return string representation of the date value
     */
    public static String encodeDate(Date date) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
        /* Java doesn't handle ISO8601 nicely: need to add ':' manually */
        String result = dateFormatter.format(date);
        return result.substring(0, result.length() - 2) + ":" + result.substring(result.length() - 2);
    }

    /**
     * Decodes date value from the string representation created using encodeDate(..) function.
     * 
     * @param	value	string representation of the date value
     * @return			original date value
     */
    public static Date decodeDate(String value) throws ParseException {
        String javaValue = value.substring(0, value.length() - 3) + value.substring(value.length() - 2);
        SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
        return dateFormatter.parse(javaValue);
    }
}
