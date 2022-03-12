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
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.olingo.odata2.api.edm.EdmFacets;
import org.apache.olingo.odata2.api.edm.EdmLiteralKind;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeException;

/**
 * Implementation of the EDM simple type DateTimeOffset.
 * 
 * Details about parsing of time strings to value objects can be found in the
 * {@link org.apache.olingo.odata2.api.edm.EdmSimpleType} documentation.
 */
public class EdmDateTimeOffset extends AbstractSimpleType {

  private static final Pattern PATTERN = Pattern.compile(
      "\\p{Digit}{1,4}-\\p{Digit}{1,2}-\\p{Digit}{1,2}"
          + "T\\p{Digit}{1,2}:\\p{Digit}{1,2}(?::\\p{Digit}{1,2}(?:\\.\\p{Digit}{1,7})?)?"
          + "(Z|([-+]\\p{Digit}{1,2}:\\p{Digit}{2}))?");
  private static final Pattern JSON_PATTERN = Pattern.compile(
      "/Date\\((-?\\p{Digit}+)(?:(\\+|-)(\\p{Digit}{1,4}))?\\)/");
  private static final EdmDateTimeOffset instance = new EdmDateTimeOffset();

  public static EdmDateTimeOffset getInstance() {
    return instance;
  }

  @Override
  public Class<?> getDefaultType() {
    return Calendar.class;
  }

  @Override
  protected <T> T internalValueOfString(final String value, final EdmLiteralKind literalKind, final EdmFacets facets,
      final Class<T> returnType) throws EdmSimpleTypeException {
    if (literalKind == EdmLiteralKind.URI) {
      //OLINGO-883 prefix is case insensitve so we need to check with lower case if we want to use startsWith()
      if (value.length() > 16 && value.toLowerCase().startsWith("datetimeoffset'") && value.endsWith("'")) {
        return internalValueOfString(value.substring(15, value.length() - 1), EdmLiteralKind.DEFAULT, facets,
            returnType);
      } else {
        throw new EdmSimpleTypeException(EdmSimpleTypeException.LITERAL_ILLEGAL_CONTENT.addContent(value));
      }
    }

    Calendar dateTimeValue = null;
    long millis = 0;

    if (literalKind == EdmLiteralKind.JSON) {
      final Matcher matcher = JSON_PATTERN.matcher(value);
      if (matcher.matches()) {
        try {
          millis = Long.parseLong(matcher.group(1));
        } catch (final NumberFormatException e) {
          throw new EdmSimpleTypeException(EdmSimpleTypeException.LITERAL_ILLEGAL_CONTENT.addContent(value), e);
        }
        String timeZone = "GMT";
        if (matcher.group(2) != null) {
          final int offsetInMinutes = Integer.parseInt(matcher.group(3));
          if (offsetInMinutes >= 24 * 60) {
            throw new EdmSimpleTypeException(EdmSimpleTypeException.LITERAL_ILLEGAL_CONTENT.addContent(value));
          }
          if (offsetInMinutes != 0) {
            timeZone += matcher.group(2) + String.valueOf(offsetInMinutes / 60)
                + ":" + String.format("%02d", offsetInMinutes % 60);
            // Convert the local-time milliseconds to UTC.
            millis -= ("+".equals(matcher.group(2)) ? 1 : -1) * offsetInMinutes * 60 * 1000;
          }
        }
        dateTimeValue = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
      }
    }

    int nanoSeconds = 0;
    if (dateTimeValue == null) {
      final Matcher matcher = PATTERN.matcher(value);
      if (!matcher.matches()) {
        throw new EdmSimpleTypeException(EdmSimpleTypeException.LITERAL_ILLEGAL_CONTENT.addContent(value));
      }

      final String timeZoneOffset =
          matcher.group(1) != null && matcher.group(2) != null && !matcher.group(2).matches("[-+]0+:0+") ? matcher
              .group(2) : null;
      dateTimeValue = Calendar.getInstance(TimeZone.getTimeZone("GMT" + timeZoneOffset));
      if (dateTimeValue.get(Calendar.ZONE_OFFSET) == 0 && timeZoneOffset != null) {
        throw new EdmSimpleTypeException(EdmSimpleTypeException.LITERAL_ILLEGAL_CONTENT.addContent(value));
      }
      dateTimeValue.clear();
      final Timestamp timestamp = EdmDateTime.getInstance().internalValueOfString(
          value.substring(0, matcher.group(1) == null ? value.length() : matcher.start(1)),
          EdmLiteralKind.DEFAULT, facets, Timestamp.class);
      millis = timestamp.getTime() - dateTimeValue.get(Calendar.ZONE_OFFSET);
      nanoSeconds = timestamp.getNanos();
      if (nanoSeconds % (1000 * 1000) != 0 && !returnType.isAssignableFrom(Timestamp.class)) {
        throw new EdmSimpleTypeException(EdmSimpleTypeException.LITERAL_ILLEGAL_CONTENT.addContent(value));
      }
    }

    if (returnType.isAssignableFrom(Calendar.class)) {
      dateTimeValue.clear();
      dateTimeValue.setTimeInMillis(millis);
      return returnType.cast(dateTimeValue);
    } else if (returnType.isAssignableFrom(Long.class)) {
      return returnType.cast(millis);
    } else if (returnType.isAssignableFrom(Date.class)) {
      return returnType.cast(new Date(millis));
    } else if (returnType.isAssignableFrom(Timestamp.class)) {
        Timestamp timestamp = new Timestamp(millis);
        if (literalKind != EdmLiteralKind.JSON) {
          timestamp.setNanos(nanoSeconds);
        }
        return returnType.cast(timestamp);
    } else {
      throw new EdmSimpleTypeException(EdmSimpleTypeException.VALUE_TYPE_NOT_SUPPORTED.addContent(returnType));
    }
  }

  @Override
  protected <T> String internalValueToString(final T value, final EdmLiteralKind literalKind, final EdmFacets facets)
      throws EdmSimpleTypeException {
    Long milliSeconds; // number of milliseconds since 1970-01-01T00:00:00Z
    if (value instanceof Date) {
      milliSeconds = ((Date) value).getTime();
    } else if (value instanceof Calendar) {
      final Calendar dateTimeValue = (Calendar) ((Calendar) value).clone();
      milliSeconds = dateTimeValue.getTimeInMillis();
    } else if (value instanceof Long) {
      milliSeconds = (Long) value;
    } else if (value instanceof Instant) {
      try {
        milliSeconds = ((Instant) value).toEpochMilli();
      } catch (ArithmeticException e) { // in case the Instant is far away from epoch
        milliSeconds = Long.MAX_VALUE;
      }
    } else {
      throw new EdmSimpleTypeException(EdmSimpleTypeException.VALUE_TYPE_NOT_SUPPORTED.addContent(value.getClass()));
    }


    if (literalKind == EdmLiteralKind.JSON) {
        return "/Date(" + milliSeconds + ")/";
    } else {
      final String localTimeString =
          EdmDateTime.getInstance().valueToString(
              value instanceof Timestamp ? value : milliSeconds, EdmLiteralKind.DEFAULT, facets);

      return localTimeString + "Z";
    }
  }

  @Override
  public String toUriLiteral(final String literal) throws EdmSimpleTypeException {
    return "datetimeoffset'" + literal + "'";
  }
}
