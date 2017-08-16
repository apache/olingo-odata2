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
package org.apache.olingo.odata2.core.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.olingo.odata2.api.batch.BatchException;
import org.apache.olingo.odata2.api.exception.MessageReference;

/**
 *
 */
public class AcceptParser {

  private static final String BAD_REQUEST = "400";
  private static final String ALL = "*";
  private static final String REG_EX_QUALITY_FACTOR = "q=((?:1\\.0{0,3})|(?:0\\.[0-9]{0,2}[1-9]))";
  private static final String REG_EX_OPTIONAL_WHITESPACE = "\\s?";
  private static final Pattern REG_EX_ACCEPT = Pattern.compile("([a-z\\*]+/[a-z0-9\\+\\*\\-=;\\s]+)");
  private static final Pattern REG_EX_ACCEPT_WITH_Q_FACTOR = Pattern.compile(REG_EX_ACCEPT + "(?:;"
      + REG_EX_OPTIONAL_WHITESPACE + REG_EX_QUALITY_FACTOR + ")?");
  private static final Pattern REG_EX_ACCEPT_LANGUAGES = Pattern
      .compile("((?:(?:[a-zA-Z]{1,8})(?:-[a-zA-Z0-9]{1,8}){0,})|(?:\\*))");
  private static final Pattern REG_EX_ACCEPT_LANGUAGES_WITH_Q_FACTOR = Pattern.compile(REG_EX_ACCEPT_LANGUAGES + "(?:;"
      + REG_EX_OPTIONAL_WHITESPACE + REG_EX_QUALITY_FACTOR + ")?");

  private static final double QUALITY_PARAM_FACTOR = 0.001;

  private List<String> acceptHeaderValues = new ArrayList<String>();
  private List<String> acceptLanguageHeaderValues = new ArrayList<String>();

  public List<String> parseAcceptHeaders() throws BatchException {
    return parseQualifiedHeader(acceptHeaderValues,
        REG_EX_ACCEPT_WITH_Q_FACTOR,
        BatchException.INVALID_ACCEPT_HEADER);
  }

  public List<String> parseAcceptableLanguages() throws BatchException {
    return parseQualifiedHeader(acceptLanguageHeaderValues,
        REG_EX_ACCEPT_LANGUAGES_WITH_Q_FACTOR,
        BatchException.INVALID_ACCEPT_LANGUAGE_HEADER);
  }

  private List<String> parseQualifiedHeader(List<String> headerValues, Pattern regEx, MessageReference exectionMessage)
      throws BatchException {
    final TreeSet<Accept> acceptTree = new TreeSet<AcceptParser.Accept>();
    final List<String> acceptHeaders = new ArrayList<String>();

    for (final String headerValue : headerValues) {
      final String[] acceptParts = headerValue.split(",");

      for (final String part : acceptParts) {
        final Matcher matcher = regEx.matcher(part.trim());

        if (matcher.matches() && matcher.groupCount() == 2) {
          final Accept acceptHeader = getQualifiedHeader(matcher);
          acceptTree.add(acceptHeader);
        } else {
          throw new BatchException(exectionMessage.addContent(part), BAD_REQUEST);
        }
      }
    }

    for (Accept accept : acceptTree) {
      if (!acceptHeaders.contains(accept.getValue())) {
        acceptHeaders.add(accept.getValue());
      }
    }
    return acceptHeaders;
  }

  private Accept getQualifiedHeader(final Matcher matcher) {
    final String acceptHeaderValue = matcher.group(1);
    double qualityFactor = matcher.group(2) != null ? Double.parseDouble(matcher.group(2)) : 1d;
    qualityFactor = getQualityFactor(acceptHeaderValue, qualityFactor);

    return new Accept().setQuality(qualityFactor).setValue(acceptHeaderValue);
  }
  
  private double getQualityFactor(final String acceptHeaderValue, double qualityFactor) {
    int paramNumber = 0;
    double typeFactor = 0.0;
    double subtypeFactor = 0.0;
    String[] mediaRange = acceptHeaderValue.split("(?=[^;]+);");
    String[] mediaTypes = mediaRange[0].split("/");

    if (mediaTypes.length == 2) {
      String type = mediaTypes[0];
      String subtype = mediaTypes[1];
      if (!ALL.equals(type)) {
        typeFactor = 0.001;
      }
      if (!ALL.equals(subtype)) {
        subtypeFactor = 0.001;
      }
    }
    if (mediaRange.length == 2) {
      String[] parameters = mediaRange[1].split(";\\s?");
      paramNumber = parameters.length;
    }

    qualityFactor = qualityFactor + paramNumber * QUALITY_PARAM_FACTOR + typeFactor + subtypeFactor;
    return qualityFactor;
  }
  
  public void addAcceptHeaderValue(final String headerValue) {
    acceptHeaderValues.add(headerValue);
  }

  public void addAcceptLanguageHeaderValue(final String headerValue) {
    acceptLanguageHeaderValues.add(headerValue);
  }

  private static class Accept implements Comparable<Accept> {
    private double quality;
    private String value;

    public String getValue() {
      return value;
    }

    public Accept setValue(final String value) {
      this.value = value;
      return this;
    }

    public Accept setQuality(final double quality) {
      this.quality = quality;
      return this;
    }

    @Override
    public int compareTo(Accept o) {
      if (quality <= o.quality) {
        return 1;
      } else {
        return -1;
      }
    }
  }
}
