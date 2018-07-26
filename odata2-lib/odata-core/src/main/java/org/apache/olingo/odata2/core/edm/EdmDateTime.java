/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 ******************************************************************************/
package org.apache.olingo.odata2.core.edm;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.olingo.odata2.api.edm.EdmFacets;
import org.apache.olingo.odata2.api.edm.EdmLiteralKind;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeException;

/**
 * Implementation of the EDM simple type DateTime.
 * 
 */
public class EdmDateTime extends AbstractSimpleType {

  private static final Pattern PATTERN = Pattern.compile(
      "(\\p{Digit}{1,4})-(\\p{Digit}{1,2})-(\\p{Digit}{1,2})"
          + "T(\\p{Digit}{1,2}):(\\p{Digit}{1,2})(?::(\\p{Digit}{1,2})(\\.(\\p{Digit}{0,9}?)0*)?)?");
  private static final Pattern JSON_PATTERN = Pattern.compile("/Date\\((-?\\p{Digit}+)\\)/");
  private static final EdmDateTime instance = new EdmDateTime();

  public static EdmDateTime getInstance() {
    return instance;
  }

  @Override
  public Class<?> getDefaultType() {
    return Calendar.class;
  }

  @Override
  protected <T> T internalValueOfString(final String value, final EdmLiteralKind literalKind, final EdmFacets facets,
      final Class<T> returnType) throws EdmSimpleTypeException {
    // In JSON, we allow also the XML literal form, so there is on purpose
    // no exception if the JSON pattern does not match.
    if (literalKind == EdmLiteralKind.JSON) {
      final Matcher matcher = JSON_PATTERN.matcher(value);
      if (matcher.matches()) {
        long millis;
        try {
          millis = Long.parseLong(matcher.group(1));
        } catch (final NumberFormatException e) {
          throw new EdmSimpleTypeException(EdmSimpleTypeException.LITERAL_ILLEGAL_CONTENT.addContent(value), e);
        }
        if (returnType.isAssignableFrom(Long.class)) {
          return returnType.cast(millis);
        } else if (returnType.isAssignableFrom(Date.class)) {
          return returnType.cast(new Date(millis));
        } else if (returnType.isAssignableFrom(Calendar.class)) {
          Calendar dateTimeValue = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
          dateTimeValue.clear();
          dateTimeValue.setTimeInMillis(millis);
          return returnType.cast(dateTimeValue);
        } else {
          throw new EdmSimpleTypeException(EdmSimpleTypeException.VALUE_TYPE_NOT_SUPPORTED.addContent(returnType));
        }
      }
    }

    Calendar dateTimeValue = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    dateTimeValue.clear();

    String valueString;
    if (literalKind == EdmLiteralKind.URI) {
      //OLINGO-883 prefix is case insensitve so we need to check with lower case if we want to use startsWith()
      if (value.length() > 10 && value.toLowerCase().startsWith("datetime'") && value.endsWith("'")) {
        valueString = value.substring(9, value.length() - 1);
      } else {
        throw new EdmSimpleTypeException(EdmSimpleTypeException.LITERAL_ILLEGAL_CONTENT.addContent(value));
      }
    } else {
      valueString = value;
    }

    Matcher matcher = PATTERN.matcher(valueString);
    if (!matcher.matches()) {
       return EdmDateTimeOffset.getInstance().internalValueOfString(value, literalKind, facets, returnType);
    }

    dateTimeValue.set(
        Short.parseShort(matcher.group(1)),
        Byte.parseByte(matcher.group(2)) - 1, // month is zero-based
        Byte.parseByte(matcher.group(3)),
        Byte.parseByte(matcher.group(4)),
        Byte.parseByte(matcher.group(5)),
        matcher.group(6) == null ? 0 : Byte.parseByte(matcher.group(6)));

    int nanoSeconds = 0;
    if (matcher.group(7) != null) {
      if (matcher.group(7).length() == 1 || matcher.group(7).length() > 10) {
        throw new EdmSimpleTypeException(EdmSimpleTypeException.LITERAL_ILLEGAL_CONTENT.addContent(value));
      }
      final String decimals = matcher.group(8);
      if (facets != null && facets.getPrecision() != null && facets.getPrecision() < decimals.length()) {
        throw new EdmSimpleTypeException(EdmSimpleTypeException.LITERAL_FACETS_NOT_MATCHED.addContent(value, facets));
      }
      nanoSeconds = Integer.parseInt(decimals + "000000000".substring(decimals.length()));
      if (!(returnType.isAssignableFrom(Timestamp.class))) {
        if (nanoSeconds % (1000 * 1000) == 0) {
          dateTimeValue.set(Calendar.MILLISECOND, nanoSeconds / (1000 * 1000));
        } else {
          throw new EdmSimpleTypeException(EdmSimpleTypeException.LITERAL_ILLEGAL_CONTENT.addContent(value));
        }
      }
    }

    // The Calendar class does not check any values until a get method is called,
    // so we do just that to validate the fields set above, not because we want
    // to return something else. For strict checks, the lenient mode is switched
    // off temporarily.
    dateTimeValue.setLenient(false);
    try {
      dateTimeValue.get(Calendar.MILLISECOND);
    } catch (final IllegalArgumentException e) {
      throw new EdmSimpleTypeException(EdmSimpleTypeException.LITERAL_ILLEGAL_CONTENT.addContent(value), e);
    }
    dateTimeValue.setLenient(true);

    if (returnType.isAssignableFrom(Calendar.class)) {
      return returnType.cast(dateTimeValue);
    } else if (returnType.isAssignableFrom(Long.class)) {
      return returnType.cast(dateTimeValue.getTimeInMillis());
    } else if (returnType.isAssignableFrom(Date.class)) {
      return returnType.cast(dateTimeValue.getTime());
    } else if (returnType.isAssignableFrom(Timestamp.class)) {
        Timestamp timestamp = new Timestamp(dateTimeValue.getTimeInMillis());
        timestamp.setNanos(nanoSeconds);
        return returnType.cast(timestamp);
    } else {
      throw new EdmSimpleTypeException(EdmSimpleTypeException.VALUE_TYPE_NOT_SUPPORTED.addContent(returnType));
    }
  }

  @Override
  protected <T> String internalValueToString(final T value, final EdmLiteralKind literalKind, final EdmFacets facets)
      throws EdmSimpleTypeException {
    long timeInMillis;
    if (value instanceof Date) {
      timeInMillis = ((Date) value).getTime();
    } else if (value instanceof Calendar) {
      timeInMillis = ((Calendar) value).getTimeInMillis();
    } else if (value instanceof Long) {
      timeInMillis = ((Long) value).longValue();
    } else {
      throw new EdmSimpleTypeException(EdmSimpleTypeException.VALUE_TYPE_NOT_SUPPORTED.addContent(value.getClass()));
    }

    if (literalKind == EdmLiteralKind.JSON) {
      if (value instanceof Timestamp && ((Timestamp) value).getNanos() % (1000 * 1000) != 0) {
        throw new EdmSimpleTypeException(EdmSimpleTypeException.VALUE_ILLEGAL_CONTENT.addContent(value));
      } else {
        return "/Date(" + timeInMillis + ")/";
      }
    }

    Calendar dateTimeValue = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    dateTimeValue.setTimeInMillis(timeInMillis);

    StringBuilder result = new StringBuilder(29); // 29 characters are enough for nanosecond precision.
    final int year = dateTimeValue.get(Calendar.YEAR);
    appendTwoDigits(result, year / 100);
    appendTwoDigits(result, year % 100);
    result.append('-');
    appendTwoDigits(result, dateTimeValue.get(Calendar.MONTH) + 1); // month is zero-based
    result.append('-');
    appendTwoDigits(result, dateTimeValue.get(Calendar.DAY_OF_MONTH));
    result.append('T');
    appendTwoDigits(result, dateTimeValue.get(Calendar.HOUR_OF_DAY));
    result.append(':');
    appendTwoDigits(result, dateTimeValue.get(Calendar.MINUTE));
    result.append(':');
    appendTwoDigits(result, dateTimeValue.get(Calendar.SECOND));

    final int fractionalSecs = value instanceof Timestamp ?
        ((Timestamp) value).getNanos() :
        dateTimeValue.get(Calendar.MILLISECOND);
    try {
      appendFractionalSeconds(result, fractionalSecs, value instanceof Timestamp, facets);
    } catch (final IllegalArgumentException e) {
      throw new EdmSimpleTypeException(EdmSimpleTypeException.VALUE_FACETS_NOT_MATCHED.addContent(value, facets), e);
    }

    return result.toString();
  }

  /**
   * Appends the given number to the given string builder,
   * assuming that the number has at most two digits, performance-optimized.
   * @param result a {@link StringBuilder}
   * @param number an integer that must satisfy <code>0 <= number <= 99</code>
   */
  private static void appendTwoDigits(final StringBuilder result, final int number) {
    result.append((char) ('0' + number / 10));
    result.append((char) ('0' + number % 10));
  }

  /**
   * Appends the given milli- or nanoseconds to the given string builder, performance-optimized.
   * @param result a {@link StringBuilder}
   * @param fractionalSeconds fractional seconds (nonnegative and assumed to be in the valid range)
   * @param isNano whether the value is to be interpreted as nanoseconds (milliseconds if false)
   * @param facets the EDM facets containing an upper limit for decimal digits (optional, defaults to zero)
   * @throws IllegalArgumentException if precision is not met
   */
  protected static void appendFractionalSeconds(StringBuilder result, final int fractionalSeconds,
      final boolean isNano, final EdmFacets facets) throws IllegalArgumentException {
    int significantDigits = 0;
    if (fractionalSeconds > 0) {
      // Determine the number of significant digits.
      significantDigits = isNano ? 9 : 3;
      int output = fractionalSeconds;
      while (output % 10 == 0) {
        output /= 10;
        significantDigits--;
      }

      result.append('.');
      for (int d = 100 * (isNano ? 1000 * 1000 : 1); d > 0; d /= 10) {
        final byte digit = (byte) (fractionalSeconds % (d * 10) / d);
        if (digit > 0 || fractionalSeconds % d > 0) {
          result.append((char) ('0' + digit));
        }
      }
    }

    // Check precision constraint.
    final Integer precision = facets == null || facets.getPrecision() == null ? null : facets.getPrecision();
    if (precision != null) {
      if (precision < significantDigits) {
        throw new IllegalArgumentException();
      } else {
        // Add additional zeroes if the precision is larger than the number of significant digits.
        if (significantDigits == 0 && precision > 0) {
          result.append('.');
        }
        for (int i = significantDigits; i < precision; i++) {
          result.append('0');
        }
      }
    }
  }

  @Override
  public String toUriLiteral(final String literal) throws EdmSimpleTypeException {
    return "datetime'" + literal + "'";
  }
}
